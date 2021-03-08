package finalproject.server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


import finalproject.db.DBInterface;
import finalproject.entities.Person;

public class Server extends JFrame implements Runnable {

	public static final int DEFAULT_PORT = 8001;
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 800;
	final int AREA_ROWS = 10;
	final int AREA_COLUMNS = 40;
	
	private Connection conn;
	private PreparedStatement queryStmt, insertStatement;
	private int clientNo = 0;
	JMenuBar menuBar;
	JLabel titleLabel, dbLabel;
	private JButton queryButton;
	private JPanel topPanel;
	final JTextArea textArea = new JTextArea(AREA_ROWS, AREA_COLUMNS);
	private JPanel textPanel;
	
	public Server() throws IOException, SQLException {
		this(DEFAULT_PORT, "server.db");
	}
	
	public Server(String dbFile) throws IOException, SQLException {
		this(DEFAULT_PORT, dbFile);
	}

	public Server(int port, String dbFile) throws IOException, SQLException {

		this.setSize(Server.FRAME_WIDTH, Server.FRAME_HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:server.db");
			queryStmt = conn.prepareStatement("Select * from People");
			
		} catch (SQLException e) {
			System.err.println("Connection error: " + e);
			System.exit(1);
		}
		
		
		createMenus();
		
		titleLabel = new JLabel("DB: server.db");
		titleLabel.setLocation(110,0);
        titleLabel.setSize(180, 30);
        titleLabel.setHorizontalAlignment(0);
		queryButton = new JButton("Query DB");
		queryButton.setLocation(110, 50);
		queryButton.setSize(100, 30);
		queryButton.setHorizontalAlignment(4);
		
		queryButton.addActionListener(new QueryButtonListener());
		
		topPanel = new JPanel();
        topPanel.setLayout(null);
        topPanel.setLocation(0, 0);
        topPanel.setSize(160, 30);
		topPanel.add(titleLabel);
		topPanel.add(queryButton);
		add(topPanel);
		
		textPanel= new JPanel();
		textArea.setEditable(false);
		JScrollPane listScroller = new JScrollPane(textArea);
		listScroller.setPreferredSize(new Dimension(550, 600));
		textPanel.add(listScroller);
		add(textPanel,BorderLayout.SOUTH);
		
		Thread t = new Thread(this);
	    t.start();
	}
	
	private void createMenus() {
		
		menuBar = new JMenuBar();
		
		/* add a "File" menu with:
		 * "Open" item which allows you to choose a new file
		 * "Exit" item which ends the process with System.exit(0);
		 * Key shortcuts are optional
		 */
		JMenu menu = new JMenu("File");
      	menu.add(createFileExitItem());
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
	}
	
	public JMenuItem createFileExitItem() {
		
		JMenuItem item = new JMenuItem("Exit");      
		class MenuItemListener implements ActionListener
		{
			public void actionPerformed(ActionEvent event)
			{
				System.exit(0);
				}
			}      
		item.addActionListener((e) -> System.exit(0));
		return item;
		}
	   class QueryButtonListener implements ActionListener {
		   public void actionPerformed(ActionEvent event)
	       {
			   try {
				   
//				   ResultSet rset = queryStmt.executeQuery();
//					ResultSetMetaData rsmd = rset.getMetaData();
//					int numColumns = rsmd.getColumnCount();
//					System.out.println("numcolumns is "+ numColumns);
//		
//					String rowString = "";
//					while (rset.next()) {
//						for (int i=1;i<=numColumns;i++) {
//							Object o = rset.getObject(i);
//							rowString += o.toString() + "\t";
//						}
//						rowString += "\n";
//					}
//					System.out.print("rowString  is  " + rowString);
//					textArea.append(rowString);
//					textArea.setText("");
				    PreparedStatement stmt = conn.prepareStatement("Select * from People");
					ResultSet rset = stmt.executeQuery();
					ResultSetMetaData rsmd = rset.getMetaData();
					int numColumns = rsmd.getColumnCount();
//					System.out.println("numcolumns is "+ numColumns);
					String rowString = "first"+ "\t" + "last"+ "\t"+"age"+ "\t"+ "city"+ "\t"+ "sent"+ "\t"+"id"+ "\t";
					rowString += "\n";
					rowString += "-----"+ "\t" + "----"+ "\t"+"---"+ "\t"+ "----"+ "\t"+ "----"+ "\t"+"--"+ "\t";
					rowString += "\n";
					while (rset.next()) {
						for (int i=1;i<=numColumns;i++) {
							Object o = rset.getObject(i);
							rowString += o.toString() + "\t";
						}
						rowString += "\n";
					}
					System.out.print("rowString  is  \n" + rowString);
					textArea.append("\n");
					textArea.append("DB Result:\n");
					textArea.append(rowString);
			   } 
			   catch (SQLException e) {
				   // TODO Auto-generated catch block
				   e.printStackTrace();
			   }
	       }
	   }
	   
	public static void main(String[] args) {

		Server sv;
		try {
			sv = new Server("server.db");
			sv.setVisible(true);
			sv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
	        // Create a server socket
	        ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
	        textArea.append("Server started at " + new Date() + '\n');
	        textArea.append("Listening on port " + DEFAULT_PORT);
	        while (true) {
	        	  
		        // Listen for a connection request
		        Socket socket = serverSocket.accept();
		        clientNo++;
		        textArea.append("Starting thread for client " + clientNo + " at " + new Date() + '\n');

	            // Find the client's host name, and IP address
	            InetAddress inetAddress = socket.getInetAddress();
	            textArea.append("Client " + clientNo + "'s host name is "
	              + inetAddress.getHostName() + "\n");
	            textArea.append("Client " + clientNo + "'s IP Address is "
	              + inetAddress.getHostAddress() + "\n");
	          
	          // Create and start a new thread for the connection
	            new Thread(new HandleAClient(socket, clientNo)).start();

	          }
	        
	      }
	      catch(IOException ex) {
	        ex.printStackTrace();
	      } 
	}

	  // Define the thread class for handling new connection
	  class HandleAClient implements Runnable {
	    private Socket socket; // A connected socket
	    private int clientNum;
	    
	    /** Construct a thread */
	    public HandleAClient(Socket socket, int clientNum) {
	      this.socket = socket;
	      this.clientNum = clientNum;
	      
	    }

	    /** Run a thread */
	    public void run() {
	      try {
	    	  
	    	  while (true) {
	    		  ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
		    	  PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				  Person p = (Person)is.readObject();
				  System.out.println(p.toString());
					
				  String info = "get person" + p.toString();
				  textArea.append(info);
				  textArea.append("\n");
				  String insertSQL = "Insert Into People (First, Last, Age, City, sent, ID) " + "Values (?,?,?,?,1,?)";
				  insertStatement = conn.prepareStatement(insertSQL);
				  insertStatement.setString(1, p.getFirstName());
				  insertStatement.setString(2, p.getLastName());
				  insertStatement.setString(3, ""+p.getAge());
				  insertStatement.setString(4, p.getCity());
				  insertStatement.setString(5, ""+p.getID());
				  textArea.append("Inserting into DB\n");
				  insertStatement.executeUpdate();
				  textArea.append("Inserted successfully\n");
				  out.println("Success");
				  out.flush();
	    	  }
	      }
	      catch(IOException ex) {
	        ex.printStackTrace();
	      } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    }
	  }
	
}
