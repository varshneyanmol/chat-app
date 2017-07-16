package com.app.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class OnlineUsers extends JFrame {

	private JPanel contentPane;
	private JList<String> onlineUsersList;
	private JList<String> groupList;
	private DefaultListModel<String> group;
	private DefaultListModel<String> users;

	GroupChatManager gcManager;
	Client client;

	public OnlineUsers(Client client, GroupChatManager gcManager) {
		this.client = client;
		this.gcManager = gcManager;
		//gcManager = new GroupChatManager(this.client);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		setTitle("Private chat");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(450, 400);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		users = new DefaultListModel<String>();
		group = new DefaultListModel<String>();

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 50, 150, 50, 150, 50 };
		gbl_contentPane.rowHeights = new int[] { 30, 150, 150, 70 };
		gbl_contentPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		onlineUsersList = new JList<String>(users);
		JScrollPane scroll1 = new JScrollPane(onlineUsersList);
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.insets = new Insets(0, 0, 5, 5);
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.gridx = 1;
		gbc_list.gridy = 1;
		gbc_list.gridwidth = 1;
		gbc_list.gridheight = 2;
		contentPane.add(scroll1, gbc_list);

		groupList = new JList<String>(group);
		JScrollPane scroll2 = new JScrollPane(groupList);
		GridBagConstraints gbc_list1 = new GridBagConstraints();
		gbc_list1.insets = new Insets(0, 0, 5, 5);
		gbc_list1.fill = GridBagConstraints.BOTH;
		gbc_list1.gridx = 3;
		gbc_list1.gridy = 1;
		gbc_list1.gridwidth = 1;
		gbc_list1.gridheight = 2;
		contentPane.add(scroll2, gbc_list1);

		JButton btnAdd = new JButton(">");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> selected = onlineUsersList.getSelectedValuesList();
				if (!selected.isEmpty()) {
					ArrayList<String> groupListElements = new ArrayList<String>();
					for (int i = 0; i < group.getSize(); i++) {
						groupListElements.add(group.get(i));
					}
					for (int i = 0; i < selected.size(); i++) {
						if (groupListElements.contains(selected.get(i)))
							continue;
						group.addElement(selected.get(i));
					}
				}
				onlineUsersList.clearSelection();
			}
		});
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
		gbc_btnAdd.gridx = 2;
		gbc_btnAdd.gridy = 1;
		contentPane.add(btnAdd, gbc_btnAdd);

		JButton btnRemove = new JButton("<");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				while (groupList.getSelectedIndex() != -1) {
					group.remove(groupList.getSelectedIndex());
				}
			}
		});
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.insets = new Insets(0, 0, 5, 5);
		gbc_btnRemove.gridx = 2;
		gbc_btnRemove.gridy = 2;
		contentPane.add(btnRemove, gbc_btnRemove);

		JButton connect = new JButton("start chat");
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startGroupChat();
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 3;
		contentPane.add(connect, gbc_btnNewButton);

	}

	private void startGroupChat() {
		if (group.getSize() == 0) {
			return;
		}

		int id = client.getID();
		String groupMembersID = id + ",";
		int numMembers = 1;
		for (int i = 0; i < group.getSize(); i++) {
			int user_id = Integer.parseInt(group.getElementAt(i).split(":")[1]);
			if (id == user_id)
				continue;
			groupMembersID = groupMembersID + user_id + ",";
			numMembers++;
		}

		if (numMembers > 1) {
			gcManager.makeGroup(groupMembersID);
			group.clear();
			dispose();
		}
	}

	public void update(String user) {
		users.add(0, user);
	}

	public void clear() {
		users.clear();
		group.clear();
	}

	public void remove(String disconnectedUser) {
		users.removeElement(disconnectedUser);
		group.removeElement(disconnectedUser);
	}

	// public GroupChatManager getGroupChatManager() {
	// return gcManager;
	// }
}
