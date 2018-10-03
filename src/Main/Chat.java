package Main;

import javafx.concurrent.Task;


import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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

        while(!exit)
        {
            if(scanner.hasNext()){
                String input = scanner.nextLine();

                if(input.equals("0")|| input.toLowerCase().equals("exit")){
                    sersock.close();
                    exit=true;
                    break;
                }
                else if (input.toLowerCase().contains("ip")){
                    try {
                        System.out.println(InetAddress.getLocalHost().getHostAddress());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
                else if(input.toLowerCase().contains("port")){
                    System.out.println(portNumber);
                }
                else if(input.toLowerCase().contains("help")){
                    System.out.println("help me Please!");
                }
                else if(input.toLowerCase().contains("connect")){
                    String[] inputs=  input.toLowerCase().split("\\s+");

                    if(inputs.length == 3){
                        Socket socket = new Socket(inputs[1], Integer.parseInt(inputs[2]));
                        User user = new User(socket,Integer.parseInt(inputs[2]));
                        users.add(user);
                        user.sendMessage("sending connection from home"); //sending message
                        System.out.println("connect to "+ inputs[1] + " "+ inputs[2]);
                    }

                }
                else if (input.toLowerCase().contains("list")){
                    System.out.println(users.size());
                    for (User user:users) {
                        System.out.println(user);
                    }
                }

            }

        }
    }
}
