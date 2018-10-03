package Main;

import javafx.concurrent.Task;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Chat {

    private static Scanner scanner = new Scanner(System.in);
    static boolean exit= false;
    static ArrayList<User> users;


    public static void main(String args[]) throws Exception
    {
        users = new ArrayList<User>();
        int portNumber = Integer.parseInt(args[0]);
        ServerSocket sersock = new ServerSocket(portNumber);


        Task<Void> serverTask = new Task<Void>() {

            @Override
            public void run() {
                while(!sersock.isClosed()){
                    try{
                        Socket connection = sersock.accept();
                        User user = new User(connection, sersock.getLocalPort());
                        user.printMessage();
                        users.add(user);                   
                    }catch (Exception e){
                        System.out.println(e);
                    }


                }
            }

            @Override
            protected Void call() throws Exception {
                return  null;

            }
        };
        Thread serverThread= new Thread(serverTask);
        serverThread.start();
        System.out.println("Connection Successful! Type help for more info..");

        while(!exit)
        {
            if(scanner.hasNext()){
                String input = scanner.nextLine();

                if(input.equals("0")|| input.toLowerCase().equals("exit")){
                    sersock.close();
                    exit=true;
                    break;
                }
                else if (input.toLowerCase().contains("myip")){
                    try {
                        System.out.println(InetAddress.getLocalHost().getHostAddress());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                else if(input.toLowerCase().contains("myport")){
                    System.out.println(portNumber);
                }
                else if(input.toLowerCase().contains("help")){
                	
                    System.out.println("myip\t Displays the IP Address of This Process");
                    System.out.println("myport\t Displays the Port on Which this Process is Listening for Incoming Connections");
                    System.out.println("list\t Displays a list of all connections with Connection IDs");
                    System.out.println("connect <destination> <port no>\t Establishes a new connection to specified IP Address at specified port number");
                    System.out.println("terminate <connection id>\t Terminates the connection listed under the specified Connection ID");
                    System.out.println("send <connection id> <message>\t Sends the desired message to the Connection listed under the specified Connection ID");
                    System.out.println("exit\t Closes all connections and Terminates this Process");        
               
                }
                else if(input.toLowerCase().contains("connect")){
                    String[] inputs=  input.toLowerCase().split("\\s+");

                    if(inputs.length == 3){
                        Socket socket = new Socket(inputs[1], Integer.parseInt(inputs[2]));
                        User user = new User(socket,Integer.parseInt(inputs[2]));
                        users.add(user);
                        user.sendMessage("sending connection from home"); //sending message
                        System.out.println("connect to "+ inputs[1] + " "+ inputs[2]);
                        System.out.println("Connection Successful");
                    }

                }
                else if(input.toLowerCase().contains("terminate")){
                	String[] inputs = input.toLowerCase().split("\\s+");
                	
                	int userId = Integer.parseInt(inputs[1]);
                		for (User user:users) {
                			if(user.getId() == userId) {
                				users.remove(userId);
                			}
                			else {
                				System.out.println("No user exists");
                			}
                		}                	     	         	
                    	
                }
                else if(input.toLowerCase().contains("send")) {//send message to user with id given
                	String[] inputs = input.toLowerCase().split("\\s+");
                	
                		int userId = Integer.parseInt(inputs[1]);
                		User user = users.get(userId-1);
                		String message = inputs[2];
                		
                		if(user.getSocket()!=null) {
                			/*try {
                				user.sendMessage(message);
                			}catch(IOException s) {
                				System.out.println("Message Send Unsuccessful. Please check connection ID");
                			}*/
                		}
                	
                	
                }
                else if (input.toLowerCase().contains("list")){
                    System.out.println(users.size());
                    System.out.println("ID: \t IP Address \t\t Port Number");
                    for (User user:users) {
                        System.out.println(user.getId() + " : IP Address   " + user.getPort());
                        
                    }
                }

            }

        }
    }
}
