package Main;


import javafx.concurrent.Task;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ChatServer {

    private static Scanner scanner = new Scanner(System.in);
    static boolean exit= false;


    public static void main(String args[]) throws Exception
    {
        int portNumber = Integer.parseInt(args[0]);
        ServerSocket sersock = new ServerSocket(portNumber);

        Task<Void> serverTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                return null;

            }
        };
        Thread serverThread= new Thread(serverTask);
        serverThread.start();

        String receiveMessage, sendMessage;

        while(!exit)
        {
            if(scanner.hasNext()){
                String input = scanner.nextLine();

                if(input.equals("0")|| input.toLowerCase().equals("exit")){
                    exit=true;
                }
                if (input.toLowerCase().contains("ip")){
                    System.out.println(InetAddress.getLocalHost().getHostAddress());
                }
                if(input.toLowerCase().contains("port")){
                    System.out.println(portNumber);
                }
                if(input.toLowerCase().contains("help")){
                    System.out.println("help me Please!");
                }
                if(input.toLowerCase().contains("connect")){
                    System.out.println(input);
                    String[] inputs=  input.toLowerCase().split("\\s+");
                    for (int i = 0; i < inputs.length; i++) {
                        System.out.println(inputs[i]);
                    }
                    Socket socket = new Socket(inputs[1], Integer.parseInt(inputs[2]));
                    PrintWriter out= new PrintWriter(socket.getOutputStream(),true);
                    out.println("connected to "+ InetAddress.getLocalHost().getHostAddress());
                    System.out.println("connect to "+ inputs[1]);
                }

            }

        }
    }
}


//        Socket sock = sersock.accept();

//        reading from keyboard (keyRead object)
//        BufferedReader keyRead = new BufferedReader(new InputStreamReader(System.in));
//        // sending to client (pwrite object)
//        OutputStream ostream = sock.getOutputStream();
//        PrintWriter pwrite = new PrintWriter(ostream, true);
//
//        // receiving from server ( receiveRead  object)
//        InputStream istream = sock.getInputStream();
//        BufferedReader receiveRead = new BufferedReader(new InputStreamReader(istream));
