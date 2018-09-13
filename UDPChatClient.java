import java.io.*;  
import java.net.*; 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.*;

/**
 * NSSA-290 Networking Essentials For Development
 * UDPChatClient.java
 * Purpose: connect to the server and send messages to other clients.
 *
 * @author Mitchell Sweet, Caitlyn Daly and Yang Jin
 * @version 1.0 12/5/2017
 */
public class UDPChatClient extends JFrame implements ActionListener {
   //Crate GUI objects
   private JTextArea jtaAll = new JTextArea();
   private JTextField jtaInput = new JTextField(25);
   private JPanel jpSend = new JPanel();
   private JButton jbSend = new JButton("Send");
   
   private TitledBorder tbInput = new TitledBorder("Type your message - Press ENTER to send");
   
   private BufferedReader rdr = null;
   private PrintWriter wrt = null;
   private Socket sock = null;
   private String SERVER_NAME = "localhost";
   public static final int SERVER_PORT = 16734;
   private String clientName = "";
   
   private JMenuBar jmbMenuBar = new JMenuBar();
   private JMenu jmFile = new JMenu("File");
   private JMenuItem jmiExit = new JMenuItem("Exit");

   //Constructor sets up gui and sockets. 
   public UDPChatClient(){
      this.setTitle("ChatClient");
      this.setLocation(20, 200);
      this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      this.setSize(450, 425);
      this.setLayout(new BorderLayout());
      
      jmbMenuBar.add(jmFile);
      jmFile.add(jmiExit);
      this.setJMenuBar(jmbMenuBar);    
      
      //jtaAll.setPreferredSize(new Dimension(400, 282));
      //jtaInput.setPreferredSize(new Dimension(400, 30));
      //jpSend.setPreferredSize(new Dimension(400, 40));
      jpSend.setBorder(tbInput);
      
      jtaInput.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent e){
               jbSend.doClick();
            }
         });      
      
      
      jtaAll.setEditable(false);
      //Scroll Panel       
      JScrollPane jsScrollPane = new JScrollPane(jtaAll); 
      jsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      this.add(jsScrollPane, BorderLayout.NORTH);
      jsScrollPane.setPreferredSize(new Dimension(400, 282));
      
      
      this.add(jtaInput, BorderLayout.CENTER);
      this.add(jpSend, BorderLayout.SOUTH);   
      
      setVisible(true);
      
      // set cursor focus to message
      this.addWindowFocusListener(
         new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
               jtaInput.requestFocusInWindow();
            }
         });
      
      // close socket on window close
      this.addWindowListener(
         new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
               doExit();
            }
         });
      
      // **** CONNECT TO SERVER *********************** //
      SERVER_NAME = JOptionPane.showInputDialog(null, "Please enter the server address:");
      
      //Create socket. 
      try {
         sock = new Socket(SERVER_NAME, SERVER_PORT);
      }
      catch (UnknownHostException uhe){
         JOptionPane.showMessageDialog(null,
            "Unknown Host: " + SERVER_NAME, "Unknown Host", 
            JOptionPane.ERROR_MESSAGE);
         return;
      }
      catch (IOException ioe){
         JOptionPane.showMessageDialog(this,
            "Server not running, or server error! - Shutting Down", "No Server", 
            JOptionPane.ERROR_MESSAGE);
         System.exit(0);
      }
      //get streams from server
      try {
         rdr = new BufferedReader(
               new InputStreamReader(
               sock.getInputStream() ));
         wrt = new PrintWriter(
               new OutputStreamWriter(
               sock.getOutputStream() ));
      }
      catch (IOException ioe){
         JOptionPane.showMessageDialog(null,
            "IO Exception (Client Stream Open): " + ioe, "IO Exception", 
            JOptionPane.ERROR_MESSAGE);
         return;
      } 
      //Ask for user's name.
      clientName = JOptionPane.showInputDialog(null, "Please enter your user name:");
      this.setTitle("ChatClient - " + clientName);
      
      try{
         InetAddress ia = InetAddress.getByName("localhost");
         SenderThread sender = new SenderThread(ia, SERVER_PORT);
         sender.start();
         ReceiverThread receiver = new ReceiverThread(sender.getSocket());
         receiver.start();
         //String sendText = jtaInput.getText();
         String sendText = "welcome"; 
         sender.setMessage(sendText); 
      } 
      catch(Exception e){
         e.printStackTrace();
      }
      
      
      jbSend.addActionListener(this);
      jmiExit.addActionListener(this);
   }
   //Action performed method activates buttons and enter key. 
   public void actionPerformed (ActionEvent ae) {
      String command = ae.getActionCommand();
      
      if (command.equals("Send")) {
         doSend();   
      }
      else if (command.equals("Exit")) {
         doExit();
      }
   } // actionPerformed end

   public void doSend(){
         
      if (jtaInput.getText().equals("")) {
         return;
      }
      else {
         String sendText = jtaInput.getText();
         //sender.setMessage(sendText); 
          //wrt.println( sendText);
         //wrt.flush();
         jtaInput.setText(null);
      }
   }// doSend end
   
   public void doExit() {
     
      wrt.flush();
   
      try {
         sock.close();
         rdr.close();
         wrt.close();
      }
      catch (IOException ioe){
         JOptionPane.showMessageDialog(null,
            "Error closing socket" + ioe, "Error", 
            JOptionPane.ERROR_MESSAGE);
         System.exit(0);
      }
      finally {
         System.exit(0);
      }
   }// doExit end
    
   public static void main(String args[]) throws Exception  {  
      new UDPChatClient();            
   }
}      
       
//This thread takes care of sending the messages. 
class SenderThread extends Thread {
    
   private InetAddress IPAddress;
   private DatagramSocket socket;
   private boolean off = false;
   private int serverport;
   private String me;
    
    //constructor
   public SenderThread(InetAddress address, int serverport) throws SocketException {
      this.IPAddress = address;
      this.serverport = serverport;
      this.socket = new DatagramSocket();
           
   }
   public DatagramSocket getSocket() {
      return this.socket;
   }
   public void setMessage(String message){
      System.out.println(message);
      System.out.println("Is the message");
      me = message;
   }
   
   //Thread method to send messages. 
   public void run() {   
          
      try {   
      
         while (true) 
         {
            if (off) return;                  
            byte[] sendData = new byte[64];
            sendData = me.getBytes();
            System.out.println(sendData);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverport);
            socket.send(sendPacket); 
         }
             
      } 
      catch (IOException e) {
         e.printStackTrace();
      }catch (NullPointerException e){
         e.printStackTrace();
      }
       
   }
}   

//This thread takes care of incoming messages. 
class ReceiverThread extends Thread {
    
    //Create datagram.
   private DatagramSocket socket;
   private boolean off = false;
   public ReceiverThread(DatagramSocket ds) throws SocketException {
      this.socket = ds;
   }
   //Run method receives data and prints it. 
   public void run() {
      byte[] receiveData = new byte[64];        
      while (true) {            
         if (off) 
            return;
         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            
         if(receivePacket.getLength()==0||receivePacket.getData().equals(null)){
            System.out.println("noreceived");
         }
            
         try {
            socket.receive(receivePacket);
            String response =  new String(receivePacket.getData());
            System.out.println("Response: \"" + response + "\"\n"); 
         } 
         catch (IOException ex) {
            System.err.println(ex);
         } 
         catch (NullPointerException e) {
            e.printStackTrace();
         }
      }
   }
}
