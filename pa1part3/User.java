//package broadcast;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.net.UnknownHostException;


public class User extends Thread {

	// The user socket
	private static Socket userSocket = null;
	// The output stream
	private static PrintStream output_stream = null;
	// The input stream
	private static BufferedReader input_stream = null;

	private static BufferedReader inputLine = null;
	private static boolean closed = false;

	public static void main(String[] args) {

		// The default port.
		int portNumber = 58888;
		// The default host.
		String host = "localhost";

		if (args.length < 2) {
			System.out
			.println("Usage: java User <host> <portNumber>\n"
					+ "Now using host=" + host + ", portNumber=" + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}

		/*
		 * Open a socket on a given host and port. Open input and output streams.
		 */
		try {
			userSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			output_stream = new PrintStream(userSocket.getOutputStream());
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host "
					+ host);
		}

		/*
		 * If everything has been initialized then we want to write some data to the
		 * socket we have opened a connection to on port portNumber.
		 */
		if (userSocket != null && output_stream != null && input_stream != null) {
			try {                
				/* Create a thread to read from the server. */
				new Thread(new User()).start();

				// Get user name and join the social net
				// Read user input and send protocol message to server
				while (!closed) {
					String userMessage = new String();
					String userInput = inputLine.readLine().trim();
					if (userInput.startsWith("@connect")){
						output_stream.println("#friendme " + userInput.substring(9));
					}
					else if (userInput.startsWith("@friend")){
						output_stream.println("#friends " + userInput.substring(8));
					}
					else if (userInput.startsWith("@deny")){
						output_stream.println("#FriendRequestDenied " + userInput.substring(6));
					}
					else if (userInput.startsWith("@disconnect")){
						output_stream.println("#unfriend " + userInput.substring(12));
					}
					else if (userInput.startsWith("@Exit")){
						output_stream.println("#Bye");
					}
					else {
						output_stream.println(userInput);
					}

				}
				/*
				 * Close the output stream, close the input stream, close the socket.
				 */
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

	/*
	 * Create a thread to read from the server.
	 */
	public void run() {
		/*
		 * Keep on reading from the socket till we receive a Bye from the
		 * server. Once we received that then we want to break.
		 */
		String responseLine;
		
		try {
			while ((responseLine = input_stream.readLine()) != null) {

				// Display on console based on what protocol message we get from server.
				if (responseLine.startsWith("#Welcome ")){
					System.out.println("###You have successfully connected!");
					System.out.println("###Here are the rules for the chat room. Please read before starting chat.");
					System.out.println("###1. If you want to add friends, please enter @connect <username>.");
					System.out.println("###2. If you want to accept friend request, please enter @friend <requestor username>.");
					System.out.println("###3. If you want to deny friend request, please enter @deny <username>.");
					System.out.println("###4. If you want to disconnect with friends, please enter @disconnect <username>.");
					System.out.println("###5. If you want to quit, please enter @Exit.");
					System.out.println("###Now enjoy your chat.");
				}
				else if (responseLine.startsWith("#Bye")){
					System.out.println("#Thank you, see you next time.");
					break;
				}
				else if (responseLine.startsWith("#busy")){
					System.out.println("#Sorry, chat room is full now.");
				}
				else if (responseLine.startsWith("#friendme")){
					System.out.println("#"+responseLine.substring(10) + " wants to be your friend.");
				}
				else if (responseLine.startsWith("#OKfriends")){
					System.out.println("#"+responseLine.split(" ")[1] + " and " + responseLine.split(" ")[2] + " are now friends.");
				}
				else if (responseLine.startsWith("#DenyFriendRequest")){
					System.out.println("#"+responseLine.substring(19) + " rejected your request.");
				}
				else if (responseLine.startsWith("#NotFriends")){
					System.out.println("#"+responseLine.split(" ")[1] + " and " + responseLine.split(" ")[2] + " are no longer friends.");
				}
				else {
					System.out.println(responseLine);
				}


			}
			closed = true;
			output_stream.close();
			input_stream.close();
			userSocket.close();
			System.exit(0);
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}



