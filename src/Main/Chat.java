package Main;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class Chat {

    public static HashMap<Integer,User> servers;
    public static HashMap<Integer,Integer> costTable;

    public static int[] neighbors;
    public static int numOfEdges = 0;
    public static int[][] totalCostTable;

    static boolean exit = false;
    static ArrayList<User> users;
    static int serverSize = 0;
    static int ID= 0;
    static int received =0;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String args[]) throws Exception {
        users = new ArrayList<>();


        neighbors = new int[4];
        totalCostTable= new int[4][4];
        servers = new HashMap<>();
        costTable = new HashMap<>();

        for (int i = 0; i < totalCostTable.length; i++) {
            for (int j = 0; j < totalCostTable.length; j++) {
                totalCostTable[i][j]= Integer.MAX_VALUE;
            }
        }


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
                        String message = home.getMessage();

                        if(message.contains("from")){
                            home.printMessage();
                        }else if(!message.isEmpty()){
                            System.out.println("data sent");
                            parseData(message);
                            System.out.println(message);
                            bellman_ford(totalCostTable);
                        }
                        received++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("closing the server");

                    } finally {
                        users.remove(home);
                        try {
                            connection.close();
                        } catch (IOException e) {
                            System.out.println("closed");
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
                String[] inputs = input.split("\\s+");

                if (input.equals("0") || input.toLowerCase().equals("exit")) {
                    updateList();
                    for (User user : users) {
                        user.sendMessage(InetAddress.getLocalHost().getHostAddress() + " is closing connection");
                        user.getSocket().close();
                    }
                    sersock.close();
                    exit = true;
                    System.exit(0);
                    break;
                } else if (inputs[0].toLowerCase().contains("ip")) {
                    try {
                        System.out.println(InetAddress.getLocalHost().getHostAddress());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                } else if (inputs[0].toLowerCase().contains("port")) {
                    System.out.println(portNumber);
                } else if (inputs[0].toLowerCase().contains("help")) {

                    System.out.println("myip\t Displays the IP Address of This Process");
                    System.out.println("myport\t Displays the Port on Which this Process is Listening for Incoming Connections");
                    System.out.println("list\t Displays a list of all connections with Connection IDs");
                    System.out.println("connect <destination> <port no>\t Establishes a new connection to specified IP Address at specified port number");
                    System.out.println("terminate <connection ID>\t Terminates the connection listed under the specified Connection ID");
                    System.out.println("send <connection ID> <message>\t Sends the desired message to the Connection listed under the specified Connection ID");
                    System.out.println("exit\t Closes all connections and Terminates this Process");

                } else if (inputs[0].toLowerCase().contains("connect")) {
                    updateList();
                    inputs = input.toLowerCase().split("\\s+");

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

                } else if (inputs[0].toLowerCase().contains("terminate")) {
                    inputs = input.toLowerCase().split("\\s+");
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
                        System.out.println("type list to get the correct ID");
                    }


                } else if (inputs[0].toLowerCase().contains("send")) {//send message to user with ID given
                    updateList();
                    inputs = input.toLowerCase().split("\\s+");

                    String message = " ";
                    int userId = Integer.parseInt(inputs[1]) - 1;
                    if (userId < users.size() || userId > 0) {
                        User user = users.get(userId);

                        //check if user is connected
                        if (user.getSocket() != null) {
                            for (int i = 2; i < inputs.length; i++) {
                                message += inputs[i] + " ";
                            }
                            user.sendMessage("\nfrom: " + InetAddress.getLocalHost().getHostAddress() + "\nmessage: " + message + "\n");
                        } else {
                            System.out.println("type list to get the correct ID");
                        }
                    } else {
                        System.out.println("Type list to see who you are connected");
                    }

                } else if (inputs[0].toLowerCase().contains("list")) {
                    updateList();
                    if (users.size() == 0) {
                        System.out.println("List is empty. Connect to someone");
                    } else {
                        System.out.println("ID: \t IP Address \t\t Port Number");
                        int id = 1;
                        for (User user : users) {
                            System.out.println(id + "\t " + user.getSocket().getInetAddress() + "\t \t " + user.getPort());
                            id++;
                        }
                    }

                } else if (inputs[0].toLowerCase().contains("test")) {
                    for(Integer id : servers.keySet()){
                        System.out.println(servers.get(id));
                    }



                }
                //TODO EVERYTHING FOR PROJECT 2

                else if (inputs[0].toLowerCase().contains("server")) {

                    String[] parsed = input.split(" ");
                    if (parsed.length == 5) {
                        int tIndex = 0;
                        int iIndex = 0;
                        for (int i = 0; i < parsed.length; i++) {
                            if (parsed[i].equals("-t")) {
                                tIndex = i;
                            } else if (parsed[i].equals("-i")) {
                                iIndex = i;
                            }
                        }
                        String topologyFilePath = parsed[tIndex + 1];
                        //interval is in seconds
                        int interval = Integer.parseInt(parsed[iIndex + 1]) * 1000;
                        File topology = new File(topologyFilePath);


                        if (topology.exists()) {

                            readFile(topology);

                            Task<Void> time = new Task<Void>() {
                                @Override
                                public void run() {
                                    try {
                                        //update send data to neighbors
                                        while(true){
                                            for (int i = 0; i < neighbors.length; i++) {
                                                String results="";
                                                for(Integer id : costTable.keySet()){
                                                    results+=ID+" "+id+" "+costTable.get(id)+";";
                                                    System.out.println(results);
                                                }
                                                System.out.println(results);
                                                servers.get(ID).sendMessage(results);
                                           }
                                            Thread.sleep(interval);
                                        }



                                    } catch (Exception e) {
                                        System.out.println("error ...\n make sure u spelled the file name correctly and have the correct values in files");
                                    }
                                }

                                @Override
                                protected Void call() throws Exception {
                                    return null;
                                }
                            };

                            Thread timeThread = new Thread(time);
                            timeThread.start();

//                            Timer timer = new Timer();
//                            timer.schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                    try {
//                                        //update send data to neighbors
//                                        for (int i = 0; i < neighbors.length; i++) {
//                                            servers.get(ID).sendMessage(stringData());
//                                        }
//
//                                    } catch (Exception e) {
//                                        System.out.println("error ...\n make sure u spelled the file name correctly and have the correct values in files");
//                                        timer.cancel();
//                                    }
//                                }
//                            }, 0, interval);

                        } else {
                            System.out.println("file doesnt exists");
                        }
                    }

                } else if (inputs[0].toLowerCase().contains("update")) {
                    /**TODO update the cost between two servers
                     * arg 1 server ID 1
                     * arg 2 server ID 2
                     * arg 3 cost
                     */
                    if(inputs.length==4){
                        try{
                            int id1= Integer.parseInt(inputs[1]);
                            int id2 = Integer.parseInt(inputs[2]);
                            int cost = 0;
                            if(inputs[3].contains("inf")){
                                cost = Integer.MAX_VALUE;
                            }

                            cost = Integer.parseInt(inputs[3]);

                            if(cost == -1){
                                cost = Integer.MAX_VALUE;
                            }
                            totalCostTable[id1][id2]= cost;
                            if(id1== ID){
                                costTable.replace(id2,cost);
                            }

                            //update send data to neighbors
                            for (int i = 0; i < neighbors.length; i++) {
                                servers.get(ID).sendMessage(stringData());
                            }

                        }catch (Exception e){
                            System.out.println("error please enter correct values enter -1 for infinity");
                        }
                    }

                } else if (input.toLowerCase().contains("step")) {
                    /**TODO step Send routing update to neighbors right away. Note that except this, routing updates only
                     * happen periodically
                     */
                } else if (inputs[0].toLowerCase().contains("packets")) {
                    /**TODO packets Display the number of distance vector packets this server has received since the last
                     * invocation of this information.
                     */
                    System.out.println(received);
                } else if (inputs[0].toLowerCase().contains("display")) {
                    /**TODO Display the current routing table. And the table should be displayed in a sorted order from
                     */
                    print(totalCostTable);
                } else if (inputs[0].toLowerCase().contains("disable")) {
                    /**TODO arg1 server ID to disable the link
                     */
                } else if (inputs[0].toLowerCase().contains("crash")) {
                    /**TODO “Close” all connections
                     *  set the link cost to infinity
                     */
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

    public static int[] bellman_ford(int[][] graph, int source){
        int[] d= new int[graph.length];

        for (int i = 0; i < graph.length; i++) {
            if(graph[source][i] == 0){
                graph[source][i]=Integer.MAX_VALUE;
            }
            d[i]= graph[source][i];
        }

        d[source]=0;

        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph[i].length; j++) {
                    if(d[j]> d[i] + graph[i][j])
                        d[j]=d[i]+graph[i][j];

            }
        }

        return d;

    }

    public static void bellman_ford(int[][] graph){
        int[][] dv= new int[graph.length][graph.length];

        for (int i = 0; i < graph.length; i++) {
            dv[i]=bellman_ford(graph,i);
        }

        totalCostTable=dv;


    }


    //end line with;
    public static String stringData(){

        return "";
    }

    public static void parseData(String s){
        String[] lines = s.split(";");
        for (String data : lines) {
            String[] info = data.split(" ");
            totalCostTable[Integer.parseInt(info[0])][Integer.parseInt(info[1])]= Integer.parseInt(info[0]);
        }
    }



    public static void print(int[][] a){
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                if(i!=j)
                    System.out.println(""+(i+1) + " "+(j+1)+" " +a[i][j]);
            }
        }
    }

    public static void readFile(File file){
        Scanner scanner = null;
        try {
            scanner = new Scanner(file.toPath());
        } catch (IOException e) {
            System.out.println("file not found... check your spelling and location of file");
            e.printStackTrace();
        }

        String line = scanner.nextLine();


        String ip ="";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }



        int serverSize = Integer.parseInt(line);

        line = scanner.nextLine();

        int numOfEdges = Integer.parseInt(line);


        //ID ipAddress port
        for (int i = 0; i < serverSize; i++) {
            line = scanner.nextLine();
            String[] split = line.split("\\s+");
            int id = Integer.parseInt(line.split("\\s+")[0]);
            Socket socket = null;
            if((line.split("\\s+")[1]).contains(ip)){
                ID = id;
            }

            try {
                socket = new Socket(split[1],Integer.parseInt(split[2]));
            } catch (IOException e) {
                e.printStackTrace();
            }
            User u = new User(socket,Integer.parseInt(split[2]));
            u.setId(id);
            servers.put(id,u);

            Thread uThread = new Thread(u);
            uThread.start();
        }

        for (int i = 0; i < numOfEdges; i++) {
            line = scanner.nextLine();

            String[] links = line.split("\\s+");
            int x = Integer.parseInt(links[0]) - 1;
            int y = Integer.parseInt(links[1]) - 1;
            int cost = Integer.parseInt(links[2]);
            neighbors[i]=y;
            totalCostTable[x][y] = cost;
            costTable.put(y,cost);
        }
    }

}

