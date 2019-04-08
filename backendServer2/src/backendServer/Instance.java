package backendServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.sql.*;

import org.apache.commons.lang3.StringUtils;

public class Instance extends Thread{
	
	private final Socket clientSocket;

	private String username = null;
	private final Connection server;
	private OutputStream oStream;
	
	public Instance(Connection server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
	}
	
	@Override
	public void run() {
		try {
			clientSocketConnect();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void clientSocketConnect() throws IOException, InterruptedException {
		InputStream iStream = clientSocket.getInputStream();
		this.oStream = clientSocket.getOutputStream();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
		String line;
		while ( (line = reader.readLine()) != null) {
			String[] part = StringUtils.split(line);
			if(part !=null && part.length > 0) {
				String cmd = part[0];
				if ("logoff".equalsIgnoreCase(cmd)) {
					handleLogoff();
					break;
				}else if("login".equalsIgnoreCase(cmd)) {
					clientLogin(oStream, part);
				}else if("msg".equalsIgnoreCase(cmd)){
					String[] tokensMsg = StringUtils.split(line, null, 3);
					handleMessage(tokensMsg);
					}else {
					String msg = "unknown " + cmd + "\n";
					oStream.write(msg.getBytes());
				}
				String msg = "\nYou typed: " + line + "\n";
				oStream.write(msg.getBytes());
				
			}
		}
		oStream.close();
	}
	private void handleMessage(String[] tokens) throws IOException {
		String sendTo = tokens[1];
		String body = tokens[2]; 
		
		List<Instance> workerList = server.getWorkerList();
		for(Instance tom : workerList) {
				if (sendTo.equalsIgnoreCase(tom.getLogin())) {
					//compiles message to that user
					String outMsg = "msg " + username + " " + body + "\n";
					//sends to outMsg method
					tom.send(outMsg);
				}	
		}
		
	}
	private void handleLogoff() throws IOException{
		List<Instance> workerList = server.getWorkerList();
		
		String onLineMsg = "Offline " + username + "\n";
		for(Instance tom : workerList) {
			if (!username.equals(tom.getLogin())) {
				tom.send(onLineMsg);
			}
		}
		
		System.out.print("User: " + username + " logged off!");
		oStream.close();
	}
	
	public String getLogin() {
		return username;
	}
	
	private void clientLogin(OutputStream oStream, String[] parts) throws IOException {
		if(parts.length == 3) {
			String login = parts[1];
			String password = parts[2];
			
			try {
			Class.forName("com.mysql.cj.jdbc.Driver");  
			Connection con=DriverManager.getConnection(  
			"jdbc:mysql://localhost:3307/messengerdata","root","password");  
			//connecting through port 3307 to the LOCAL mysql database, using credentials root for username and password for password
			Statement stmt=con.createStatement();
			
			ResultSet us=stmt.executeQuery("select password from userpass where username = '" + login + "'");
			while(!us.next()) 
			private String username = (login);
			private String dbPass = (us.getString(1));

			}catch(Exception e){ System.out.println(e);}
			
			
			if ((login.equals(username) && password.equals(dbpass)) || (login.equals("admin") && password.equals("password")) || (login.equals("nathan") && password.equals("password"))) {
				String msg = "ok login";
				oStream.write(msg.getBytes());
				this.username = login;
				System.out.println("User logged in: " + login);
				
				List<Instance> workerList = server.getWorkerList();
				
				for(Instance client : workerList) {
					if (client.getLogin() != null) {
						if (!login.equals(client.getLogin())) {
							String msg2 = "\nOnline " + client.getLogin() + "\n";
							send(msg2);
						}
					}
				}
				String onLineMsg = "online " + login + "\n";
				for(Instance client : workerList) {
					if (!login.equals(client.getLogin())) {
						client.send(onLineMsg);
					}
				}
			}else {
				String msg = "error login";
				oStream.write(msg.getBytes());
				System.err.println("Login failed for " + login);
			}
		}
	}
	private void send(String msg) throws IOException {
		if (username != null) {
		oStream.write(msg.getBytes());
		}
	}
}
