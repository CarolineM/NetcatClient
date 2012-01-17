import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

import javax.swing.JOptionPane;


public class NCClient implements Runnable {
	
    private Socket ncClientSocket;
    private PrintWriter out;
    private BufferedReader listenIn;
    private BufferedReader stdIn;
    private String inMsg;
    private String outMsg;
    private boolean isTerminated;
    private final String closeCommand;
    Thread listenThread;
    String hostname;
    int portNum;
    
    private NCClient() {
    	JOptionPane optionsDlg = new JOptionPane();
    	hostname = optionsDlg.showInputDialog("Hostname:");
    	String portNumStr = optionsDlg.showInputDialog("Port Number:");
    	if (portNumStr == null || hostname == null || portNumStr.isEmpty() || hostname.isEmpty())
    	{
    		System.out.println("Cannot continue without hostname and port number. Exiting now.");
    		System.exit(1);
    	}
    	
    	portNum = Integer.parseInt(portNumStr);
    	
    	
        stdIn = new BufferedReader(new InputStreamReader(System.in));

        try {
            ncClientSocket = new Socket(hostname, portNum);
            out = new PrintWriter(ncClientSocket.getOutputStream(), true);
            listenIn = new BufferedReader(new InputStreamReader(ncClientSocket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "+ "the connection to: " + hostname);
            e.printStackTrace();
            System.exit(1);
        }  catch (Exception e) {
            System.err.println("Connection error");
            e.printStackTrace();
            System.exit(1);
        }
       isTerminated = false;
       closeCommand = new String("nc close");
       listenThread = new Thread(this);
       Date timestamp = new Date(System.currentTimeMillis());
       System.out.println("!!Connection established: " + timestamp);
       out.println("!!Connection established: " + timestamp);
    }
    
    private void closeConnection() {
    	try {
    		out.close();
    		listenIn.close();
    		stdIn.close();
    		ncClientSocket.close();
    	} catch (IOException e) {
            System.err.println("Issue closing connection(s)");
            e.printStackTrace();
    	}
        System.out.println("NCClient closed successfully");
    }
    

	@Override
	public void run() {
        try {
        	//TODO: have to hit enter after the connection is closed
			while ((inMsg = listenIn.readLine()) != null && isTerminated == false) {
				System.out.println("echo: " + inMsg);
			}
		} catch (IOException e) {
			System.out.println("Server connection closed");
			isTerminated = true;
			e.printStackTrace();
		}
        isTerminated = true;
        System.out.println("Server connection closed");
   }
	
	private void talk() throws IOException {
			while (isTerminated == false) {
				//TODO: have to hit enter after the connection is closed
				outMsg = stdIn.readLine();
				if (outMsg.equals(closeCommand))
				{
					isTerminated = true;
					listenThread.interrupt();
					break;
				}
				out.println(outMsg);
			}
	}
	
	public void startListenTalk() {
	       listenThread.start();
	       try {
	       this.talk();
	       }
	       catch (IOException e) {
	    	   System.err.println("Error printing to server");
	    	   e.printStackTrace();
	   	  }
	       try {
			listenThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
	}
    
	public static void main(String args[]) throws IOException {
    NCClient ncClient = new NCClient();
    ncClient.startListenTalk();
    ncClient.closeConnection();   
  }

}
