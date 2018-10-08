package Main;

import javafx.concurrent.Task;


import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Chat {

    private static Scanner scanner = new Scanner(System.in);
    static boolean exit= false;
    static ArrayList<User> users;


    public static void main(String args[]) throws Exception
    {
        users = new ArrayList<>();
        int portNumber = Integer.parseInt(args[0]);
        ServerSocket sersock = new ServerSocket(portNumber);


        Task<Void> serverTask = new Task<Void>() {

            @Override
            public void run() {
                while(!sersock.isClosed()){
                    try{
                        Socket connection = sersock.accept();
                        User home = new User(connection, sersock.getLocalPort());
                        users.add(home);
                        Task<Void> chatTask = new Task<Void>() {
                            @Override
                            public void run() {
                                home.printMessage();
                            }

                            @Override
                            protected Void call() throws Exception {
                                return null;
                            }
                        };
                        Thread homeThread = new Thread(chatTask);
                        homeThread.start();
                    }catch (Exception e){
                        System.out.println("closing the server");
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
                    System.exit(0);
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
                    else {
                        System.out.println("please enter 'connect <ip> <port>");
                    }

                }
                else if (input.toLowerCase().contains("list")){
                    System.out.println(users.size());
                    for (User user:users) {
                        System.out.println(user);
                    }
                }else {
                    System.out.println("type 'help' for some assistance");
                }

            }

        }
    }
}
