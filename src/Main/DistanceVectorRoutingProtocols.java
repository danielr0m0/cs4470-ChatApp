package Main;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class DistanceVectorRoutingProtocols {

    public static HashMap<Integer,Server> servers;
    public static int[] neighbors;
    public static int ID =0;
    public static int received=0;
    public static int[][] totalCostTable;
    //ID,cost
    public static HashMap<Integer,Integer> costTable;
    public static boolean exit = false;
    public static  DatagramSocket ds;


    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws UnknownHostException {
        neighbors = new int[4];
        totalCostTable= new int[4][4];
        servers = new HashMap<>();
        costTable = new HashMap<>();
        ds=null;

        int portNumber = Integer.parseInt(args[0]);
        String ip = InetAddress.getLocalHost().getHostAddress();


        Task<Void> serverTask = new Task<Void>(){
            @Override
            public void run() {

                    try {
                        ds = new DatagramSocket(portNumber, InetAddress.getLocalHost());
                        //data received
                        while(true){
                            byte[] buf= new byte[1024];
                            DatagramPacket dp = new DatagramPacket(buf,1024);
                            ds.receive(dp);

                            String str= new String(dp.getData(),0,dp.getLength());
                            System.out.println(str);
                            parseData(str);
                            received++;
                        }

                    } catch (SocketException | UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
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

        while (!exit){
            if(scanner.hasNext()){
                String input = scanner.nextLine();
                String[] inputs = input.split("\\s+");
                if (inputs[0].toLowerCase().contains("ip")) {
                        System.out.println(ip);
                } else if (inputs[0].toLowerCase().contains("port")) {
                    System.out.println(portNumber);
                }
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

                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        //update send data to neighbors
                                        String s = stringData();
                                        for (int i = 0; i < neighbors.length; i++) {
                                            DatagramSocket ds=null;
                                            try {
                                                ds = new DatagramSocket();
                                                DatagramPacket dp = new DatagramPacket(s.getBytes(), s.length(),InetAddress.getByName(servers.get(neighbors[i]).getIp()),servers.get(neighbors[i]).getPort());
                                                ds.send(dp);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            ds.close();
//                                            send(stringData(),servers.get(neighbors[i]).getIp(),servers.get(neighbors[i]).getPort());
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        System.out.println("error ...\n make sure u spelled the file name correctly and have the correct values in files");
                                        timer.cancel();
                                    }
                                }
                            }, interval, interval);

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
                                send(stringData(),servers.get(neighbors[i]).getIp(),servers.get(neighbors[i]).getPort());
                            }

                        }catch (Exception e){
                            System.out.println("error please enter correct values enter -1 for infinity");
                        }
                    }

                } else if (inputs[0].toLowerCase().contains("step")) {
                    /**TODO step Send routing update to neighbors right away. Note that except this, routing updates only
                     * happen periodically
                     */
                    //update send data to neighbors
                    for (int i = 0; i < neighbors.length; i++) {
                        send(stringData(),servers.get(neighbors[i]).getIp(),servers.get(neighbors[i]).getPort());
                    }
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
                    /**TODO all connections
                     *  set the link cost to infinity
                     */
                    ds.close();
                } else if(input.contains("test")){
                    readFile(new File("topology.txt"));
                    System.out.println(stringData());
                }
                else {
                    System.out.println("type 'help' for assistance");
                }

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

            if((line.split("\\s+")[1]).contains(ip)){
                ID = Integer.parseInt(line.split("\\s+")[0]);
            }


            Server server = new Server(Integer.parseInt(split[0]), split[1],Integer.parseInt(split[2]));

            servers.put(server.getId(), server);
        }

        for (int i = 0; i < numOfEdges; i++) {
            line = scanner.nextLine();
            String[] links = line.split("\\s+");
            int x = Integer.parseInt(links[0]);
            int y = Integer.parseInt(links[1]);
            int cost = Integer.parseInt(links[2]);
            neighbors[i]=y;
            totalCostTable[x][y] = cost;
            costTable.put(y,cost);

            System.out.println(x+" "+ y+" "+cost);
        }

    }

    //end line with;
    public static String stringData(){
        String results="";
        for(Integer id : costTable.keySet()){
            results+=ID+" "+id+" "+costTable.get(id)+";";
        }
        return results;
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
                    System.out.println(""+i + " "+j+" " +a[i][j]);
            }
        }
    }
    public static void print(int[] a){
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]+ " ");
        }
        System.out.println();
    }

    public static void send(String s, String ip, int port){
        DatagramSocket ds=null;
        try {
            ds = new DatagramSocket();
            DatagramPacket dp = new DatagramPacket(s.getBytes(), s.length(),InetAddress.getByName(ip),port);
            ds.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ds.close();
    }
    public static int[] bellman_ford(int[][] graph, int source){
        int[] d= new int[graph.length];
        int[] p = new int[graph.length];

        for (int i = 0; i < graph.length; i++) {
            d[i]= graph[source][i];
            p[i]=0;
        }

        d[source]=0;

        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph[i].length; j++) {
                if(d[i]!=Integer.MAX_VALUE && graph[i][j]!=Integer.MAX_VALUE){
                    if(d[j]> d[i] + graph[i][j])
                        d[j]=d[i]+graph[i][j];
                }

            }
        }

        return d;

    }

    public static void bellman_ford(int[][] graph){
        int[][] dv= new int[graph.length][graph.length];

        for (int i = 0; i < graph.length; i++) {
            dv[i]=bellman_ford(graph,i);
        }

        print(dv);

    }

    //use dictionary  i think hash map for java
    public static int[] dijkstra(int[][] graph, int source){
        int[] d= new int[graph.length];
        boolean[] visited = new boolean[graph.length];

        for (int i = 0; i < graph.length; i++) {
            d[i]=Integer.MAX_VALUE;
            visited[i]=false;
        }

        d[source]=0;
        //find the shortest path for all vertices
        for (int i = 0; i < graph.length - 1; i++) {
            int min = findMinIdex(d,visited);
            visited[min]=true;

            for (int j = 0; j < graph.length; j++) {
                if (!visited[j] && graph[min][j]!=0 && d[min] != Integer.MAX_VALUE && d[min]+graph[min][j] < d[j]){
                    d[j]= d[min]+graph[min][j];
                }
            }

        }
        return d;
    }

    public static void dijkstra(int[][]graph){
        for (int i = 0; i < graph.length; i++) {
            print(dijkstra(graph,i));
        }
    }

    public static int findMinIdex(int[] dist, boolean[] visited){
        int min = Integer.MAX_VALUE;
        int index= 0;
        for (int i = 0; i < dist.length; i++) {
            if(min>=dist[i] && visited[i]==false){
                min=dist[i];
                index=i;
            }
        }
        return index;
    }

    public static class Server{
        private int id;
        private String ip;
        private int port;

        public Server(int id, String ip, int port) {
            this.id = id;
            this.ip = ip;
            this.port = port;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
