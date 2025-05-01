/*
 * 1408063 Soyeon Park
 */
package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class ClientGUI extends JFrame {
    private Client client;
    private JTextArea textArea;
    private JButton btnQuery, btnAdd, btnRemove, btnUpdate, btnDisconnect;

    public ClientGUI(Client client) {
    	this.client = client;
        initializeUI();
    }


    private void initializeUI() {
        setTitle("Dictionary Client");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setBackground(new Color(255, 250, 250));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 228, 225));
        scrollPane.setColumnHeaderView(buttonPanel);

        btnQuery = new JButton("Query");
        btnQuery.setBackground(new Color(255, 255, 255));
        btnQuery.setForeground(new Color(255, 0, 0));
        btnQuery.addActionListener(e -> queryWord());
        buttonPanel.add(btnQuery);

        btnAdd = new JButton("Add");
        btnAdd.setForeground(new Color(255, 165, 0));
        btnAdd.setBackground(new Color(255, 255, 255));
        btnAdd.addActionListener(e -> addWord());
        buttonPanel.add(btnAdd);

        btnRemove = new JButton("Remove");
        btnRemove.setForeground(new Color(0, 255, 0));
        btnRemove.setBackground(new Color(255, 255, 255));
        btnRemove.addActionListener(e -> removeWord());
        buttonPanel.add(btnRemove);

        btnUpdate = new JButton("Update");
        btnUpdate.setForeground(new Color(0, 0, 255));
        btnUpdate.setBackground(new Color(255, 255, 255));
        btnUpdate.addActionListener(e -> updateWord());
        buttonPanel.add(btnUpdate);

        btnDisconnect = new JButton("Disconnect");
        btnDisconnect.setForeground(new Color(148, 0, 211));
        btnDisconnect.setBackground(new Color(255, 255, 255));
        btnDisconnect.addActionListener(e -> disconnect());
        buttonPanel.add(btnDisconnect);
    }

    private void queryWord() {
        String word = JOptionPane.showInputDialog(this, "Enter the word to query:");
        if (word != null && !word.trim().isEmpty()) {
            try {
                String response = client.sendRequest("QUERY_WORD", word.trim(), null);
                textArea.append("Query: " + capitalizeFirstLetter(word) + " - " + capitalizeFirstLetter(response) + "\n");
            } catch (IOException e) {
                textArea.append("Error: " + e.getMessage() + "\n");
                connectionLost();
            }
        }
    }

    private void addWord() {
        String word = JOptionPane.showInputDialog(this, "Enter the word to add:");
        if (word != null && !word.trim().isEmpty()) {
            String meaning = JOptionPane.showInputDialog(this, "Enter meaning for " + word + ":");
            if (meaning != null && !meaning.trim().isEmpty()) {
                try {
                    String response = client.sendRequest("ADD_WORD", word.trim(), meaning.trim());
                    textArea.append("Add: " + capitalizeFirstLetter(word) + " - " + capitalizeFirstLetter(response) + "\n");
                } catch (IOException e) {
                    textArea.append("Error: " + e.getMessage() + "\n");
                    connectionLost();
                }
            }
        }
    }

    private void removeWord() {
        String word = JOptionPane.showInputDialog(this, "Enter the word to remove:");
        if (word != null && !word.trim().isEmpty()) {
            try {
                String response = client.sendRequest("REMOVE_WORD", word.trim(), null);
                textArea.append("Remove: " + capitalizeFirstLetter(word) + " - " + capitalizeFirstLetter(response) + "\n");
            } catch (IOException e) {
                textArea.append("Error: " + e.getMessage() + "\n");
                connectionLost();
            }
        }
    }

    private void updateWord() {
        String word = JOptionPane.showInputDialog(this, "Enter the word to update:");
        if (word != null && !word.trim().isEmpty()) {
            String newMeaning = JOptionPane.showInputDialog(this, "Enter new meaning for " + word + ":");
            if (newMeaning != null && !newMeaning.trim().isEmpty()) {
                try {
                    String response = client.sendRequest("UPDATE_WORD", word.trim(), newMeaning.trim());
                    textArea.append("Update: " + capitalizeFirstLetter(word) + " - " + capitalizeFirstLetter(response) + "\n");
                } catch (IOException e) {
                    textArea.append("Error: " + e.getMessage() + "\n");
                    connectionLost();
                }
            }
        }
    }

    private void disconnect() {
        try {
            client.disconnect();
            textArea.append("Disconnected from the server.\n");
            disableButtons();
        } catch (IOException e) {
            textArea.append("Error: " + e.getMessage() + "\n");
            connectionLost();
        }
    }
    
    public void connectionLost() {
        disableEditing();
        this.dispose();
    }


    private void disableButtons() {
        btnQuery.setEnabled(false);
        btnAdd.setEnabled(false);
        btnRemove.setEnabled(false);
        btnUpdate.setEnabled(false);
        btnDisconnect.setEnabled(false);
    }
    
    public void disableEditing() {
        btnQuery.setEnabled(false);
        btnAdd.setEnabled(false);
        btnRemove.setEnabled(false);
        btnUpdate.setEnabled(false);
        JOptionPane.showMessageDialog(this, "Server connection lost. Editing is disabled.", "Connection Lost", JOptionPane.WARNING_MESSAGE);
    }
    
    public static String capitalizeFirstLetter(String str) {
	    if (str == null || str.isEmpty()) {
	        return str;
	    }
	    str = str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();

	    char[] chars = str.toCharArray();
	    for (int i = 0; i < chars.length - 1; i++) {
	        if ((chars[i] == ',' && chars[i + 1] == ' ')) {
	            chars[i + 2] = Character.toUpperCase(chars[i + 2]);
	        }
	    }
	    return new String(chars);
	}
    
}