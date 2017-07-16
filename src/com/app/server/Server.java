package com.app.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Server implements Runnable {
	private int port;
	private DatagramSocket socket;
	private boolean running = false;
	private boolean raw = false;
	private ResourceBundle config = ResourceBundle.getBundle("com.app.config");

	Thread runServer, receive, manage, send;
	private ArrayList<ConnectedClient> connectedClients = new ArrayList<ConnectedClient>();
	private ArrayList<Integer> pingResponses = new ArrayList<Integer>();

	private int maxPacketSize = Integer.parseInt(config.getString("datagram-packet-size"));

	private GroupChatManager gcManager;

	public Server(int port) {
		gcManager = new GroupChatManager(this);
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		runServer = new Thread(this, "RunServer");
		runServer.start();
	}

	public void run() {
		running = true;
		System.out.println("Server started on port: " + this.port);
		manageClients();
		receive();
		adminCommands();
	}

	private void adminCommands() {
		Scanner scanner = new Scanner(System.in);
		while (running) {
			String command = scanner.nextLine().trim();
			if (command.startsWith("-")) {
				String message = "Server: " + command.substring(1, command.length());
				broadcast(message, true);
				System.out.println("message broadcasted");

			} else if (command.startsWith("/")) {
				command = command.substring(1, command.length());

				if (command.equals("raw")) {
					raw = !raw;
					if (raw)
						System.out.println("raw mode on");
					else
						System.out.println("raw mode off");

				} else if (command.equals("listclients")) {
					System.out.println("Total clients connected: " + connectedClients.size());
					System.out.println("------------------------------------");
					for (int i = 0; i < connectedClients.size(); i++) {
						ConnectedClient c = connectedClients.get(i);
						System.out.println(
								c.getName() + " [" + c.getUID() + "] @ " + c.getIp().toString() + ":" + c.getPort());
					}
					System.out.println("------------------------------------");

				} else if (command.equals("dropall")) {
					disconnectAll();

				} else if (command.startsWith("drop")) {
					if (connectedClients.size() == 0) {
						System.out.println("No clients to drop.");
						continue;
					}
					if (command.split(" ").length != 2) {
						System.out.println("usage: /drop [id|name]");
						continue;
					}
					String value = command.split(" ")[1];
					if (value.matches("[0-9]+")) {
						int id = Integer.parseInt(value);
						if (!disconnect(id, true, true)) {
							System.out.println("No client with id " + id);
						}
					} else {
						boolean disconnected = false;
						for (int i = 0; i < connectedClients.size(); i++) {
							if (connectedClients.get(i).getName().equals(value)) {
								disconnected = disconnect(connectedClients.get(i).getUID(), true, true);
								break;
							}
						}
						if (!disconnected) {
							System.out.println("no client of name " + value);
						}
					}

				} else if (command.equals("help")) {
					listAllCommands();

				} else if (command.equals("quit")) {
					quit();

				} else if (command.equals("groupids")) {
					ArrayList<Integer> ids = gcManager.getAllGroupIDs();
					if (ids.isEmpty()) {
						System.out.println("No groups.");
						continue;
					}
					System.out.println("All groups : " + ids.size());
					for (int i = 0; i < ids.size(); i++) {
						System.out.println(ids.get(i));
					}

				} else if (command.equals("listgroups")) {
					ArrayList<Integer> ids = gcManager.getAllGroupIDs();
					if (ids.isEmpty()) {
						System.out.println("No groups.");
						continue;
					}
					System.out.println("Total groups : " + ids.size());
					System.out.println("------------------------------------\n");
					for (int i = 0; i < ids.size(); i++) {
						gcManager.printGroupInfo(ids.get(i));
					}

				} else if (command.startsWith("groupinfo")) {
					if (command.split(" ").length != 2) {
						System.out.println("usage: /groupinfo [id]");
						continue;
					}
					String value = command.split(" ")[1];
					if (value.matches("[0-9]+")) {
						int group_id = Integer.parseInt(value);
						gcManager.printGroupInfo(group_id);

					} else {
						System.out.println("no group with ID: " + value);
					}

				} else if (command.startsWith("clientinfo")) {
					if (connectedClients.size() == 0) {
						System.out.println("No clients connected.");
						continue;
					}
					if (command.split(" ").length != 2) {
						System.out.println("usage: /clientinfo [id|name]");
						continue;
					}
					String value = command.split(" ")[1];
					if (value.matches("[0-9]+")) {
						int id = Integer.parseInt(value);
						printClientInfo(id);

					} else {
						boolean found = false;
						for (int i = 0; i < connectedClients.size(); i++) {
							if (connectedClients.get(i).getName().equals(value)) {
								found = true;
								printClientInfo(connectedClients.get(i).getUID());
							}
						}
						if (!found) {
							System.out.println("No client with name: " + value);
						}
					}

				}

				/*
				 * else if (command.startsWith("removefromgroup")) { if
				 * (command.split(" ").length != 3) { System.out.println(
				 * "usage: /removefromgroup [client id|name] [group id]");
				 * continue; } String[] values = command.split(" "); String
				 * client = values[1]; String group_id = values[2]; if
				 * (client.matches("[0-9]+")) { int client_id =
				 * Integer.parseInt(client); } }
				 */
				else {
					System.out.println("unknown command");
					listAllCommands();
				}
			}
		}
		scanner.close();
	}

	private void listAllCommands() {
		System.out.println("All commands are: ");
		System.out.println("------------------------------------------");
		System.out.println("-[message] -> broadcasts the message");
		System.out.println("/raw -> switch raw mode");
		System.out.println("/drop [id|name]-> disconnects the client");
		System.out.println("/dropall -> disconnects all clients");
		System.out.println("/listclients -> lists all connected clients");
		System.out.println("/help -> lists all commands");
		System.out.println("/quit -> terminates the server");
		System.out.println("/groupids -> prints IDs of all groups");
		System.out.println("/listgroups -> prints group info of all the groups");
		System.out.println("/groupinfo [id] -> prints group info");
		System.out.println("/clientinfo [id|name] -> prints client info and its groups info");
		System.out.println("------------------------------------------");
	}

	private void printClientInfo(int id) {
		ConnectedClient c = getConnectedClient(id);
		if (c == null) {
			System.out.println("no client with id: " + id);
			return;
		}
		System.out.println(c.getName() + " [" + c.getUID() + "] @ " + c.getIp().toString() + ":" + c.getPort());
		ArrayList<Integer> groups = gcManager.getClientGroups(id);
		System.out.println("client is added in " + groups.size() + " groups.");
		System.out.println("------------------------------------");
		for (int i = 0; i < groups.size(); i++) {
			gcManager.printGroupInfo(groups.get(i));
		}
	}

	private void manageClients() {
		manage = new Thread("Manage") {
			public void run() {
				while (running) {
					String pingMessage = config.getString("ping-delimiter") + "ping!";
					broadcast(pingMessage, false);

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					for (int i = 0; i < connectedClients.size(); i++) {
						ConnectedClient c = connectedClients.get(i);
						if (!pingResponses.contains(c.getUID())) {
							if (c.getAttempts() >= Integer.parseInt(config.getString("max-ping-attempts"))) {
								disconnect(c.getUID(), false, false);
							} else {
								c.increaseAttempts();
							}

						} else {
							pingResponses.remove(new Integer(c.getUID()));
							c.resetAttempts();
						}
					}
				}
			}
		};
		manage.start();
	}

	private void sendConnectionAck(int id, InetAddress ip, int port) {
		/*
		 * /c/100,sugandha:200,jatin:500,
		 */
		String msg = config.getString("connect-delimiter") + id + ",";
		for (int i = 0; i < connectedClients.size(); i++) {
			if (connectedClients.get(i).getUID() == id) {
				continue;
			}
			// msg = msg.concat(connectedClients.get(i).getName()).concat(",");
			msg = msg + connectedClients.get(i).getName() + ":" + connectedClients.get(i).getUID() + ",";
		}
		send(msg.getBytes(), ip, port);
	}

	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while (running) {
					byte[] data = new byte[maxPacketSize];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (SocketException e) {
					} catch (IOException e) {
						e.printStackTrace();
					}
					processPacket(packet);
				}
			}
		};
		receive.start();
	}

	private void processPacket(DatagramPacket packet) {
		String message = new String(packet.getData());
		if (raw)
			System.out.println(message);

		String connect_delimiter = config.getString("connect-delimiter");

		/**
		 * Add the client and send back its Unique ID as connection
		 * acknowledgement
		 */
		if (message.startsWith(connect_delimiter)) {
			int id = UniqueIdentifier.getUniqueClientIdentifier();
			String name = message.substring(connect_delimiter.length(), message.length()).trim();

			connectedClients.add(new ConnectedClient(name, packet.getAddress(), packet.getPort(), id));

			sendConnectionAck(id, packet.getAddress(), packet.getPort());

			// String returnMsgToClient = connect_delimiter + id;
			// send(returnMsgToClient.getBytes(), packet.getAddress(),
			// packet.getPort());

			String clientAddedMsgToAll = config.getString("online-users-delimiter") + name + ":" + id;
			broadcast(clientAddedMsgToAll, false);

			System.out.println("client " + name + " [" + id + "] connected");

		} else if (message.startsWith(config.getString("broadcast-delimiter"))) {
			broadcastIfClientConnected(message.trim());

		} else if (message.startsWith(config.getString("disconnect-delimiter"))) {
			processDisconnectMessage(message.trim());

		} else if (message.startsWith(config.getString("ping-delimiter"))) {
			int client_id = Integer
					.parseInt(message.substring(config.getString("ping-delimiter").length(), message.length()).trim());
			pingResponses.add(client_id);

		} else if (message.startsWith(config.getString("group-chat-delimiter"))) {
			message = message.substring(config.getString("group-chat-delimiter").length(), message.length()).trim();
			gcManager.process(message);
		}
	}

	private void processDisconnectMessage(String message) {
		/**
		 * receives a msg like(if client is connected to groups):
		 * "/d/123/i/507,157,844," OR receives a msg like (if client is not in
		 * any group): "/d/123/i/"
		 */

		String d_d = config.getString("disconnect-delimiter");
		String id_d = config.getString("id-delimiter");

		String[] arr = message.split(d_d + "|" + id_d);
		int client_id = Integer.parseInt(arr[1]);
		if (arr.length == 2) {
			disconnect(client_id, true, false);
			return;
		}

		String[] groupIDs = arr[2].split(",");
		for (int i = 0; i < groupIDs.length; i++) {
			gcManager.disconnect(Integer.parseInt(groupIDs[i]), client_id, false);
		}
		disconnect(client_id, true, false);
	}

	private void broadcastIfClientConnected(String message) {
		String b_d = config.getString("broadcast-delimiter");
		String id_d = config.getString("id-delimiter");

		/**
		 * if message = "/b/152/i/hey man" then msgArr[0] = "", msgArr[1] =
		 * "152", msgArr[2] = "hey man"
		 */
		String[] msgArr = message.split(b_d + "|" + id_d);
		int client_id = Integer.parseInt(msgArr[1]);
		String client_message = msgArr[2];

		for (int i = 0; i < connectedClients.size(); i++) {
			if (connectedClients.get(i).getUID() == client_id) {
				client_message = connectedClients.get(i).getName() + ": " + client_message;
				broadcast(client_message, true);
				break;
			}
		}
	}

	private void broadcast(String message, boolean attachBroadcastDelimiter) {
		if (attachBroadcastDelimiter) {
			message = config.getString("broadcast-delimiter") + message;
		}

		for (int i = 0; i < connectedClients.size(); i++) {
			send(message.getBytes(), connectedClients.get(i).getIp(), connectedClients.get(i).getPort());
		}
	}

	public void send(byte[] data, InetAddress ip, int port) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				try {
					socket.send(packet);
				} catch (SocketException e) {
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	private boolean disconnect(int id, boolean status, boolean sendAck) {
		ConnectedClient c = null;
		boolean found = false;
		for (int i = 0; i < connectedClients.size(); i++) {
			if (connectedClients.get(i).getUID() == id) {
				c = connectedClients.get(i);
				// connectedClients.remove(i);
				found = true;
				break;
			}
		}
		if (!found) {
			return false;
		}

		/**
		 * Here it is disconnecting client from all the groups client is added
		 * in. If client has sent the disconnect request then client itself
		 * sends all it's groups ids and <processDisconnectMessage> function
		 * gets executed first which disconnects clients from all the groups and
		 * hence <clientGroups> arraylist here will be empty and code in the
		 * <if> condition will not be executed. But if client has been timed out
		 * or the server has dropped the client then in those cases client would
		 * not be able to send all it's group ids. Then server has to disconnect
		 * the client from all the groups and hence below code will be exucuted.
		 */
		ArrayList<Integer> clientGroups = gcManager.getClientGroups(id);
		if (!clientGroups.isEmpty()) {
			for (int i = 0; i < clientGroups.size(); i++) {
				gcManager.disconnect(clientGroups.get(i), id, sendAck);
			}
		}

		connectedClients.remove(c);

		String message;
		if (status) {
			message = "client " + c.getName() + " [" + c.getUID() + "] @ " + c.getIp().toString() + ":" + c.getPort()
					+ " disconnected.";

		} else {
			message = "client " + c.getName() + " [" + c.getUID() + "] @ " + c.getIp().toString() + ":" + c.getPort()
					+ " timed out.";
		}
		System.out.println(message);
		broadcast(message, true);

		String updateUsersBroadcast = config.getString("disconnect-delimiter") + c.getName() + ":" + c.getUID();
		broadcast(updateUsersBroadcast, false);

		if (sendAck) {
			String ack = config.getString("disconnect-delimiter");
			send(ack.getBytes(), c.getIp(), c.getPort());
		}

		return true;
	}

	private void disconnectAll() {

		while (!connectedClients.isEmpty()) {
			ConnectedClient c = connectedClients.get(0);
			disconnect(c.getUID(), true, true);
		}
	}

	public void quit() {
		disconnectAll();
		running = false;
		socket.close();
		System.out.println("Server down!");
	}

	public int getPort() {
		return this.port;
	}

	public ConnectedClient getConnectedClient(int id) {
		ConnectedClient c = null;

		for (int i = 0; i < connectedClients.size(); i++) {
			if (id == connectedClients.get(i).getUID()) {
				c = connectedClients.get(i);
				break;
			}
		}
		return c;
	}

	public ArrayList<ConnectedClient> getConnectedClients() {
		return connectedClients;
	}
}
