package Main;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientThread implements Runnable {
	
	Socket clientSock;
	PrintWriter output;
	ChatServer server;
	
	@Override
	public void run() {
		
		try {
			Scanner scanner = new Scanner(clientSock.getInputStream());
			this.output = new PrintWriter(clientSock.getOutputStream());
			
			while(!clientSock.isClosed()) {
				if(scanner.hasNextLine()) {
					String input = scanner.nextLine();
					for(ClientThread client : server.getClients()) {
						PrintWriter clientOutput = output;
						if(clientOutput != null) {
							clientOutput.write(input + "\n");
							clientOutput.flush();
						}
					}
				}
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public ClientThread(ChatServer server, Socket clientSock) {
		this.server = server;
		this.clientSock = clientSock;
	}
}
