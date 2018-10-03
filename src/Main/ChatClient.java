package Main;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
	
	String host ="localhost";
	int portNum;
	
	public ChatClient(String host, int portNum) {
		this.host = host;
		this.portNum = portNum;
	}

	
	public void startClient(Scanner scanner) {
		
		try {
			Socket clientSock = new Socket(host, portNum);
			Thread.sleep(1000);
			
			
		}catch(IOException e) {
			System.err.println("Connection Error");
		}catch(InterruptedException e) {
			System.out.println("Connection Interrupted");
		}
		
	}
}
