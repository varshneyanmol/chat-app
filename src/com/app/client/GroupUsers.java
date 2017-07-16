package com.app.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.ActionEvent;

public class GroupUsers extends JFrame {

	private JPanel contentPane;
	private JList<String> list;
	private DefaultListModel<String> model;
	private Client client;
	// private GroupChatManager gcManager;
	private GroupChatWindow gcWindow;

	public GroupUsers(Client client, GroupChatManager gcManager, GroupChatWindow gcWindow) {
		this.client = client;
		// this.gcManager = gcManager;
		this.gcWindow = gcWindow;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Group users");
		setSize(300, 400);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 50, 200, 50 };
		gbl_contentPane.rowHeights = new int[] { 50, 250, 80, 20 };
		gbl_contentPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		model = new DefaultListModel<String>();
		list = new JList<String>(model);
		JScrollPane scroll = new JScrollPane(list);
		GridBagConstraints gbc_scroll = new GridBagConstraints();
		gbc_scroll.insets = new Insets(0, 0, 5, 5);
		gbc_scroll.fill = GridBagConstraints.BOTH;
		gbc_scroll.gridx = 1;
		gbc_scroll.gridy = 1;
		contentPane.add(scroll, gbc_scroll);

		JButton btnRemove = new JButton("remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeUsers();
			}
		});
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.insets = new Insets(0, 0, 5, 5);
		gbc_btnRemove.gridx = 1;
		gbc_btnRemove.gridy = 2;
		contentPane.add(btnRemove, gbc_btnRemove);
	}

	private void removeUsers() {
		if (model.size() == 0) {
			return;
		}

		List<String> selected = list.getSelectedValuesList();
		if (!selected.isEmpty()) {
			String client_id = client.getID() + "";
			boolean removeItself = false;
			String ids = "";
			for (int i = 0; i < selected.size(); i++) {
				String id = selected.get(i).split(":")[1];
				if (id.equals(client_id))
					removeItself = true;
				ids = ids + id + ",";
			}
			gcWindow.disconnectMany(client.getID(), ids);
			if (removeItself) {
				gcWindow.dispose();
				dispose();
			}
		}
	}

	public void update(String user) {
		model.addElement(user);
	}

	public void remove(String user) {
		model.removeElement(user);
	}

	public void clear() {
		model.clear();
	}
}
