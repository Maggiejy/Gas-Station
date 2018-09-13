import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.*;


/**
 * NSSA-290 Networking Essentials For Development
 * ChatClient1.java
 * Purpose: 
 * 
 * @author Mitchell Sweet, Caitlyn Daly and Yang Jin
 * @version 1.0 12/5/2017
 */
public class ChatClient1 extends JFrame implements ActionListener {

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

   /**
   * The constructor of chat client. It creates the panel of the chat box. 
   */
   public ChatClient1() {
   
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
      
      jtaInput.addActionListener(new ActionListener(){
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
      this.addWindowFocusListener(new WindowAdapter() {
          public void windowGainedFocus(WindowEvent e) {
              jtaInput.requestFocusInWindow();
          }
      });
      
      // close socket on window close
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            doExit();
         }
      });
      
      // **** CONNECT TO SERVER *********************** //
      SERVER_NAME = JOptionPane.showInputDialog(null, "Please enter the server address:");

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
      
      clientName = JOptionPane.showInputDialog(null, "Please enter your user name:");
      this.setTitle("ChatClient - " + clientName);
      
      if (sock != null && rdr != null && wrt != null) {

         ClientThread ct = new ClientThread();
         Thread t = new Thread(ct);
         t.start();
      }
      
      jbSend.addActionListener(this);
      jmiExit.addActionListener(this);

   } // ChatClient1 constructor end
   
  /**
   * The function to call the method doSend and doExit
   * @return none
   */
   public void actionPerformed (ActionEvent ae) {
      String command = ae.getActionCommand();
      
      if (command.equals("Send")) {
         doSend();   
      }
      else if (command.equals("Exit")) {
         doExit();
      }

   } // actionPerformed end
   
  /**
   * The main function that create new ChatClient object.
   * @return none
   */
   public static void main (String[] args) {
      new ChatClient1();
   }// main end
   
  /**
   * The function to send in the client's input
   * @return none
   */
   public void doSend() {
         
         if (jtaInput.getText().equals("")) {
            return;
         }
         else {
            String sendText = jtaInput.getText();
                
            wrt.println(clientName + "%N@M3" + sendText);
            wrt.flush();
            jtaInput.setText(null);
         }
   }// doSend end
   
   /**
   * The function to end the PrintWriter and finally exit
   * @return none
   */
   public void doExit() {
     
      wrt.println(clientName + "%N@M3EX1T@0");
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
   
   
   class ClientThread implements Runnable {
     /**
      * The constructor of Client Thread
      */
      public ClientThread() {
      
      }
     /**
      * This function 
      */
      public void run() {
      // **** HANDSHAKE *********************** //    
         try {   
            wrt.println(clientName + "%N@M3H@ND5H@K3_H3110");
            wrt.flush();
            
            String hsMessage = rdr.readLine();
            jtaAll.append(hsMessage + "\n");
            
         }
         catch (IOException ioe){
               JOptionPane.showMessageDialog(null,
               "IO Exception (Client Stream Open): " + ioe, "IO Exception", 
               JOptionPane.ERROR_MESSAGE);
               return;
         }
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
               "IO Exception (Client stream open): " + ioe, "IO Exception", 
               JOptionPane.ERROR_MESSAGE);
               return;
         } 
         while (true) {

            try {
               String broadcast = rdr.readLine();
               
               jtaAll.requestFocusInWindow();
               jtaAll.append(broadcast + "\n");
               int len = jtaAll.getDocument().getLength();
               jtaAll.setCaretPosition(len);
               jtaInput.requestFocusInWindow();
               
            }
            catch (IOException ioe){
                  JOptionPane.showMessageDialog(null,
                  "IO Exception (Client broadcast receive): " + ioe, "IO Exception", 
                  JOptionPane.ERROR_MESSAGE);
                  return;
            }
         } // broadcast while true listener end
      } // run end
   } // ClassthThread class end
   
} // ChatClient1 end
