import java.util.Scanner;

public class Client {
    /**
     * This is the method to run the client with the choice of UDP or TCP/IP. If the client choose TCP/IP
     * the function would call chatServer, if the choice is UDP, it would call UDPChatServer
     *
     * @param args Unused.
     * @return none
     */
    public Client() {
        Scanner in = new Scanner(System.in);//Start the scanner object
        System.out.println("How would you like to run the chat Client? UDP or TCP/IP?");//Print instructions
        String response = in.next();
        while (!response.equals("UDP") && !response.equals("TCP/IP")) { //Check for correct response
            System.out.println(response);//if incorrect resoponse, print error message. 
            System.out.println("Incorrect response please respond with exactly UPD or TCP/IP");
            System.out.println("How would you like to run the chat client? UDP or TCP/IP?");
            response = in.next();
        }
        System.out.println(response);
        if (response.equals("TCP/IP")) {
            new ChatClient1();//if User wants TCP, launch TCP client.
        } else if (response.equals("UDP")) {
        	try {
				new UDPChatClient();//If user wants UDP, launch UDP client. 
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
    public static void main(String[] args) {
        new Client();
    } // main end
}

