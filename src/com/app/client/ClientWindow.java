package com.app.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class ClientWindow extends JFrame {

	private JPanel contentPane;
	private JTextArea chatHistory;
	private JTextField messageField;

	private Client client;
	private Thread listen;
	private boolean running = false;

	private ResourceBundle config = ResourceBundle.getBundle("com.app.config");
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;

	private OnlineUsers users;
	private GroupChatManager gcManager;

	public ClientWindow() {

	}

	public ClientWindow(String name, String destIP, int destPort) {
		client = new Client(name, destIP, destPort);
		gcManager = new GroupChatManager(client);
		users = new OnlineUsers(client, gcManager);
		// gcHandler = users.getGroupChatHandler();
		generateWindow();
		connectToServer();
	}

	private void connectToServer() {
		console("Connecting you to server running at " + client.getDestIP_Str() + ":" + client.getDestPort() + " ...");
		boolean connect = client.openConnection();
		if (!connect) {
			System.err.println("Connection failed!");
			console("Connection Failed!");
			return;
		}

		running = true;
		String connectionMsg = config.getString("connect-delimiter") + client.getName();
		client.send(connectionMsg.getBytes());
		listen();
	}

	private void generateWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("Client: " + client.getName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(600, 430);
		setLocationRelativeTo(null);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmOnlineUsers = new JMenuItem("online users");
		mntmOnlineUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				users.setVisible(true);
			}
		});
		mnFile.add(mntmOnlineUsers);

		mntmExit = new JMenuItem("exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnect();
				dispose();
			}
		});
		mnFile.add(mntmExit);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 10, 540, 50 };
		gbl_contentPane.rowHeights = new int[] { 10, 15, 340, 55 };
		contentPane.setLayout(gbl_contentPane);

		chatHistory = new JTextArea();
		chatHistory.setFont(new Font("Purisa", Font.PLAIN, 14));
		chatHistory.setForeground(new Color(128, 0, 0));
		chatHistory.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(chatHistory);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.weightx = 1;
		gbc_scrollPane.weighty = 1;
		contentPane.add(scrollPane, gbc_scrollPane);

		messageField = new JTextField();
		messageField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					broadCast(messageField.getText());
				}
			}
		});
		GridBagConstraints gbc_messageField = new GridBagConstraints();
		gbc_messageField.insets = new Insets(0, 0, 0, 5);
		gbc_messageField.fill = GridBagConstraints.HORIZONTAL;
		gbc_messageField.gridx = 0;
		gbc_messageField.gridy = 3;
		gbc_messageField.gridwidth = 2;
		gbc_messageField.weightx = 1;
		gbc_messageField.weighty = 0;
		contentPane.add(messageField, gbc_messageField);
		messageField.setColumns(10);

		JButton btnSend = new JButton("send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				broadCast(messageField.getText());
			}
		});
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.gridx = 2;
		gbc_btnSend.gridy = 3;
		gbc_btnSend.weightx = 0;
		gbc_btnSend.weighty = 0;
		contentPane.add(btnSend, gbc_btnSend);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				disconnect();
			}
		});

		setVisible(true);
		messageField.requestFocusInWindow();
	}

	public void listen() {
		listen = new Thread("Listen") {
			public void run() {
				while (running) {
					String message = client.receive();
					process(message);
				}
			}
		};
		listen.start();
	}

	private void process(String message) {
		if (message.startsWith(config.getString("connect-delimiter"))) {
			// int id = Integer.parseInt(
			// message.substring(config.getString("connect-delimiter").length(),
			// message.length()).trim());

			message = message.substring(config.getString("connect-delimiter").length(), message.length()).trim();
			String[] arr = message.split(",");
			int id = Integer.parseInt(arr[0]);
			if (arr.length > 1) {
				for (int i = 1; i < arr.length; i++) {
					users.update(arr[i]);
				}
			}

			client.setID(id);
			console("connected with client ID: " + client.getID());

		} else if (message.startsWith(config.getString("broadcast-delimiter"))) {
			message = message.substring(config.getString("broadcast-delimiter").length(), message.length()).trim();
			console(message);

		} else if (message.startsWith(config.getString("ping-delimiter"))) {
			String pingMessage = config.getString("ping-delimiter") + client.getID();
			send(pingMessage);

		} else if (message.startsWith(config.getString("online-users-delimiter"))) {
			String user = message.substring(config.getString("online-users-delimiter").length(), message.length())
					.trim();
			users.update(user);

		} else if (message.startsWith(config.getString("disconnect-delimiter"))) {

			message = message.trim();
			if (message.length() == config.getString("disconnect-delimiter").length()) {
				message = "Server: You have been disconnected by server";
				users.clear();
				console(message);

			} else {
				String disconnectedUser = message
						.substring(config.getString("disconnect-delimiter").length(), message.length()).trim();
				users.remove(disconnectedUser);
			}

		} else if (message.startsWith(config.getString("group-chat-delimiter"))) {
			message = message.substring(config.getString("group-chat-delimiter").length(), message.length()).trim();
			gcManager.process(message);
		}
	}

	private void send(String message) {
		message = message.trim();
		if (message.equals(""))
			return;
		client.send(message.getBytes());
	}

	private void broadCast(String message) {
		message = message.trim();
		if (message.equals("")) {
			return;
		}
		message = config.getString("broadcast-delimiter") + client.getID() + config.getString("id-delimiter") + message;
		send(message);
		messageField.setText("");
	}

	private void disconnect() {
		/**
		 * sends a msg like(if client is connected to groups):
		 * "/d/123/i/507,157,844," OR sends a msg like (if client is not in any
		 * group): "/d/123/i/"
		 */
		String groupIDs = gcManager.getAllGroupsIDs();
		String message = config.getString("disconnect-delimiter") + client.getID() + config.getString("id-delimiter")
				+ groupIDs;
		send(message);
		// gcManager.closeAllGroupChatWindows();
		running = false;
		client.close();
	}

	private void console(String message) {
		chatHistory.append(message + "\n");

		// update the caret of the JtextArea at the latest message
		chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
	}
}
