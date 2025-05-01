/*
 * 1408063 Soyeon Park
 */
package client;

import java.io.*;
import java.net.*;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Client {
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    
    private ClientGUI clientGUI;

    public Client(String ip, int port) throws IOException {
        try {
            socket = new Socket(ip, port);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            
        } catch (IOException e) {
            throw e;
        }
    }
    
    public void setGUI(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Client <server-address> <server-port>");
            return;
        }
        String ip = args[0];
        int port = Integer.parseInt(args[1]);

        SwingUtilities.invokeLater(() -> {
            try {
                Client client = new Client(ip, port);
                ClientGUI clientGUI = new ClientGUI(client);
                client.setGUI(clientGUI);
                clientGUI.setVisible(true);
                
            }
            catch (SocketException e) {
            	JOptionPane.showMessageDialog(null, "Socket closed." + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Cannot connect to the server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public String sendRequest(String command, String word, String meaning) throws IOException, SocketException {
        
    	output.writeUTF(command);
        output.writeUTF(word);
        if (meaning != null) {
            output.writeUTF(meaning);
        }
        
        if (socket.isClosed()) {
            JOptionPane.showMessageDialog(null,"The server connection has been lost.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            return null; 
        }
        
        return input.readUTF();
    } 

    public void disconnect() throws IOException {
        output.writeUTF("DISCONNECT");
        socket.close();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }


}
