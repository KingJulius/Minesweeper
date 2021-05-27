package Final_Project_v2;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;



public class Client extends JFrame {
  ObjectOutputStream toServer = null;
  ObjectInputStream fromServer = null;
  JTextArea textArea = null;
  Socket socket = null;
  JMenuBar menubar;
  JMenu menu;
  JMenuItem i1, i2, i3, i4, i0;
  Board b;
  private JLabel status;
  JLabel l1;
  public  int i = 1000;
  private final int SAVE_STATE = 100;
  private final int LOAD_STATE = 101;
  private final int TOP_FIVE_STATE =102;
  Timer timer;
  TimerTask task;
  
  public Client() {  
	  menubar = new JMenuBar();
	  menu = new JMenu("File");
	  i0 = new JMenuItem("Top 5 Scores");
	  i1 = new JMenuItem("New");
	  i2 = new JMenuItem("Open");
	  i3 = new JMenuItem("Save");
	  i4 = new JMenuItem("Exit");
	  menu.add(i1);
	  menu.add(i2);
	  menu.add(i3);
	  menu.add(i0);
	  menu.add(i4);
	  menubar.add(menu);
	  this.setJMenuBar(menubar);
	  
	  textArea = new JTextArea(1,1);
	  
	  JPanel topPanel = new JPanel(new GridLayout(1,1));
	  topPanel.add(l1 =new JLabel("Timer"));
	  
	  status = new JLabel("");
	  JPanel bottomPanel = new JPanel(new GridLayout(2,1));
	  bottomPanel.add(status);
	  bottomPanel.add(textArea);
	  
	  this.setLayout(new BorderLayout());
	  this.add(topPanel, BorderLayout.NORTH);
	  this.add(b=new Board(status), BorderLayout.CENTER);
	  this.add(bottomPanel, BorderLayout.SOUTH);
	  this.setSize(255, 800);
	  this.setTitle("Minesweeper");
	  this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.setVisible(true);
      this.setResizable(false);
      this.pack();
	  
      timer = new Timer();
      task = new Helper();
      timer.schedule(task, 0, i);
      
      
      i0.addActionListener(new TopFiveItemListener());
      i1.addActionListener(new NewItemListener());
      i2.addActionListener(new OpenItemListener());
	  i3.addActionListener(new SaveItemListener());
	  i4.addActionListener((e) -> { try { socket.close(); textArea.setText("connection closed");} catch (Exception e1) {System.err.println("error"); }});
  }
 
  class Helper extends TimerTask{
      public void run()
      {
      	int a = i--;
      	l1.setText("Time Remaining: "+String.valueOf(a));
          if(a == 0 | b.getGameStatus()==false) {
          	status.setText("Game Over!");
          	this.cancel();
          	//repaint();
          	//return;
          }
          
      }
  }
  
 class OpenItemListener implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		JFrame f=new JFrame();
		try {
			String id_String = JOptionPane.showInputDialog(f,"Enter Used ID: ");
		    int id = Integer.parseInt(id_String);
		    try {
				socket = new Socket("localhost", 8000);
				textArea.setText("Game Data Loaded");
				toServer = new ObjectOutputStream(socket.getOutputStream());
				toServer.writeInt(LOAD_STATE);
				toServer.writeInt(id);
				toServer.flush();
				
				
				
				fromServer = new ObjectInputStream(socket.getInputStream());
				System.out.println(fromServer.readInt()+" PLayer ID"); // Player Id
				timer.cancel();
				i=fromServer.readInt();
				timer = new Timer();
			    task = new Helper();
			    timer.schedule(task, 0, i);
				System.out.println(i); // Time
				
				int [] test = (int [])fromServer.readObject();
				b.setField(test); // FIELD SET
				
				int temp1 = fromServer.readInt();
				b.setNumberOfMines(temp1);
				b.status.setText(Integer.toString(temp1));
				System.out.println(temp1+" MINES LEFT"); // MINES LEFT
				
				boolean bVal = fromServer.readBoolean();
				b.setGameStatus(bVal);				
				System.out.println(bVal+" Game State"); // GAME STATE				

				
				b.repaint();
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				textArea.setText("connection Failure");
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	 
 }
 class SaveItemListener implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		try {
			socket = new Socket("localhost", 8000);
			textArea.setText("Saved Game Data");
			toServer = new ObjectOutputStream(socket.getOutputStream());
			toServer.writeInt(SAVE_STATE);
			toServer.writeInt(i);
			toServer.writeInt(b.getNumberOfMines());
			toServer.writeBoolean(b.getGameStatus());
			toServer.writeObject(b.getField());
	        toServer.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			textArea.setText("connection Failure");
		}
	}
	 
  }
  
 class NewItemListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			i = 1000;
			timer = new Timer();
		    task = new Helper();
		    timer.schedule(task, 0, i);
			status.setText("New Game Created");
			b.setNumberOfMines(40);
			b.setGameStatus(true);
			b.newGame();
			b.repaint();
		}
		 
 }
 
 class TopFiveItemListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			try {
				socket = new Socket("localhost", 8000);
				textArea.setText("TOP FIVE");
				toServer = new ObjectOutputStream(socket.getOutputStream());
				toServer.writeInt(TOP_FIVE_STATE);
				toServer.flush();
				
				String msg = "Top 5 Users are:\n";
				fromServer = new ObjectInputStream(socket.getInputStream());
				Map<Integer, Integer> hmap = (Map<Integer, Integer>) fromServer.readObject();
				Set set = hmap.entrySet();
			    Iterator iterator = set.iterator();
			    while(iterator.hasNext()) {
			         Map.Entry mentry = (Map.Entry)iterator.next();
			         msg+=(mentry.getKey().toString()+"-->"+mentry.getValue()+"\n");
			    }
				JOptionPane.showMessageDialog(new JFrame(), msg);
			}
			catch(IOException | ClassNotFoundException ex) {
				
			}
			
		}
		 
	  }
 
  public static void main(String[] args) {
    Client c = new Client();
  }
}
