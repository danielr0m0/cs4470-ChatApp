package Main;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class User extends Task<Void> {
    private int id=0;

    private Socket socket;
    private int port;

    public User(Socket socket, int port) {
        this.id=id++;
        this.socket = socket;
        this.port = port;
        id++;
    }

    public int getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getPort() {
        return port;
    }

    public void sendMessage(String message){
        try{
            PrintWriter out = new PrintWriter(getSocket().getOutputStream(),true);
            out.println(message);
            out.flush();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public void printMessage(){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            String message;
            while((message= reader.readLine())!=null){
                System.out.println(message);
            }


        }catch (Exception e){
            System.out.println(e);
        }
    }

    @Override
    public String toString() {
        return ("" + getId() + ": " + getSocket().getInetAddress().toString()+ ":"+ getPort());
    }

    @Override
    public void run() {
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            String message;
            while((message= reader.readLine())!=null){
                System.out.println(message);
            }


        }catch (Exception e){
            System.out.println(e);
        }
    }

    @Override
    protected Void call() throws Exception {
        return null;
    }
}
