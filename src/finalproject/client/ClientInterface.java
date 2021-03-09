package finalproject.client;

import java.util.ArrayList;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;


import finalproject.client.ClientInterface.ComboBoxItem;
import finalproject.db.DBInterface;
import finalproject.entities.Person;
import finalproject.server.Server;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientInterface extends JFrame {

	private static final long serialVersionUID = 1L;

	public static final int DEFAULT_PORT = 8001;
	
	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 400;
	final int AREA_ROWS = 10;
	final int AREA_COLUMNS = 40;

	private Connection conn;
	JMenuBar menuBar;
	
	JLabel dbtitleLabel, dbName;
	JLabel conLabel, statusLabel;
	
	JButton openButton, closeButton, sendButton, queryButton;
	
	JComboBox peopleSelect;
	JFileChooser jFileChooser;
	Socket socket;
	int port;
	DataOutputStream toServer = null;
	private JPanel topPanel;
	final JTextArea textArea = new JTextArea(AREA_ROWS, AREA_COLUMNS);
	private JPanel textPanel;
	
	
	public ClientInterface() {
		this(DEFAULT_PORT);
		
	}
	
	public ClientInterface(int port) {
		this.port = port;
		this.setSize(ClientInterface.FRAME_WIDTH, ClientInterface.FRAME_HEIGHT);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		createMenus();
		jFileChooser = new JFileChooser();
		
		
		JPanel statusPanel = new JPanel();
		JPanel statusPanel2 = new JPanel();
		dbtitleLabel = new JLabel("Active DB: ");
		dbName = new JLabel("<None>");
		conLabel = new JLabel("Active Connection: ");
		statusLabel = new JLabel("<None>");		
		statusPanel.add(dbtitleLabel);
		statusPanel.add(dbName);
		statusPanel2.add(conLabel);
		statusPanel2.add(statusLabel);
		topPanel = new JPanel(new GridLayout(5,1));
		JPanel controlPanel = new JPanel();
		JPanel controlPanel2 = new JPanel();
		openButton = new JButton("Open Connection");
	    closeButton = new JButton("Close Connection");
	    sendButton = new JButton("Send Data");
	    queryButton = new JButton("Query DB Data");
        controlPanel.add(openButton);
	    controlPanel.add(closeButton);
	    controlPanel2.add(sendButton);
	    controlPanel2.add(queryButton);
	    String[] boxOptions = { "<Empty>"};
	    peopleSelect = new JComboBox(boxOptions);
	    topPanel.add(statusPanel);
	    topPanel.add(statusPanel2);
	    topPanel.add(peopleSelect);
	    topPanel.add(controlPanel);
	    topPanel.add(controlPanel2);
		this.add(topPanel, BorderLayout.NORTH);
		textPanel = new JPanel();
		textPanel.add(textArea);
		this.add(textPanel, BorderLayout.CENTER);
		
		closeButton.addActionListener((e) -> { try { socket.close();statusLabel.setText("<None>");textArea.append("Connection closed");} catch (Exception e1) {System.err.println("error"); }});
		openButton.addActionListener(new OpenConnectionListener());
		queryButton.addActionListener(new queryButtonListener());
		sendButton.addActionListener(new SendButtonListener());
	}
	

   public JMenu createFileMenu()
   {
      JMenu menu = new JMenu("File");
      menu.add(createFileOpenItem());
      menu.add(createFileExitItem());
      return menu;
   }
   
   private void createMenus() {
		menuBar = new JMenuBar();
		JMenu menu = createFileMenu();
		menuBar.add(menu);
		this.setJMenuBar(menuBar);
	}
   
   
   private void fillComboBox() throws SQLException {
	   
	   List<ComboBoxItem> l = getNames();
	   peopleSelect.setModel(new DefaultComboBoxModel(l.toArray()));
		
	   
   }
   
   private void clearComboBox() {
	   String[] boxOptions = {"<Empty>"};
	   peopleSelect = new JComboBox(boxOptions);
   }
   
   private void connectToDB(String fileName) {
	   try {
			conn = DriverManager.getConnection("jdbc:sqlite:" + fileName);
			dbName.setText(fileName);
			
		} catch (SQLException e) {
			System.err.println("Connection error: " + e);
			System.exit(1);
		}

   }
   
   private JMenuItem createFileOpenItem() {
	   JMenuItem item = new JMenuItem("Open DB");
	   class OpenDBListener implements ActionListener
	      {
	         public void actionPerformed(ActionEvent event)
	         {
	 			int returnVal = jFileChooser.showOpenDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					System.out.println("You chose to open this file: " + jFileChooser.getSelectedFile().getAbsolutePath());
					String dbFileName = jFileChooser.getSelectedFile().getAbsolutePath();
					try {
//						connect to the DB 
						connectToDB(dbFileName);
//						indicating the name of the Active DB
						dbName.setText(dbFileName.substring(dbFileName.lastIndexOf("/")+1));
//						queryButtonListener.setConnection(conn);
//						filling the contents of the dropdown box listing names 
						fillComboBox();
						
					} catch (Exception e ) {
						System.err.println("error connection to db: "+ e.getMessage());
						e.printStackTrace();
						dbName.setText("<None>");
						clearComboBox();
					}
					
				}
	         }
	      }
	   
	   item.addActionListener(new OpenDBListener());
	   return item;
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
	
	class queryButtonListener implements ActionListener {
	   public void actionPerformed(ActionEvent event)
       {
		   try {
			   PreparedStatement stmt = conn.prepareStatement("Select * from People");
				ResultSet rset = stmt.executeQuery();
				ResultSetMetaData rsmd = rset.getMetaData();
				int numColumns = rsmd.getColumnCount();
				System.out.println("numcolumns is "+ numColumns);
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
				System.out.print("rowString  is  " + rowString);
				textArea.setText(rowString);
		   } catch (SQLException e) {
			   e.printStackTrace();
		   }
       }
   }
	class SendButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {

	        try {
				
				
				
				// now, get the person on the object dropdownbox we've selected
				ComboBoxItem personEntry = (ComboBoxItem)peopleSelect.getSelectedItem();
				
				// the personEntry
				// contains an ID and a name. You want to get a "Person" object out of that
				// which is stored in the database
				PreparedStatement stmt = conn.prepareStatement("Select * from People WHERE id = ?");
				String temp = "" + personEntry.getId();
				stmt.setString(1, temp);
				ResultSet rset = stmt.executeQuery();
				Object o = rset.getObject(1);
				int id = rset.getInt("id");
				String firstName = rset.getString("first");
				String lastName = rset.getString("last");
				String city = rset.getString("city");
				int age = rset.getInt("age");
				toServer = new DataOutputStream(socket.getOutputStream());
				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
			    Person object1 = new Person(firstName, lastName, age, city, id);
				os.writeObject(object1);
				// Send the person object here over an output stream that you got from the socket.
				
				System.out.println("1");
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				System.out.println("2");
				String response = br.readLine();
				System.out.println("3");
				if (response.contains("Success")) {
					System.out.println("Success");
					PreparedStatement stmt2 = conn.prepareStatement("UPDATE People SET sent=1 WHERE id = ?");
					stmt2.setString(1, temp);
					stmt2.executeUpdate();
					List<ComboBoxItem> l = getNames();
				    peopleSelect.setModel(new DefaultComboBoxModel(l.toArray()));
				    
				} else {
					System.out.println("Failed");
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
	        
			
		}
		
	}
	
   private List<ComboBoxItem> getNames() throws SQLException {
	   List<ComboBoxItem> result = new ArrayList<ComboBoxItem>(); 
	   PreparedStatement stmt = conn.prepareStatement("Select * from People");
	   ResultSet rset = stmt.executeQuery();
	   while (rset.next()) {
		   int id = rset.getInt("id");
		   String firstName = rset.getString("first");
		   String lastName = rset.getString("last");
		   ComboBoxItem temp = new ComboBoxItem(id, firstName + " " + lastName);
		   int sent = rset.getInt("sent");
		   if(sent == 0) {
			   result.add(temp);   
		   }
		   
	   }
	   return result;
   }
	
	// a JComboBox will take a bunch of objects and use the "toString()" method
	// of those objects to print out what's in there. 
	class ComboBoxItem {
		private int id;
		private String name;
		
		public ComboBoxItem(int id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public int getId() {
			return this.id;
		}
		
		public String getName() {
			return this.name;
		}
		
		public String toString() {
			return this.name;
		}
	}
	
	/* the "open db" menu item in the client use this ActionListener */
	   class OpenDBListener implements ActionListener
	      {
	         public void actionPerformed(ActionEvent event)
	         {
	 			int returnVal = jFileChooser.showOpenDialog(getParent());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					System.out.println("You chose to open this file: " + jFileChooser.getSelectedFile().getAbsolutePath());
					String dbFileName = jFileChooser.getSelectedFile().getAbsolutePath();
					try {
						connectToDB(dbFileName);
//						indicating the name of the Active DB
						dbName.setText(dbFileName.substring(dbFileName.lastIndexOf("/")+1));
//						queryButtonListener.setConnection(conn);
						//clearComboBox();
//						filling the contents of the dropdown box listing names 
						fillComboBox();
						
					} catch (Exception e ) {
						System.err.println("error connection to db: "+ e.getMessage());
						e.printStackTrace();

					}
					
				}
	         }
	      }
	   class OpenConnectionListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new Socket("localhost", 8001);
					textArea.append("Connected");
					statusLabel.setText("localhost:8001");
				} catch (IOException e1) {
					e1.printStackTrace();
					textArea.append("Connection Failure");
				}
			}
			  
		  }
	
	public static void main(String[] args) {
		ClientInterface ci = new ClientInterface();
		ci.setVisible(true);
	}
}
