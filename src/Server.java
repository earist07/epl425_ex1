import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	
	private static int port; 
	private static int messages;
	private static int repetitions; 
	private static double start_time;
	
	private static class TCPWorker implements Runnable {

		
		private Socket client;
		private String clientbuffer;

		public TCPWorker(Socket client) {
			this.client = client;
			this.clientbuffer = "";
		}
		
		@Override
		public void run() {
			try {
				while (this.client.isConnected()) {
					//System.out.println("Client connected with: "+ this.client.getInetAddress());

					DataOutputStream output = new DataOutputStream(
							this.client.getOutputStream());
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(this.client.getInputStream()));

					this.clientbuffer = reader.readLine();
					if (messages ==0){
						System.out.println("Time started.");
						start_time = System.currentTimeMillis();
					}
					String tokens[] = this.clientbuffer.split(", ");
					System.out.println("[" + new Date() + "] Received: "
							+ this.clientbuffer);
					Random num = new Random(System.nanoTime());
					int n = num.nextInt(2000-300) + 300;
					n*=1024;
					System.out.println("Starting building a payload: "+ n/1024 + "KB");
					String payload = createPayload(n);
				 	System.out.println("Payload created");
					
					String reply = "Welcome " + tokens[3] + " " + payload;
					System.out.println("Sending the reply.");
					output.writeBytes(reply + System.lineSeparator());
					System.out.println("Reply sent");
					messages+=1;
					if (messages==repetitions){
						double end_time = System.currentTimeMillis();
						System.out.println("Messages reached: "+ messages);
						double total_time = end_time-start_time;
						System.out.println("Total time: " + total_time);
						System.out.println("Throughput: " + messages/(total_time/1000) + " mes/sec");
						
					}
					output.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
	
	private static String createPayload(int size){
		char[] payload = new char[size/2];
		Arrays.fill(payload, 'a');
		String str = new String(payload);
		return str; 
	}
	

	public static ExecutorService TCP_WORKER_SERVICE = Executors
			.newFixedThreadPool(10);

	public static void main(String argv[]) {
		messages = 0;
		try{
			repetitions = Integer.parseInt(argv[1]);
			port = Integer.parseInt(argv[0]);
		}catch(Exception e){
			System.err.println("Wrong arguments. Correct syntax: java Server <port_number>");
			System.exit(1);
		}
		try {
			ServerSocket socket = new ServerSocket(port);

			System.out.println("Server listening to: "
					+ socket.getInetAddress() + ":" + socket.getLocalPort());

			while (true) {
				Socket client = socket.accept();

				TCP_WORKER_SERVICE.submit(new TCPWorker(client));

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}