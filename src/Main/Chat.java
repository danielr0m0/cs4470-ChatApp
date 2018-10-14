package Main;

import javafx.concurrent.Task;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Chat {

    private static Scanner scanner = new Scanner(System.in);
    static boolean exit = false;
    static ArrayList<User> users;


    public static void main(String args[]) throws Exception {
        users = new ArrayList<>();
        int portNumber = Integer.parseInt(args[0]);
        ServerSocket sersock = new ServerSocket(portNumber);


        Task<Void> serverTask = new Task<Void>() {

            @Override
            public void run() {
                Socket connection = null;
                User home = null;
                while (!sersock.isClosed()) {
                    try {
                        connection = sersock.accept();
                        home = new User(connection, sersock.getLocalPort());
                        users.add(home);
                        home.printMessage();
                    } catch (Exception e) {
                        System.out.println("closing the server");

                    } finally {
                        users.remove(home);
                        try {
                            connection.close();
                        } catch (IOException e) {
                            System.out.println("closed");
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }

            @Override
            protected Void call() throws Exception {
                return null;

            }
        };


        Thread serverThread = new Thread(serverTask);
        serverThread.start();

        System.out.println("Connection Successful! type help for more information..");

        while (!exit) {
            if (scanner.hasNext()) {
                String input = scanner.nextLine();

                if (input.equals("0") || input.toLowerCase().equals("exit")) {
                    for (User user : users) {
                        user.getSocket().close();
                    }
                    sersock.close();
                    exit = true;
                    System.exit(0);
                    break;
                } else if (input.toLowerCase().contains("ip")) {
                    try {
                        System.out.println(InetAddress.getLocalHost().getHostAddress());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                } else if (input.toLowerCase().contains("port")) {
                    System.out.println(portNumber);
                } else if (input.toLowerCase().contains("help")) {

                    System.out.println("myip\t Displays the IP Address of This Process");
                    System.out.println("myport\t Displays the Port on Which this Process is Listening for Incoming Connections");
                    System.out.println("list\t Displays a list of all connections with Connection IDs");
                    System.out.println("connect <destination> <port no>\t Establishes a new connection to specified IP Address at specified port number");
                    System.out.println("terminate <connection id>\t Terminates the connection listed under the specified Connection ID");
                    System.out.println("send <connection id> <message>\t Sends the desired message to the Connection listed under the specified Connection ID");
                    System.out.println("exit\t Closes all connections and Terminates this Process");

                } else if (input.toLowerCase().contains("connect")) {
                    String[] inputs = input.toLowerCase().split("\\s+");

                    if (inputs.length == 3) {
                        Socket socket = null;
                        try {
                            socket = new Socket(inputs[1], Integer.parseInt(inputs[2]));
                        } catch (Exception e) {
                            System.out.println("could not connect");
                        }
                        if (socket != null) {
                            User user = new User(socket, Integer.parseInt(inputs[2]));
                            boolean isDuplicate = false;

                            for (User checkUser : users) {
                                if (user.isEquals(checkUser)) {
                                    System.out.println("Connection with user already exist.");
                                    isDuplicate = true;
                                }
                            }

                            if (!isDuplicate) {
                                users.add(user);

                                user.sendMessage("sending connection from" + InetAddress.getLocalHost().getHostAddress() + ":" + portNumber); //sending message
                                System.out.println("Successful connection to " + inputs[1] + " " + inputs[2]);
                                Thread userThread = new Thread(user);
                                userThread.start();
                            }
                        }

                    } else {
                        System.out.println("please enter 'connect <ip> <port>");
                    }

                } else if (input.toLowerCase().contains("terminate")) {
                    String[] inputs = input.toLowerCase().split("\\s+");
                    boolean found = false;

                    int userId = (Integer.parseInt(inputs[1]));
                    if (userId < users.size() || userId > 0) {
                        for (int i = 0; i < users.size(); i++) {
                            if (i == userId - 1) {
                                users.get(i).sendMessage("Your connection has been terminated with " + InetAddress.getLocalHost().getHostAddress());
                                users.get(i).getSocket().close();
                                users.remove(userId - 1);
                                System.out.println("Your connection with user " + userId + " has been terminated successfully!");
                                found = true;
                                break;
                            }

                        }
                        if (!found) {
                            System.out.println("No user exists");
                        }
                    } else {
                        System.out.println("type list to get the correct id");
                    }


                } else if (input.toLowerCase().contains("send")) {//send message to user with id given
                    updateList();
                    String[] inputs = input.toLowerCase().split("\\s+");

                    String message = " ";
                    int userId = Integer.parseInt(inputs[1]);
                    if (userId < users.size() || userId > 0) {
                        User user = users.get(userId - 1);

                        //check if user is connected
                        if (user.getSocket() != null) {
                            for (int i = 2; i < inputs.length; i++) {
                                message += inputs[i] + " ";
                            }
                            user.sendMessage("message: " + message + "\n" + "from:" + InetAddress.getLocalHost().getHostAddress());
                        }
                        else {
                            System.out.println("type list to get the correct id");
                        }
                    } else {
                        System.out.println("Type list to see who you are connected");
                    }

                } else if (input.toLowerCase().contains("list")) {
                    updateList();
                    if (users.size() == 0) {
                        System.out.println("try connecting to someone...");
                    } else {
                        System.out.println("ID: \t IP Address \t\t Port Number");
                        int id = 1;
                        for (User user : users) {
                            System.out.println(id + "\t " + user.getSocket().getInetAddress() + "\t \t " + user.getPort());
                            id++;
                        }
                    }

                } else {
                    System.out.println("type 'help' for assistance");
                }

            }

        }
    }

    public static void updateList() {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getSocket() == null) {
                users.remove(i);
            }
        }
    }
}

