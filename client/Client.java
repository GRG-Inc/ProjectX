package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread {
	
	private Socket socket;
	
	private static BufferedReader in,in1;
	private static PrintWriter out;
	private String colour;
	private AI ai;
	public Client(String serverAddress, int port) throws Exception {
		super("Client");
		// Setup networking
		socket = new Socket(serverAddress, port);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		ai=  new AI();
	}

	public void play() throws Exception {
		String response;
		Scanner sc = new Scanner(System.in);
		try {
			response = in.readLine();
			if (response.startsWith("WELCOME")) {
				colour = response.substring(8);	
				System.out.println(colour);
			}
			while (true) {
				response = in.readLine();
				if (response.startsWith("VALID_MOVE")) {
					System.out.println("Valid move, please wait");
				} else if (response.startsWith("OPPONENT_MOVE")) {
					ai.convertiStringaMossa(response.substring(14));
					System.out.println("Opponent move: " + response.substring(14));					
				} else if (response.startsWith("VICTORY")) {
					System.out.println("You win");
					break;
				} else if (response.startsWith("DEFEAT")) {
					System.out.println("You lose");
					break;
				} else if (response.startsWith("TIE")) {
					System.out.println("You tied");
					break;
				} else if (response.startsWith("YOUR_MOVE")) {
					System.out.println("Your move: ");
					String move = ai.generaProssimaMossa(ai.getScacchiera(), colour, 3);// sc.next();
					out.println("MOVE "+move);
				} else if (response.startsWith("TIMEOUT")) {
					System.out.println("Time out");
					System.exit(1);
				} else if (response.startsWith("MESSAGE")) {
					System.out.println(response.substring(8));
				}
			}
		} finally {
			sc.close();
			socket.close();
		}
	}
	
	public void run(){
		try {
			play();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Runs the client as an application.
	 */
	public static void main(String[] args) throws Exception {
		//String serverAddress = (args.length == 0) ? "localhost" : args[0];
		String serverAddress = "160.97.25.82";
		int serverPort = (args.length == 0) ? 8901 : Integer.parseInt(args[1]);
		Client client = new Client(serverAddress, serverPort);
		client.start();
//		in1 = new BufferedReader(new InputStreamReader(System.in));
//		long startTime, endTime;
//		String mossa;
//		Scacchiera s = ai.getScacchiera();
//		s.stampa(s.getScacchiera());
//		ai.distanza();
//		while(true){
//			System.out.println("Inserire mossa>> ");
//			mossa = in1.readLine();
//			startTime = System.nanoTime();
//			ai.convertiStringaMossa(mossa); 
//			s.stampa(s.getScacchiera());
//			//endTime = System.nanoTime();
//			System.out.println("\nPedine nere mangiate:    " + s.getNereCatturate());
//			System.out.println("Pedine bianche mangiate: " + s.getBiancheCatturate() + "\n");
//			//System.out.println("Ho finito in : " + (endTime-startTime) + " ns." );
//			//startTime = System.nanoTime();
//			System.out.println("Mo ce pens.... \n");
//			mossa = ai.generaProssimaMossa(s,"bianco",6);
//			ai.convertiStringaMossa(mossa);
//			s.stampa(s.getScacchiera());
//			endTime = System.nanoTime();
//			System.out.println("La mossa generata è: " + mossa + "\n");
//			System.out.println("\nPedine nere mangiate:    " + s.getNereCatturate());
//			System.out.println("Pedine bianche mangiate: " + s.getBiancheCatturate() + "\n");
//			System.out.println("Ho finito di elaborare la mossa in: " + (endTime-startTime) + " ns." );
//			System.out.println("Il numero di mose generate è : " + ai.getNumMosse());
//		}
	}
}
