package com.app.server;

import java.net.InetAddress;

public class ConnectedClient {
	private String name;
	private InetAddress ip;
	private int port;
	private final int ID;
	private int attempts = 0;

	public ConnectedClient(String name, InetAddress ip, int port, int ID) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.ID = ID;
	}

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public InetAddress getIp() {
		return ip;
	}

	public int getUID() {
		return ID;
	}

	public int getAttempts() {
		return attempts;
	}

	public void resetAttempts() {
		this.attempts = 0;
	}

	public void increaseAttempts() {
		this.attempts++;
	}
}
