package Final_Project_v2;


import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.*;

public class MultiThreadServer extends JFrame implements Runnable {
  private JTextArea ta;
  private String url = "jdbc:sqlite:javabook.db";
  private int clientNo = 0;
  HashMap<Integer, Integer> hmap;
  
  public MultiThreadServer() {
	  ta = new JTextArea(10,10);
	  JScrollPane sp = new JScrollPane(ta);
	  this.add(sp);
	  this.setTitle("MultiThreadServer");
	  this.setSize(400,200);
	  this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  this.setVisible(true);
	  Thread t = new Thread(this);
	  t.start();
  }

  public void run() {
	  try {
        ServerSocket serverSocket = new ServerSocket(8000);
        ta.append("MultiThreadServer started at " 
          + new Date() + '\n');
    
        while (true) {
          Socket socket = serverSocket.accept();
    
          clientNo++;
          
          ta.append("Starting thread for Game " + clientNo +
              " at " + new Date() + '\n');

            InetAddress inetAddress = socket.getInetAddress();
            ta.append("Game " + clientNo + "'s host name is "
              + inetAddress.getHostName() + "\n");
            ta.append("Game " + clientNo + "'s IP Address is "
              + inetAddress.getHostAddress() + "\n");
          
          new Thread(new HandleAClient(socket, clientNo)).start();
        }
      }
      catch(IOException ex) {
        System.err.println(ex);
      }
	    
  }
  
  // Define the thread class for handling new connection
  class HandleAClient implements Runnable {
    private Socket socket;
    private int clientNum;
    private PreparedStatement queryStmt;
    
    /** Construct a thread */
    public HandleAClient(Socket socket, int clientNum) {
      this.socket = socket;
      this.clientNum = clientNum;
    }

    /** Run a thread */
    public void run() {
      try {
        ObjectInputStream inputFromClient = new ObjectInputStream(
          socket.getInputStream());
        ObjectOutputStream outputToClient = new ObjectOutputStream(
          socket.getOutputStream());

        // Continuously serve the client
        while (true) {
          int state = inputFromClient.readInt();
          int timeLeft;
          int numOfMinesLeft;
          boolean gameStatus;
          int [] b;
          int searchId;

          try {
        	Connection connect = DriverManager.getConnection(url);
        	if(state == 100) {
        		System.out.println("Save State");
        		timeLeft = inputFromClient.readInt();
                numOfMinesLeft = inputFromClient.readInt();
                gameStatus = inputFromClient.readBoolean();
                
                queryStmt = connect.prepareStatement("INSERT INTO Player (timeLeft, boardState, minesLeft, gameStatus) VALUES (?,?,?,?)");
            	queryStmt.setInt(1, timeLeft);
            	queryStmt.setBytes(2, getBytes(inputFromClient.readObject()));
            	queryStmt.setInt(3, numOfMinesLeft);
            	queryStmt.setBoolean(4, gameStatus);
            	queryStmt.executeUpdate();
            	queryStmt.close();
            	ta.append("Game "+this.clientNum+"\'  has been stored in the database"); 
        	}
        	else if(state == 101){
        		searchId = inputFromClient.readInt();

        		queryStmt = connect.prepareStatement("SELECT * FROM Player WHERE playerId=?");
        		queryStmt.setInt(1, searchId);
        		ResultSet rset = queryStmt.executeQuery();
        		while(rset.next()) {
        			outputToClient.writeInt(rset.getInt("playerId"));
        			outputToClient.writeInt(rset.getInt("timeLeft"));
        			outputToClient.writeObject(getObject(rset.getBytes("boardState")));
        			outputToClient.writeInt(rset.getInt("minesLeft"));
        			outputToClient.writeBoolean(rset.getBoolean("gameStatus"));
        			outputToClient.flush();
        		}
        		queryStmt.close();
        		ta.append("Game "+this.clientNum+"\' has been restored"); 
        	}
        	else if(state == 102) {
        		hmap = new HashMap<>();
        		queryStmt = connect.prepareStatement("SELECT * FROM Player where gameStatus=true");
        		ResultSet rset = queryStmt.executeQuery();
        		while(rset.next()) {
        			hmap.put(rset.getInt("playerId"), rset.getInt("timeLeft"));
        		}
        		Map<Integer, Integer> mp = sortByValue(hmap);
        		outputToClient.writeObject(mp);
        		outputToClient.flush();
        		queryStmt.close();
        		ta.append("Top 5 Players"); 
        	}
          }
          catch(Exception ex) {
        	  System.out.println(ex);
          }
                   
        }
      }
      catch(Exception ex) {
    	System.out.println("SERVER:C");
        ex.printStackTrace();
      }
    }
  }
  
  public static byte[] getBytes(Object obj) throws java.io.IOException {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(bos);
	    oos.writeObject(obj);
	    oos.flush();
	    oos.close();
	    bos.close();
	    byte[] data = bos.toByteArray();
	    return data;
  }
  
  public static Object getObject(byte[] bytearr) throws IOException, ClassNotFoundException{
	  ByteArrayInputStream bis = new ByteArrayInputStream(bytearr);
	  ObjectInputStream ois = new ObjectInputStream(bis);
	  return ois.readObject();
  }
  
  public static Map<Integer, Integer> sortByValue(HashMap<Integer, Integer> hmap)   
  {    
  	List<Entry<Integer, Integer>> list = new LinkedList<Entry<Integer, Integer>>(hmap.entrySet());   
  	Collections.sort(list, new Comparator<Entry<Integer, Integer>>()   
  	{  
  		public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2)   
  		{  
  			if (false)   
  				return o1.getValue().compareTo(o2.getValue());
   
  			else     
  				return o2.getValue().compareTo(o1.getValue());   
  		}
  	
  	});
  	
  	//prints the sorted HashMap  
  	Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
  	int count = 0;
  	for (Entry<Integer, Integer> entry : list)   
  	{  
  		if(count!=5) {
  			sortedMap.put(entry.getKey(), entry.getValue());
  			count++;
  		}
  		else
  			break;
  	}  
  	//printMap(sortedMap); 
  	return sortedMap;
  } 
  
  public static void main(String[] args) {
    MultiThreadServer mts = new MultiThreadServer();
  }
}