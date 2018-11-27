

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

	
	class client {
		private String name;
		private Socket socket;
		private PrintWriter printWriter;
		private int lPort;
		public client(String name,Socket socket, PrintWriter printWriter, int lPort) {
			this.name = name;
			this.socket = socket;
			this.printWriter = printWriter;
			this.lPort = lPort;
		}
		
		public String getName() {
			return name;
		}
		
		public Socket getSocket() {
			return socket;
		}
		
		public PrintWriter getPrintWriter() {
			return printWriter;
		}
		
		public int getLPort() {
			return lPort;
		}
	}

	ServerSocket serverSocket;
	ExecutorService threads;
	ArrayList<client> clients;
	//HashMap<Socket, PrintWriter> clients;
	HashMap<String,Integer> transfers;
	
	public ChatServer(int port){
		try {
			this.serverSocket = new ServerSocket(port);
			this.threads = Executors.newFixedThreadPool(5);
			//this.clients = new HashMap<Socket, PrintWriter>();
			this.clients = new ArrayList<>();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		
		
	}
	public static void main(String[] args) {
		int port = Integer.decode(args[0]);
		ChatServer server = new ChatServer(port);
		server.acceptClients();
		server.end();
		
		

	}
	
	
	public void acceptClients() {
		try {
		while(true) {
			Socket clientSocket = serverSocket.accept();
			PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
			//clients.put(clientSocket, output);
			threads.submit(() -> handleClient(clientSocket, output));
			
		}
		}catch(Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	public void handleClient(Socket client, PrintWriter output) {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String message = null;
			int lPort = Integer.decode(input.readLine());
			String name = input.readLine();
			client single = new client(name,client,output,lPort);
			clients.add(single);
			//transfers.put(name, lPort);
			while ((message = input.readLine()) != null) {
				if(message.startsWith("$")){
					String transferTo = message.substring(1);
					for(client c : clients) {
						if(c.getName().equals(transferTo)) {
							output.println("$"+c.getLPort());
						}
					}
					
				}
				else {

//				for (Socket c : clients.keySet()) {
//					if (!c.equals(client)) {
//						clients.get(c).println(name + ": " + message);
//
//					}
//
//				}
				for (client c : clients) {
					if(!c.getName().equals(name)) {
						c.getPrintWriter().println(name + ": " + message);
					}
				}

			}
			}
		clients.remove(client);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void end() {
		threads.shutdown();
	}
}
