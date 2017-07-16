package com.app.client;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Login extends JFrame {

	private JPanel contentPane;
	private JTextField txtName;
	private JTextField txtIp;
	private JTextField txtPort;

	public Login() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 380);
		setLocationRelativeTo(null);
		setResizable(false);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		txtName = new JTextField();
		txtName.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String name = txtName.getText();
					String destIP = txtIp.getText();
					String destPort = txtPort.getText();
					if (!(name.equals("") || destIP.equals("") || destPort.equals(""))) {
						login(name, destIP, Integer.parseInt(destPort));
					}
				}
			}
		});

		txtName.setText("anmol");
		txtName.setBounds(67, 50, 165, 28);
		contentPane.add(txtName);
		txtName.setColumns(10);

		txtIp = new JTextField();
		txtIp.setText("localhost");
		txtIp.setColumns(10);
		txtIp.setBounds(67, 125, 165, 28);
		contentPane.add(txtIp);

		txtPort = new JTextField();
		txtPort.setText("7890");
		txtPort.setColumns(10);
		txtPort.setBounds(67, 210, 165, 28);
		contentPane.add(txtPort);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(127, 34, 45, 16);
		contentPane.add(lblName);

		JLabel lblServerIp = new JLabel("server ip:");
		lblServerIp.setBounds(103, 99, 70, 15);
		contentPane.add(lblServerIp);

		JLabel lblServerPort = new JLabel("server port:");
		lblServerPort.setBounds(103, 190, 101, 15);
		contentPane.add(lblServerPort);

		JButton btnLogin = new JButton("login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = txtName.getText();
				String destIP = txtIp.getText();
				String destPort = txtPort.getText();
				if (!(name.equals("") || destIP.equals("") || destPort.equals(""))) {
					login(name, destIP, Integer.parseInt(destPort));
				}
			}
		});
		btnLogin.setBounds(91, 311, 117, 29);
		contentPane.add(btnLogin);
	}

	private void login(String name, String destIP, int destPort) {
		dispose();
		new ClientWindow(name, destIP, destPort);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
