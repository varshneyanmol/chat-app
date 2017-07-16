package com.app.server;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class GroupChatManager {

	private ResourceBundle config = ResourceBundle.getBundle("com.app.config");
	private Server server;

	private ArrayList<Group> groups = new ArrayList<Group>();
	private ArrayList<ConnectedClient> connectedClients;

	public GroupChatManager(Server server) {
		this.server = server;
		connectedClients = server.getConnectedClients();
	}

	public void process(String message) {
		if (message.startsWith(config.getString("group-form-delimiter"))) {
			message = message.substring(config.getString("group-form-delimiter").length(), message.length()).trim();
			createGroup(message);
		} else if (message.startsWith(config.getString("broadcast-delimiter"))) {
			processBroadcast(message);
		} else if (message.startsWith(config.getString("disconnect-delimiter"))) {
			/**
			 * receives a msg like: "/d/362/i/614" OR receives a msg like:
			 * "/d/362/i/614/i/345,890,134,672"
			 */
			String d_d = config.getString("disconnect-delimiter");
			String id_d = config.getString("id-delimiter");

			String[] arr = message.split(d_d + "|" + id_d);
			int group_id = Integer.parseInt(arr[1]);
			int client_id = Integer.parseInt(arr[2]);

			if (arr.length == 3) {
				disconnect(group_id, client_id, false);
				return;
			}

			String clientsToRemove = arr[3];
			disconnectMany(group_id, client_id, clientsToRemove);

		}
	}

	private void processBroadcast(String message) {
		/**
		 * receives a msg like: "/b/235/i/614/i/hey there". sends a msg like:
		 * "/g//b/235/i/aditya: hey there".
		 */
		String b_d = config.getString("broadcast-delimiter");
		String id_d = config.getString("id-delimiter");

		String[] arr = message.split(b_d + "|" + id_d);
		int group_id = Integer.parseInt(arr[1]);
		int client_id = Integer.parseInt(arr[2]);
		String msg = arr[3];

		for (int i = 0; i < groups.size(); i++) {
			if (group_id == groups.get(i).getGroupID()) {
				for (int j = 0; j < connectedClients.size(); j++) {
					if (client_id == connectedClients.get(j).getUID()) {
						msg = config.getString("group-chat-delimiter") + config.getString("broadcast-delimiter")
								+ group_id + config.getString("id-delimiter") + connectedClients.get(j).getName() + " :"
								+ msg;

						broadcastToGroup(msg, groups.get(i).getGroupMembers());
						break;
					}
				}
				break;
			}
		}
	}

	private void createGroup(String msg) {

		/**
		 * receives a msg like: "614,346,809,403"
		 */
		String[] arr = msg.split(",");
		ArrayList<Integer> members = new ArrayList<Integer>();
		for (int i = 0; i < arr.length; i++) {
			members.add(Integer.parseInt(arr[i]));
		}
		int group_id = UniqueIdentifier.getUniqueGroupIdentifier();

		groups.add(new Group(group_id, members));

		msg = "";
		for (int i = 0; i < members.size(); i++) {
			for (int j = 0; j < connectedClients.size(); j++) {
				if (members.get(i) == connectedClients.get(j).getUID()) {
					msg = msg + connectedClients.get(j).getName() + ":" + connectedClients.get(j).getUID() + ",";
				}
			}
		}

		/**
		 * sends a ack like:
		 * "/g//f/362/i/anmol:134,aditya:614,patel:309,gaurav:990"
		 */
		String connnectionAck = config.getString("group-chat-delimiter") + config.getString("group-form-delimiter")
				+ group_id + config.getString("id-delimiter") + msg;

		broadcastToGroup(connnectionAck, members);

	}

	public void disconnect(int group_id, int client_id, boolean sendAck) {
		Group group = getGroup(group_id);
		ConnectedClient c = server.getConnectedClient(client_id);
		boolean removed = false;
		if (group != null)
			removed = group.getGroupMembers().remove(new Integer(client_id));

		if (!removed)
			return;

		if (group.getGroupMembers().size() == 0) {
			groups.remove(group);
			return;
		}

		/**
		 * broadcast a msg like:
		 * "/g//d/362/i/aditya:614/i/aditya:614 left the group"
		 */
		String returnMsg = config.getString("group-chat-delimiter") + config.getString("disconnect-delimiter")
				+ group_id + config.getString("id-delimiter") + c.getName() + ":" + client_id
				+ config.getString("id-delimiter") + c.getName() + ":" + client_id + " left the group";

		broadcastToGroup(returnMsg, group.getGroupMembers());

		if (group.getGroupMembers().size() == 1) {
			/**
			 * send a msg like:
			 * "/g//d/362/i/aditya:641/i/You are the only one left in the group"
			 */
			String groupCloseMsg = config.getString("group-chat-delimiter") + config.getString("disconnect-delimiter")
					+ group_id + config.getString("id-delimiter") + c.getName() + ":" + client_id
					+ config.getString("id-delimiter") + "You are the only one left in the group.";
			broadcastToGroup(groupCloseMsg, group.getGroupMembers());

		}
	}

	private void disconnectMany(int group_id, int client_id, String clientsToRemoveStr) {
		Group group = getGroup(group_id);
		ConnectedClient removerClient = server.getConnectedClient(client_id);

		ArrayList<Integer> clientsToRemove = new ArrayList<Integer>();
		String[] clientsToRemoveStrArr = clientsToRemoveStr.split(",");
		for (int i = 0; i < clientsToRemoveStrArr.length; i++) {
			clientsToRemove.add(Integer.parseInt(clientsToRemoveStrArr[i]));
		}

		for (int i = 0; i < clientsToRemove.size(); i++) {
			ConnectedClient removedClient = server.getConnectedClient(clientsToRemove.get(i));
			boolean removed = false;
			if (group != null)
				removed = group.getGroupMembers().remove(new Integer(removedClient.getUID()));
			if (!removed)
				return;

			if (group.getGroupMembers().size() == 0) {
				groups.remove(group);
				return;
			}

			if (removedClient.getUID() == removerClient.getUID()) {
				/**
				 * broadcast a msg like:
				 * "/g//d/362/i/aditya:614/i/aditya:614 left the group"
				 */
				String ackToGroup = config.getString("group-chat-delimiter") + config.getString("disconnect-delimiter")
						+ group_id + config.getString("id-delimiter") + removedClient.getName() + ":" + client_id
						+ config.getString("id-delimiter") + removedClient.getName() + ":" + client_id
						+ " left the group";

				broadcastToGroup(ackToGroup, group.getGroupMembers());

			} else {
				/**
				 * broadcast a msg like:
				 * "/g//d/362/i/jatin:134/i/aditya:614 removed jatin:134"
				 */
				String ackToGroup = config.getString("group-chat-delimiter") + config.getString("disconnect-delimiter")
						+ group_id + config.getString("id-delimiter") + removedClient.getName() + ":"
						+ removedClient.getUID() + config.getString("id-delimiter") + removerClient.getName() + ":"
						+ removerClient.getUID() + " removed " + removedClient.getName() + ":" + removedClient.getUID();

				broadcastToGroup(ackToGroup, group.getGroupMembers());

				/**
				 * send a msg to the removed client like:
				 * "/g//d/362/i/jatin:134/i/aditya:614 removed you"
				 */
				String ackToRemovedClient = config.getString("group-chat-delimiter")
						+ config.getString("disconnect-delimiter") + group_id + config.getString("id-delimiter")
						+ removedClient.getName() + ":" + removedClient.getUID() + config.getString("id-delimiter")
						+ removerClient.getName() + ":" + removerClient.getUID() + " removed you.";

				server.send(ackToRemovedClient.getBytes(), removedClient.getIp(), removedClient.getPort());
			}
		}
	}

	private Group getGroup(int id) {
		Group group = null;
		for (int i = 0; i < groups.size(); i++) {
			if (id == groups.get(i).getGroupID()) {
				group = groups.get(i);
				break;
			}
		}
		return group;
	}

	public ArrayList<Integer> getClientGroups(int client_id) {
		/**
		 * returns an ArrayList of all the gorupIDs client is added in,
		 * otherwise returns null
		 */
		ArrayList<Integer> clientGroups = new ArrayList<Integer>();

		Group group;
		for (int i = 0; i < groups.size(); i++) {
			group = groups.get(i);
			if (group.getGroupMembers().contains(client_id)) {
				clientGroups.add(group.getGroupID());
			}
		}

		return clientGroups;
	}

	public ArrayList<Integer> getAllGroupIDs() {
		ArrayList<Integer> groupIDs = new ArrayList<Integer>();
		for (int i = 0; i < groups.size(); i++) {
			groupIDs.add(groups.get(i).getGroupID());
		}

		return groupIDs;
	}

	private void broadcastToGroup(String msg, ArrayList<Integer> members) {
		for (int i = 0; i < connectedClients.size(); i++) {
			ConnectedClient c = connectedClients.get(i);
			if (members.contains(c.getUID())) {
				server.send(msg.getBytes(), c.getIp(), c.getPort());
			}
		}
	}

	public ArrayList<ConnectedClient> getGroupClients(int group_id) {
		ArrayList<ConnectedClient> clients = new ArrayList<ConnectedClient>();
		Group group = getGroup(group_id);
		if (group != null) {
			ArrayList<Integer> members;
			members = group.getGroupMembers();
			for (int i = 0; i < members.size(); i++) {
				clients.add(server.getConnectedClient(members.get(i)));
			}
		}
		return clients;
	}

	public void printGroupInfo(int group_id) {
		ArrayList<ConnectedClient> clients = getGroupClients(group_id);
		if (clients.size() == 0) {
			System.out.println("no gorup with ID: " + group_id);
			return;
		}
		System.out.println("Total " + clients.size() + " clients in group: " + group_id);
		System.out.println("------------------------------------");
		for (int i = 0; i < clients.size(); i++) {
			ConnectedClient c = clients.get(i);
			System.out.println(c.getName() + " [" + c.getUID() + "] @ " + c.getIp().toString() + ":" + c.getPort());
		}
		System.out.println("------------------------------------");

	}
}
