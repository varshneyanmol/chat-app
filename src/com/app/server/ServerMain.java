package com.app.server;

import java.util.ResourceBundle;

public class ServerMain {
	private Server server;
	
	public ServerMain(int port) {
		server = new Server(port);
	}
	
	public static void main(String[] args) {
		int port;
		if (args.length > 1) {
			System.out.println("usage: java -jar ChatServer.jar [port]");
			return;
			
		} else if (args.length < 1) {
			ResourceBundle config = ResourceBundle.getBundle("com.app.config");
			port = Integer.parseInt(config.getString("default-server-port"));
			
		} else {
			port = Integer.parseInt(args[0]);
		}
		new ServerMain(port);
	}
}
