/*
 * 1408063 Soyeon Park
 */
package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ServerGUI {

	private JFrame frame;
	private static JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private Server server;
    
	public ServerGUI(Server server) {
		this.server = server;
        initialize();
	}
	
	public void show() {
        frame.setVisible(true);
    }


	private void initialize() {
		frame = new JFrame("Dictionary Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);

        logArea = new JTextArea();
        logArea.setBackground(new Color(255, 250, 250));
        JScrollPane scrollPane = new JScrollPane(logArea);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(255, 228, 225));
        scrollPane.setColumnHeaderView(panel);

        startButton = new JButton("Start Server");
        startButton.setForeground(new Color(0, 0, 255));
        startButton.setBackground(new Color(255, 255, 255));
        startButton.addActionListener(e -> {
        	new Thread(server::startServer).start();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            logArea.append("Server started.\n");
        });
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel.add(startButton);

        stopButton = new JButton("Stop Server");
        stopButton.setForeground(new Color(255, 0, 0));
        stopButton.setBackground(new Color(255, 255, 255));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> {
            server.closeServer();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            logArea.append("Server stopped.\n");
        });
        panel.add(stopButton);

        frame.setVisible(true);
		
	}

	public void appendLog(String message) {
	    SwingUtilities.invokeLater(() -> {
	        logArea.append(message + "\n");
	        logArea.setCaretPosition(logArea.getDocument().getLength()); 
	    });
	}
}
