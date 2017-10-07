/*
 * Evangelos Aristodemou
 * Christos Vasiliou 
 */

import java.io.*;
import java.net.*;
import java.util.Date;

public class Client implements Runnable {

	private static final int USERS = 1;
	private static final int MESSAGES_PER_USER = 5;
	private static double RTT[] = new double[USERS];
	private static int working_users;
	private static String serverip;
	private static int port;
	private int userid;
	private String message = "";
	private String received = "";

	public Client(int userid) {
		this.userid = userid;
		this.message = "";
		this.received = "";
	}

	@Override
	public void run() {
		try {
			Socket socket = new Socket(serverip, port);
			DataOutputStream output = new DataOutputStream(
					socket.getOutputStream());
			BufferedReader input = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			int i;
			double sum = 0;
			this.message = "HELLO, " + socket.getLocalAddress() + ", " + socket.getLocalPort() + ", user" + this.userid;
			for (i = 0; i < MESSAGES_PER_USER; i++) {
				double time_before = System.currentTimeMillis();
				output.writeBytes(message + '\n');
				received = input.readLine();
				double time_after = System.currentTimeMillis();
				String tokens[] = received.split(" ");
				System.out.println("[" + new Date() + "] Message from server: "
						+ tokens[0] + " " + tokens[1] + " Payload: " + (tokens[2].length()*2)/1024 + "KB");
				double total_time = time_after - time_before;
				sum += total_time;
			}
			RTT[this.userid] = sum / i;
			working_users-=1;
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void printResults(){
		while (working_users != 0) {System.out.print("");}
		System.out.println("***********************************************************");
		System.out.println("Average Round Trip Times per user:");
		int sum = 0;
		for (int i = 0; i < USERS; i++) {
			System.out.println("User" + i + ": " + RTT[i] + " ms");
			sum+=RTT[i];
		}
		double avg = sum/USERS;
		System.out.println("Average Latence: "+ avg + " ms");
		
	}

	public static void main(String argv[]) throws Exception {
		working_users=USERS;
		try{
			serverip=argv[0];
			port = Integer.parseInt(argv[1]);
		}catch(Exception e){
			System.err.println("Wrong arguments. Correct syntax: java Client <server_ip> <port_number> <repetitions>");
			System.exit(1);
		}
		
		for (int i = 0; i < USERS; i++) {
			(new Thread(new Client(i))).start();
		}
		printResults();

	}
}
