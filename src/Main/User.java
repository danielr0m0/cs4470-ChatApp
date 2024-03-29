package Main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.concurrent.Task;

public class User extends Task<Void> {
    private Socket socket;
    private int port;
    private int id;
    private String txt="";
    public User(Socket socket, int port) {
        this.socket = socket;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getTxt(){return  txt;}


    public Socket getSocket() {
        return socket;
    }

    public int getPort() {
        return port;
    }

    public void sendMessage(String message) {
        System.out.println(message);
        try {
            PrintWriter out = new PrintWriter(getSocket().getOutputStream(), true);
            out.println(message);
            out.flush();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public void sendCost(int[] cost){

    }

    //if i dont have print message it will not print message when i send
    public void printMessage() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            String message;
            while ((message = reader.readLine()) != null) {
                txt=message;
                System.out.println(message);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }



    public boolean isEquals(User u) {
        String ip1 = socket.getLocalAddress().toString();
        ip1 = ip1.replace("/", "");
        ip1 = ip1.replace(".", "");

        String ip2 = u.getSocket().getLocalAddress().toString();
        ip2 = ip2.replace("/", "");
        ip2 = ip2.replace(".", "");


        if (Integer.parseInt(ip1) == Integer.parseInt(ip2) && port == u.getPort()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return (getSocket().getInetAddress().toString() + "\t\t" + getPort());
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            String message;
            while ((message = reader.readLine()) != null) {
                txt=message;
                System.out.println(message);
            }


        } catch (Exception e) {
            System.out.println("socket has been closed");
        } finally {
            socket = null;
        }
    }


    @Override
    protected Void call() throws Exception {
        return null;
    }
}
