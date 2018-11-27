

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {

	PrintWriter output;
	BufferedReader input;
	BufferedReader reader;
	Socket clientSocket;
	int lPort;
	String fileName;

	public ChatClient(int lPort, int sPort) {

		try {
			this.lPort = lPort;
			clientSocket = new Socket("localhost", sPort); 
			reader = new BufferedReader(new InputStreamReader(System.in));
			output = new PrintWriter(clientSocket.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		ChatClient client = new ChatClient(Integer.decode(args[1]), Integer.decode(args[3]));
		client.execute();

	}

	public void execute() {
		ExecutorService threads = Executors.newFixedThreadPool(5);
		threads.submit(this::reader);
		threads.submit(this::writer);
		threads.submit(this::connect);
		threads.shutdown();

	}

	public void writer() {
		try {
			String localInput;
			output.println(lPort);
			localInput = reader.readLine();
			output.println(localInput);
			while (localInput != null) {
				System.out.println(
						"Enter an opiton ('m', 'f', 'x'):\n" + "(M)essage (send)\n" + "(F)ile (request)\n" + "e(X)it ");
				localInput = reader.readLine();
				if (localInput.equals("m")) {
					System.out.println("Enter your message:");
					localInput = reader.readLine();
					output.println(localInput);
				} else if (localInput.equals("f")) {
					System.out.println("Who owns the file?");
					String owner = reader.readLine();
					System.out.println("Which file do you want?");
					this.fileName = reader.readLine();
					output.println("$" + owner);

				} else if (localInput.equals("x")) {
					break;
				} else if (localInput.length() == 0) {
					break;
				}


			}
			close();

		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}

	}

	public void reader() {
		try {
			String message;
			while ((message = input.readLine()) != null) {
				if (message.length() == 0) {
					break;
					}
					if (message.startsWith("$")) {
					String ret = message.substring(1);
					int port = Integer.decode(ret);
					ExecutorService threads = Executors.newFixedThreadPool(5);
					threads.submit(() -> acceptFile(fileName, port));
					threads.shutdown();
				}
				
				else{
				System.out.println(message);
				}
			}
			close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	public void connect() {
		try {
			ServerSocket serverSocket = new ServerSocket(lPort);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
				DataInputStream input = new DataInputStream(clientSocket.getInputStream());
				String fileName = input.readUTF();
				File file = new File(fileName);
				FileInputStream file_input = new FileInputStream(file);
				byte[] file_buffer = new byte[1500];
				int number_read;
				while ((number_read = file_input.read(file_buffer)) != -1) {
					output.write(file_buffer, 0, number_read);
				}
				file_input.close();
				clientSocket.close();
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}

	}

	public void acceptFile(String fileName, int port) {
		try {
			Socket clientSocket = new Socket("localhost", port);
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			output.writeUTF(fileName);
			FileOutputStream fileOut = new FileOutputStream(fileName);
			int number_read;
			byte[] buffer = new byte[1500];
			while ((number_read = input.read(buffer)) != -1) {
				fileOut.write(buffer, 0, number_read);
			}
			fileOut.close();
			clientSocket.close();

		} catch (Exception e) {
			System.exit(1);
		}

	}

	public void close() {
		try {
			clientSocket.shutdownOutput();
			clientSocket.close();
			System.exit(0);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);

		}

	}

}
