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

public class GroupChatWindow extends JFrame {

	private JPanel contentPane;
	private JTextArea chatHistory;
	private JTextField messageField;

	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;

	private int group_id;
	// String group_members_ids;
	private ResourceBundle config = ResourceBundle.getBundle("com.app.config");
	private Client client;
	private GroupUsers groupUsers;
	private GroupChatManager gcManager;

	public GroupChatWindow(int id, Client client, GroupChatManager gcManager) {
		// this.group_members_ids = group_members_ids;
		this.group_id = id;
		this.client = client;
		this.gcManager = gcManager;
		groupUsers = new GroupUsers(client, gcManager, this);
		generateWindow();
	}

	private void generateWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle(client.getName() + "'s chat " + " : group " + this.group_id);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(600, 430);
		setLocationRelativeTo(null);
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmOnlineUsers = new JMenuItem("group users");
		mntmOnlineUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				groupUsers.setVisible(true);
			}
		});
		mnFile.add(mntmOnlineUsers);

		mntmExit = new JMenuItem("exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disconnect();
				dispose();
				groupUsers.dispose();
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
		chatHistory.setBackground(new Color(211, 211, 211));
		chatHistory.setFont(new Font("Purisa", Font.PLAIN, 14));
		chatHistory.setForeground(new Color(0, 128, 0));
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
					broadcastToGroup(messageField.getText());
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
				broadcastToGroup(messageField.getText());
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
				dispose();
				groupUsers.dispose();
			}
		});

		setVisible(true);
		messageField.requestFocusInWindow();
	}

	private void broadcastToGroup(String message) {
		message = message.trim();
		if (message.equals("")) {
			return;
		}
		message = config.getString("group-chat-delimiter") + config.getString("broadcast-delimiter") + group_id
				+ config.getString("id-delimiter") + client.getID() + config.getString("id-delimiter") + message;
		client.send(message.getBytes());
		messageField.setText("");
	}

	public void disconnect() {
		/**
		 * sends a msg like: "/g//d/362/i/614"
		 */
		String msg = config.getString("group-chat-delimiter") + config.getString("disconnect-delimiter") + group_id
				+ config.getString("id-delimiter") + client.getID();
		client.send(msg.getBytes());
	}

	public void disconnectMany(int client_id, String ids) {
		/**
		 * sends a msg like: "/g//d/362/i/614/i/345,890,134,672"
		 */
		String msg = config.getString("group-chat-delimiter") + config.getString("disconnect-delimiter") + group_id
				+ config.getString("id-delimiter") + client_id + config.getString("id-delimiter") + ids;
		client.send(msg.getBytes());
	}

	public void console(String message) {
		chatHistory.append(message + "\n");

		// update the caret of the JtextArea at the latest message
		chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
	}

	public int getGroupID() {
		return group_id;
	}

	public GroupUsers getGroupUsers() {
		return groupUsers;
	}
}
