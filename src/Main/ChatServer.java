package Main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ChatServer {
	
	List<ClientThread> clients;
	
	public List<ClientThread> getClients(){
		return clients;
	}

	
    public static void main(String args[]) throws Exception
    {
    	
    	
    	 ServerSocket sersock = new ServerSocket(3000);
         System.out.println("Server  ready for chatting");
         Socket sock = sersock.accept( );
         // reading from keyboard (keyRead object)
         BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
         // sending to client (pwrite object)
         OutputStream ostream = sock.getOutputStream();
         PrintWriter pwrite = new PrintWriter(ostream, true);

         // receiving from server ( receiveRead  object)
         InputStream istream = sock.getInputStream();
         BufferedReader receiveRead = new BufferedReader(new InputStreamReader(istream));

         String receiveMessage, sendMessage;
         while(true)
         {
             if((receiveMessage = receiveRead.readLine()) != null)
             {
                 System.out.println(receiveMessage);
             }
             sendMessage = keyRead.readLine();
             pwrite.println(sendMessage);
             pwrite.flush();
    	
        }
    }
    
    public void addClient(ServerSocket servSock) {
    	
    	boolean connect = true;
    	while(connect) {
    		try {
    			Socket sock = servSock.accept();
    			
    			Thread thread = new Thread();
    			
    		} catch(IOException e) {
    			System.out.println("Failed to Add Client on Port");
    		}
    	}
    }
}
