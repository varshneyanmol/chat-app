package com.app.client;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class GroupChatManager {

	private ResourceBundle config = ResourceBundle.getBundle("com.app.config");
	private Client client;
	private int groupID = 0;
	private GroupUsers groupUsers;

	private ArrayList<GroupChatWindow> groupChatWindows = new ArrayList<GroupChatWindow>();

	public GroupChatManager(Client client) {
		this.client = client;
	}

	public void makeGroup(String msg) {
		/**
		 * sends a msg like: "/g//f/134,614,309,990"
		 */
		msg = config.getString("group-chat-delimiter") + config.getString("group-form-delimiter") + msg;
		client.send(msg.getBytes());
	}

	public void process(String msg) {
		String id_d = config.getString("id-delimiter");

		if (msg.startsWith(config.getString("group-form-delimiter"))) {
			String g_f_d = config.getString("group-form-delimiter");

			/**
			 * receives an ack like:
			 * "/f/362/i/anmol:134,aditya:614,patel:309,gaurav:990". msgArr[0] =
			 * "", msgArr[1] = "362", msgArr[2] =
			 * "anmol:134,aditya:614,patel:309,gaurav:990"
			 */
			String[] msgArr = msg.split(g_f_d + "|" + id_d);

			int group_id = Integer.parseInt(msgArr[1]);
			String[] members = msgArr[2].split(",");
			String consoleMsg = members[0].split(":")[0] + " created the group with members:\n";

			GroupChatWindow w = new GroupChatWindow(group_id, client, this);
			groupChatWindows.add(w);

			groupUsers = w.getGroupUsers();

			for (int i = 0; i < members.length; i++) {
				consoleMsg = consoleMsg + members[i].split(":")[0] + ", ";
				groupUsers.update(members[i]);
			}

			w.console(consoleMsg);

		} else if (msg.startsWith(config.getString("broadcast-delimiter"))) {
			String b_d = config.getString("broadcast-delimiter");

			String[] msgArr = msg.split(b_d + "|" + id_d);
			int group_id = Integer.parseInt(msgArr[1]);
			String message = msgArr[2];

			for (int i = 0; i < groupChatWindows.size(); i++) {
				if (group_id == groupChatWindows.get(i).getGroupID()) {
					groupChatWindows.get(i).console(message);
					break;
				}
			}
		} else if (msg.startsWith(config.getString("disconnect-delimiter"))) {
			/**
			 * receives a msg like:
			 * "/d/362/i/aditya:614/i/aditya:614 left the group". OR
			 * "/d/362/i/aditya:614/i/You are the only one left in the group".
			 * OR "/d/362/i/jatin:134/i/aditya:614 removed jatin:134". OR
			 * "/d/362/i/jatin:134/i/aditya:614 removed you".
			 */

			String d_d = config.getString("disconnect-delimiter");
			String[] msgArr = msg.split(d_d + "|" + id_d);
			int group_id = Integer.parseInt(msgArr[1]);
			String removedClient = msgArr[2];
			String message = msgArr[3];
			int removedClientID = Integer.parseInt(removedClient.split(":")[1]);

			for (int i = 0; i < groupChatWindows.size(); i++) {
				if (group_id == groupChatWindows.get(i).getGroupID()) {
					groupChatWindows.get(i).console(message);
					if (removedClientID == client.getID())
						groupUsers.clear();
					else
						groupUsers.remove(removedClient);
					break;
				}
			}
		}
	}

	public void closeAllGroupChatWindows() {
		for (int i = 0; i < groupChatWindows.size(); i++) {
			groupChatWindows.get(i).disconnect();
		}
	}

	public String getAllGroupsIDs() {
		/**
		 * returns a string of all the groups ids this client is added in,
		 * seperated by comma.
		 * 
		 */
		String ids = "";
		for (int i = 0; i < groupChatWindows.size(); i++) {
			ids = ids + groupChatWindows.get(i).getGroupID() + ",";
		}
		return ids;
	}

}
