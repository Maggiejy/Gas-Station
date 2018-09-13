import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

/**
 * NSSA-290 Networking Essentials For Development
 * ChatServer.java
 * Purpose: connect to the server and have feed back to the client
 * 
 * @author Mitchell Sweet, Caitlyn Daly and Yang Jin
 * @version 1.0 12/5/2017
 */
public class ChatServer extends JFrame {

   private JPanel jpNorth = new JPanel();
   private TitledBorder tbServer = new TitledBorder("Chat Server :");
   private JTextArea jtaMain = new JTextArea();
   
   private ServerSocket servSock = null;
   private BufferedReader rdr = null;
   private PrintWriter wrt = null;
   
   private Socket sock = null;
   public static final String SERVER_NAME = "";
   public static final int SERVER_PORT = 16734;
   
   private Vector<ServerRunnable> serverList = new Vector<ServerRunnable>();
   private ArrayList<String> nameHolder = new ArrayList<String>();
   private Vector<Thread> threadHolder = new Vector<Thread>();
   
   private int clientCount = 0;
   
   private JLabel ipAddress = new JLabel();
   private JLabel jlConnected = new JLabel("Connected Clients: " + clientCount);
   private final Lock rLock = new ReentrantLock(true);

  /**
   * The constructor of the chat server that initiate the server with the layout of the panel
   */
   public ChatServer() {
      //Layout of the panel
      this.setTitle("Chat Server Message Console");
      this.setLocation(1180, 200);
      this.setDefaultCloseOperation(EXIT_ON_CLOSE);
      this.setSize(400, 440);
      this.setLayout(new BorderLayout());
      
      jpNorth.setBorder(tbServer);
      jpNorth.setPreferredSize(new Dimension(400, 100));
      
      JPanel jpLabels = new JPanel(new GridLayout(0,1));
      jpLabels.add(ipAddress);
      jpLabels.add(jlConnected);
      jpNorth.add(jpLabels);
      
      this.add(jpNorth, BorderLayout.NORTH);
      
      
      //Scroll Panel 
      jtaMain.setEditable(false);
      this.add(new JScrollPane(jtaMain), BorderLayout.CENTER);
   
      setVisible(true);
      
      try {
         servSock = new ServerSocket(SERVER_PORT);   
         InetAddress ip = InetAddress.getLocalHost();
         ipAddress.setText( "IP Address: " + ip.getHostAddress() );
         
         System.out.println("getLocalHost: "+InetAddress.getLocalHost() );
      } 
      catch (IOException ioe){
         JOptionPane.showMessageDialog(null,
            "IO Exception" + ioe, "IO Exception", 
            JOptionPane.ERROR_MESSAGE);
         System.exit(1);
      }
      
      //create the thread and start the new thread
      while(true) {
         try {
            Socket sock = servSock.accept();
            ServerRunnable cr = new ServerRunnable(sock, serverList);
            Thread t = new Thread(cr);
            t.start();
            
            serverList.add(cr);
            threadHolder.add(t);
            
            clientCount++;
            jlConnected.setText("Connected Clients: " + clientCount);
            
            System.out.println( "serverList: " + serverList.size() );
            
         }
         catch (IOException ioe){
            JOptionPane.showMessageDialog(null,
               "IO Exception" + ioe, "IO Exception", 
               JOptionPane.ERROR_MESSAGE);
            System.exit(1);
         }
      }

   } // ChatServer constructor end
   

   class ServerRunnable implements Runnable {
      
      private Socket sock;
      private Vector<ServerRunnable> serverList = new Vector<ServerRunnable>();

      private PrintStream os = null;
      private BufferedReader is = null;      
	   
      /**
       * the constructor of ServerRunnable 
       */
      public ServerRunnable(Socket _sock, Vector<ServerRunnable> _serverList) {
         serverList = _serverList;
         sock = _sock;
      } // ServerRunnable constructor end
      
      /**
       * the method to open up the chat, and print out the information with the
       * client count and client name.
       * @return none
       */     
      public void run() {
         try {
            is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            
            os = new PrintStream(sock.getOutputStream());
         }
         catch (IOException ioe) {
            JOptionPane.showMessageDialog(null,
               "IO Exception(Server Stream Open): " + ioe, "IO Exception", 
               JOptionPane.ERROR_MESSAGE);
            return;
         }
         
         try {
            while(true) {
               String message = is.readLine();
               
               String[] parts = message.split("%N@M3");
               String clientName = (parts[0]);
		    
               if (parts[1].equals("H@ND5H@K3_H3110")) {
                  
                  writeString("FROM SERVER: Welcome to Team Chat!: " + clientName);

                  jtaMain.append("Client: " + clientName + " connected.\n");
                  
                  nameHolder.add(clientName);
                  System.out.println( "nameHolder: " + nameHolder.size() );
              
               }
               else if (parts[1].equals("EX1T@0")) {
                  int index = -1;
                  for (int i=0; i < nameHolder.size(); i++) {
                      if (nameHolder.get(i).equals(clientName)) {
                          index = i;
                          break;
                      }
                  }
                  
                  threadHolder.get(index).interrupt();
                  threadHolder.remove(index);
                  serverList.remove(index);
                  nameHolder.remove(index);
                  
                  System.out.println( "serverList: " + serverList.size() );
                  System.out.println( "nameHolder: " + nameHolder.size() );
                  
                  jtaMain.append("Client " + clientName + " has disconnected.\n");
                  clientCount--;
                  jlConnected.setText("Connected Clients: " + clientCount);
                  
                  writeString(clientName + " has left the chat.");

               }
               else {
               
                  writeString("From " + clientName + ": " + parts[1]);
                  
                  jtaMain.append("Client " + clientName + " sent a message.\n");
                  int len = jtaMain.getDocument().getLength();
                  jtaMain.setCaretPosition(len);
                  
               }
            }
         }          
         catch(NullPointerException npe){return;}
         catch (IOException ioe){
            JOptionPane.showMessageDialog(null,
               "IO Exception(CR-RUN-2): " + ioe, "IO Exception", 
               JOptionPane.ERROR_MESSAGE);
            return;
         }       
      } // run end
	   
     /**
      * This is the method to write out the information. It will be called in the run function
      * @param s the string of information that wants to print out
      * @return none
      */      
      public void writeString(String s) {
        rLock.lock();
           for (int i = 0; i < serverList.size(); i++) {
             if (serverList.get(i) != null) {
               serverList.get(i).os.println(s);
             }
           }   
        rLock.unlock();
      } // writeString end     
   } // ServerRunnable end

} // ChatServer end

/**
 * This file will contain in the same folder as UDPChatServer.java. 
 * The main function will call the UDPChatServer after compile the files.
 * Two steps of commonds to run the file:
 * 1. javac ChatServer.java UDPChatServer.java
 * 2. java ChatServer
 */
