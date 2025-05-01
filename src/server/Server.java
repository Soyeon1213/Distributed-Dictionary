/*
 * 1408063 Soyeon Park
 */
package server;

import java.awt.EventQueue;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;


public class Server {
	
	private int port;
	private static String filepath;
	
	private static ServerSocket serverSocket;
	
	static ConcurrentHashMap<String, List<String>> dictionary = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Socket, Thread> connectedClients = new ConcurrentHashMap<>();
	
	private static boolean isRunning = false;
	
	private static ServerGUI gui;
	
	public Server(int port, String filepath) {
        this.port = port;
        Server.filepath = filepath;
    }
	
	public void setGUI(ServerGUI gui) {
        this.gui = gui;
    }

	public static void main(String[] args)
	{
		if(args.length < 2) {
			gui.appendLog("Usage: java Server <port number> <dictionary file path>");
	        System.out.println("Usage: java Server <port number> <dictionary file path>");
	        System.exit(1);
	    }
		
		int port = Integer.parseInt(args[0]);
        String filepath = args[1];
        
        EventQueue.invokeLater(() -> {
            Server server = new Server(port, filepath);
            ServerGUI gui = new ServerGUI(server);
            server.setGUI(gui);
            gui.show();
        });
		
	}
	
	public void startServer() {
		if (isRunning) {
			gui.appendLog("Server is already running.");
            return;
        }
		try {
            serverSocket = new ServerSocket(port);
            gui.appendLog("Server is listening on port " + port);
            isRunning = true;
            readDictionary();

            while (isRunning) {
            	 try {
                     Socket clientSocket = serverSocket.accept();
                     gui.appendLog("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                     
                     Thread clientThread = new Thread(() -> serveClient(clientSocket));
                     clientThread.start();
                     
                     connectedClients.put(clientSocket, clientThread);
                 } 
            	 catch (IOException e) {
                     if (isRunning) {
                    	 gui.appendLog("Error accepting client connection: " + e.getMessage());
                     }
                 }
            }

        } 
		catch (IOException e) {
			gui.appendLog("Server error: " + e.getMessage());
            e.printStackTrace();
        } 
		finally {
            closeServer();
        }
	}
	
	private static void serveClient(Socket client)
	{
		try(Socket clientSocket = client)
		{
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
		    DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            
            boolean running = true;
            
            while (running) {
            	String request = input.readUTF();
                if ("QUERY_WORD".equals(request)) {
                	String queryWord = input.readUTF().toLowerCase();
                	if (dictionary.containsKey(queryWord)) {
                        List<String> meanings = dictionary.get(queryWord);
                        String meaningsStr = String.join(", ", meanings);
                        output.writeUTF(meaningsStr);
                    } 
                	else {
                        output.writeUTF("Word not found.");
                    }
                }
                else if ("ADD_WORD".equals(request)) {
                	String newWord = input.readUTF().toLowerCase();
                    String newMeaning = input.readUTF();
                    if (dictionary.containsKey(newWord)) {
                        output.writeUTF("Word already exists.");
                    } 
                    else {
                    	synchronized (dictionary) {
                            List<String> meanings = dictionary.computeIfAbsent(newWord, k -> new ArrayList<>());
                            meanings.add(newMeaning);
                            saveDictionary();
                            output.writeUTF("Word is added.");
                        }
                    }     	
                }
                else if ("REMOVE_WORD".equals(request)) {
                	String removeWord = input.readUTF().toLowerCase();
                	synchronized (dictionary) { 
                        if (dictionary.remove(removeWord) != null) {
                            saveDictionary(); 
                            output.writeUTF("Word is removed.");
                        } 
                        else {
                            output.writeUTF("Word is not found.");
                        }
                    }
                }
                else if ("UPDATE_WORD".equals(request)) {
                	String updateWord = input.readUTF().toLowerCase();
                    String updateMeaning = input.readUTF().toLowerCase();
                    synchronized (dictionary) {  
                        if (dictionary.containsKey(updateWord)) {
                            List<String> meaningsList = dictionary.get(updateWord);  
                            if (!meaningsList.contains(updateMeaning)) {  
                                meaningsList.add(updateMeaning); 
                                saveDictionary(); 
                                output.writeUTF("Word is updated.");
                            } 
                            else {  
                                output.writeUTF("Meaning already exists."); 
                            }
                        } else {
                            output.writeUTF("Word is not found.");  
                        }
                    }
                }
                else if ("DISCONNECT".equals(request)) {
                	gui.appendLog("Client " + client.getInetAddress().getHostAddress() + " requested to disconnect.");

                    output.writeUTF("DISCONNECT_ACK"); 
                    running = false;  
                }
            }
		} 
		catch (IOException e) 
		{
			gui.appendLog("Error handling client: " + e.getMessage());
			e.printStackTrace();
		} finally {
			connectedClients.remove(client);
	        try {
	            client.close();  
	        } catch (IOException e) {
	            System.err.println("Error closing client socket: " + e.getMessage());
	            gui.appendLog("Error closing client socket: " + e.getMessage());
	        }
	    }
	}
	
	
	public static void readDictionary() {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                	String word = parts[0].trim().toLowerCase();  
                    String[] meanings = parts[1].split(","); 

                    List<String> meaningsList = new ArrayList<>(Arrays.asList(meanings));
                    for (int i = 0; i < meaningsList.size(); i++) {
                        meaningsList.set(i, meaningsList.get(i).trim().toLowerCase()); 
                    }
                    dictionary.put(word, meaningsList);
                }
            }
        } catch (FileNotFoundException e) {
        	gui.appendLog("Dictionary file not found: " + filepath);
        } catch (IOException e) {
        	gui.appendLog("Error reading dictionary file: " + filepath);
        }	
	}
	
	
	public static void printAllWords() {
        if (dictionary.isEmpty()) {
        	gui.appendLog("Dictionary is empty.");
        } else {
        	gui.appendLog("Dictionary contents:");
            for (Entry<String, List<String>> entry : dictionary.entrySet()) {
            	gui.appendLog("Word: " + entry.getKey() + ", Meaning: " + entry.getValue());
                System.out.println("Word: " + entry.getKey() + ", Meaning: " + entry.getValue());
            }
        }
    }
	
	public void closeServer() {
		isRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
            	for (Socket clientSocket : connectedClients.keySet()) {
                    DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
                    output.writeUTF("SERVER_CLOSING");
                    clientSocket.close(); 
                }
                serverSocket.close();
                gui.appendLog("Server has been stopped.");
            } 
            catch (IOException e) {
            	gui.appendLog("Error closing the server: " + e.getMessage());
            }
        }
    }
	
	public static void saveDictionary() {
	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
	        for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
	            String word = entry.getKey();
	            List<String> meanings = entry.getValue();
	            String line = word + ":" + String.join(", ", meanings);
	            writer.write(line);
	            writer.newLine();
	        }
	        gui.appendLog("Dictionary saved successfully.");
	    } catch (IOException e) {
	    	gui.appendLog("Error saving dictionary: " + e.getMessage());
	    }
	}


}