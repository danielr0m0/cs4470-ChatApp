package Main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.concurrent.Task;

public class User extends Task<Void> {
	private Socket socket;
    private int port;

    public User(Socket socket, int port) {
        this.socket = socket;
        this.port = port;
        
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
    
    public boolean isEquals(User u) {
    
    		if(socket.getLocalAddress().equals(u.getSocket().getLocalAddress())&& port == u.getPort()){
    			return true;		
    		}
    	return false;
    }

    @Override
    public String toString() {
        return (getSocket().getInetAddress().toString()+ "\t\t"+ getPort());
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
        finally {
        	socket = null;
        }
    }
    

	@Override
    protected Void call() throws Exception {
        return null;
    }
}
