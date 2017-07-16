package com.app.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class Client {

	private String name;
	private String destIP_Str;
	private int destPort;
	private InetAddress destIP;
	private DatagramSocket socket;

	private Thread send;

	private int ID = -1;

	private ResourceBundle config = ResourceBundle.getBundle("com.app.config");
	private int maxPacketSize = Integer.parseInt(config.getString("datagram-packet-size"));

	public Client(String name, String destIP_Str, int destPort) {
		this.name = name;
		this.destIP_Str = destIP_Str;
		this.destPort = destPort;
	}

	public boolean openConnection() {

		try {
			socket = new DatagramSocket(); // Opens a sockets at any
											// available port
			this.destIP = InetAddress.getByName(this.destIP_Str);
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String receive() {
		byte[] data = new byte[maxPacketSize];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String message = new String(packet.getData());
		return message;
	}

	public void send(byte[] data) {
		send = new Thread("Send_Client") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, destIP, destPort);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	public void close() {
		new Thread() {
			public void run() {
				synchronized (socket) {
					socket.close();
				}
			}
		};
	}

	public String getName() {
		return name;
	}

	public String getDestIP_Str() {
		return destIP_Str;
	}

	public int getDestPort() {
		return destPort;
	}

	public void setID(int id) {
		this.ID = id;
	}

	public int getID() {
		return ID;
	}

}
