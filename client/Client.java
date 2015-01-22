package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
//import java.util.Map;
import java.util.Scanner;

public class Client {
	
	public static Integer dist;
	private static HashMap<Integer, Integer> distance = new HashMap<Integer, Integer>();
	private Socket socket;
	private String colour;
	private static int[] mossa = new int[8];
	private static BufferedReader in,in1;
	private static PrintWriter out;
	static final byte bianco=2, nero=3;
	private static int nereCatturate=0, biancheCatturate=0;//nereCatturate e' il numero di pedine perse da side1
	
	public final static int[] minColumn = { 1, 1, 1, 1, 1, 1, 2, 3, 4, 5}; //da che colonna inizia la scacchiera per ogni riga compresa cornice
	public final static int[] maxColumn = { 5, 5, 6, 7, 8, 9, 9, 9, 9, 9}; //a che colonna finisce la scacchiera per ogni riga compresa cornice
	private static byte[] CostiCattura = { 0, 4, 6, 8, 12, 18, 100 };
	
//	private Thread attesaMessaggi;
//	private Thread operazioni;
	//0= casella non valida, 1=casella vuota, 2=pedina bianca, 3=pedina nera
	private static byte[][] scacchiera = 
		  { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 2, 2, 1, 3, 3, 0, 0, 0, 0, 0 }, // A
			{ 0, 2, 2, 2, 3, 3, 3, 0, 0, 0, 0 }, // B
			{ 0, 1, 2, 2, 1, 3, 3, 1, 0, 0, 0 }, // C
			{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 }, // D
			{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, // E
			{ 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, // F
			{ 0, 0, 0, 1, 3, 3, 1, 2, 2, 1, 0 }, // G
			{ 0, 0, 0, 0, 3, 3, 3, 2, 2, 2, 0 }, // H
			{ 0, 0, 0, 0, 0, 3, 3, 1, 2, 2, 0 }, // I
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };
//	private Map distanze;
	
	public Client(String serverAddress, int port) throws Exception {

		// Setup networking
		socket = new Socket(serverAddress, port);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
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
					
					System.out.println("Opponent move: " + response.substring(14));
					//String mossaAvversario = response.substring(14);
					//aggiornaScacchiera(mossaAvversario, scacchiera);
					int depth = 3; //da configurare in base al tempo disponibile per generare la mossa
					String mossa = generaProssimaMossa(this.scacchiera, colour, depth);
					
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
					//trasmetti(mossa);
					String move = sc.next();
					out.println("MOVE "+move);
				} else if (response.startsWith("TIMEOUT")) {
					System.out.println("Time out");
				} else if (response.startsWith("MESSAGE")) {
					System.out.println(response.substring(8));
				}
			}
		} finally {
			sc.close();
			socket.close();
		}
	}
	
	public static String generaProssimaMossa(byte[][] s, String side, int d){
		
		String m1 = "";
		valutaMossa(s, side, d, Double.NEGATIVE_INFINITY);
		for(int i=0; i<8; i++){
			if(i%2==0)
				m1+=corrispondenzaR(mossa[i]);
			else
				m1+=mossa[i];
		}
		return m1;
	}
	
	private static char corrispondenzaR(int indice) {
		switch(indice){
			case 1:
				return 'A';
			case 2:
				return 'B';
			case 3:
				return 'C';
			case 4:
				return 'D';
			case 5:
				return 'E';
			case 6:
				return 'F';
			case 7:
				return 'G';
			case 8:
				return 'H';
			case 9:
				return 'I';	
			default:
				throw new IllegalStateException("Entrato in caso default dello switch"+ indice);
		}
	}

	public static double valutaMossa(byte[][] scacchiera2, String s, int depth, double alfabeta) {
		double bestValue = Double.POSITIVE_INFINITY, currValue;
		byte s1, s2;
		if(s.equalsIgnoreCase("bianco")){
			s1 = bianco;
			s2 = nero;
		}
		else{
			s1 = nero;
			s2 = bianco;
		}
			
		if(depth == 0){
			//assegna valore a configurazione corrente
			double w1 = 1,w2 = 1,w3 = 1; //pesi
			double centerDist = 0, coesione = 0, premioCatt = 0, penaleCatt = 0;
			if(s1==2){
				premioCatt = CostiCattura[nereCatturate];
				penaleCatt = CostiCattura[biancheCatturate];
			}else{
				premioCatt = CostiCattura[biancheCatturate];
				penaleCatt = CostiCattura[nereCatturate];
			}
			for(int i = 1; i<10; i++)
				for(int j = minColumn[i]; j <= maxColumn[i]; j++){
					if(scacchiera2[i][j] == s1){
						centerDist += 0.1;
						coesione += calcolaCoesione(scacchiera2,i,j);
					}else if(scacchiera2[i][j] == s2){
						centerDist -= 0.1;
						coesione -= calcolaCoesione(scacchiera2,i,j);
					}
				}
			return w1*centerDist + w2*coesione + w3*premioCatt + w3*penaleCatt;
		}else{
			//genera configurazione futura
			byte[][] scacFutura;
			for(int i=1; i<10; i++){
				for(int j = minColumn[i]; j<=maxColumn[i]; j++){
					if(scacchiera2[i][j]==s1){
						if(scacchiera2[i+1][j]==1){//Movimento a Nord e cella libera
							scacFutura = scacchiera2.clone();
									
						}
					}
				}
			}
			
			return 0.0;
		}
	}

	private static double calcolaCoesione(byte[][] copia, int i, int j) {
		double val = 0;
		byte side = copia[i][j];
		if(copia[i-1][j] == side)//Nord
			val+= 0.1;
		if(copia[i-1][j-1] == side)//Nord-Ovest
			val+= 0.1;
		if(copia[i][j-1] == side)//Ovest
			val+= 0.1;
		if(copia[i][j+1] == side)//Est
			val+= 0.1;
		if(copia[i+1][j] == side)//Sud
			val+= 0.1;
		if(copia[i+1][j+1] == side)//Sud-Est
			val+= 0.1;
		
		return val;
	}
	
	public static void convertiStringaMossa(String mossa){
		char[] vm = mossa.toCharArray();
		int i = corrispondenza(vm[0]);//indice posizione di partenza della prima pedina del gruppo
		int k = corrispondenza(vm[2]);//indice posizione di partenza dell'ultima pedina del gruppo
		int j = corrispondenza(vm[4]);//indice posizione di arrivo della prima pedina del gruppo
		int l = corrispondenza(vm[6]);//indice posizione di arrivo dell'ultima pedina del gruppo
		aggiornaScacchiera(i,Character.getNumericValue(vm[1]),k,Character.getNumericValue(vm[3]),j,Character.getNumericValue(vm[5]),l,Character.getNumericValue(vm[7]));
	}

	public static void aggiornaScacchiera(int origP1, int origP2, int origU1, int origU2, int destP1, int destP2, int destU1, int destU2){
		
		int numeroPedine;//numero di pedine da muovere
		boolean dir1, dir2; //dir1 false=destra true=sinistra, dir2 false=alto true=basso
		
		if(origP2 - origU2!=0)
			numeroPedine = Math.abs(origP2 - origU2) + 1; //RIGA PEZZ'I MMERDA(POTREBBE GENERARE ERRORE!!!!!!!!!!)
		else
			numeroPedine = Math.abs(origP1-origU1) + 1;
		
		System.out.println("numero pedine mosse: " + numeroPedine);
		byte side1 = getSide(origP1, origP2);
		if(numeroPedine == 1){
			scacchiera[destP1][destP2] = side1;
			scacchiera[origP1][origP2] = 1;
			return;
		}
		if(origP1 == destP1 && origU1 == destU1){//movimento orizzontale
			System.out.println("movimento orizzontale");
			if(origP1 == origU1){//movimento sulla stessa riga
				if((origP2 - destP2) < 0 ){//movimento verso destra
					dir1=false;
					if(scacchiera[origP1][destU2] == 1){//cella successiva vuota
						scacchiera[origP1][destU2] = side1;
						scacchiera[origP1][origP2] = 1;
					}else if(scacchiera[origP1][destU2 + 1] == 1){
						spostaRiga(dir1,origP1,origP2, destU1, destU2);
					}else if(scacchiera[origP1][destU2 + 1] == 0){
						attacca(origP1,origP2, destU1, destU2);
					}else if(scacchiera[origP1][destU2 + 2] == 1){
						spostaRiga2(dir1,origP1,origP2, destU1, destU2);
					}else if(scacchiera[origP1][destU2 + 2] == 0)
						attacca(origP1,origP2, destU1, destU2);
				}else {//movimento verso sinistra
					dir1=true;
					if(scacchiera[origP1][destP2] == 1){//cella successiva vuota
						scacchiera[origP1][origU2] = 1;
						scacchiera[origP1][destP2] = side1;
					}else if(scacchiera[origP1][destP2 - 1] == 1){
						spostaRiga(dir1,origU1,origU2,destP1,destP2);
					}else if(scacchiera[origP1][destP2 - 1] == 0){
						attacca(origU1,origU2,destP1,destP2);
					}else if(scacchiera[origP1][destP2 - 2] == 1){
						spostaRiga2(dir1,origU1,origU2,destP1,destP2);
					}else if(scacchiera[origP1][destP2 - 2] == 0){
						attacca(origU1,origU2,destP1,destP2);
					}						
				}
			}else if (origP2 == origU2){//movimento di pedine allineate verticalmente
				System.out.println("pedine allineate verticalmente");
				traslaColonna(numeroPedine, origP1, origP2, destP1, destP2);
			}else{
				//TODO
			}
		}else if(origP2 == destP2 && origU2 == destU2){
			System.out.println("mov verticale");//movimento verticale
			if(origP2 == origU2){//movimento sulla stessa colonna
				if(origP1 > destP1 ){//movimento verso l'alto 
					dir2=false;
					if(scacchiera[destP1][destP2] == 1){
						scacchiera[origU1][destP2] = 1;
						scacchiera[destP1][destP2] = side1;
					}else if(scacchiera[destP1-1][destP2] == 1){
						spostaColonna(dir2,origU1,origU2,destP1,destP2);
					}else if(scacchiera[destP1-1][destP2] == 0){
						attacca(origU1,origU2,destP1,destP2);
					}else if(scacchiera[destP1-2][destP2] == 1){
						spostaColonna2(dir2,origU1,origU2,destP1,destP2);
					}else if(scacchiera[destP1-2][destP2] == 0){
						attacca(origU1,origU2,destP1,destP2);
					}
				}else{//movimento verso il basso
					dir2=true;
					if(scacchiera[destU1][origP2] == 1){
						scacchiera[origP1][origP2] = 1;
						scacchiera[destU1][origP2] = side1;
					}else if(scacchiera[destU1+1][origP2] == 1){
						spostaColonna(dir2, origP1, origP2, destU1, destU2);
					}else if(scacchiera[destU1+1][origP2] == 0){
						attacca(origP1, origP2,destU1,destU2);
					}else if(scacchiera[destU1+2][origP2] == 1){
						spostaColonna2(dir2, origP1, origP2, destU1, destU2);
					}else if(scacchiera[destU1+2][origP2] == 0){
						attacca(origP1,origP2,destU1,destU2);
					}
				}
			}else if(origP1 == origU1){//movimento di pedine allineate orizzontalmente 
				System.out.println("trasla pedine allineate orizzontalmente");
				traslaRiga(numeroPedine, origP1, origP2, destP1, destP2);
			}else{
				//FIXME
			}
		}else if(origP1 < destP1 && origU1 < destU1 && origP2 < destP2 && origU2 < destU2){//movimento sud-est
			System.out.println("movimento SE");
			if(origP1 == origU1){//pedine allineate orizzontalmente(singola o gruppo?)
				traslaRiga(numeroPedine,origP1,origP2,destP1,destP2);
			}else if(origP2 == origU2){//pedine allineate verticalmente (singola o gruppo?)
				traslaColonna(numeroPedine,origP1,origP2,destP1,destP2);
			}else if(origP2 < origU2){//movimento pedine allineate oblique dall'alto verso il basso (cosi \)
				if(scacchiera[destU1][destU2] == 1){
					scacchiera[origP1][origP2] = 1;
					scacchiera[destU1][destU2] = side1;
				}else if(scacchiera[destU1+1][destU2 + 1] == 1){
					spostaSE(origP1,origP2,destU1,destU2);
				}else if(scacchiera[destU1+1][destU2 + 1] == 0){
					attacca(origP1,origP2,destU1,destU2);
				}else if(scacchiera[destU1+2][destU2 + 2] == 1){
					spostaSE2(origP1,origP2,destU1,destU2);
				}else if(scacchiera[destU1+2][destU2 + 2] == 0){
					attacca(origP1,origP2,destU1,destU2);
					}
				}
		}else if(origP1 > destP1 && origU1 > destU1 && origP2 > destP2 && origU2 > destU2){//movimento nord-ovest
			System.out.println("movimento NO");
			if(origP1 == origU1){//pedine allineate orizzontalmente(singola o gruppo?)
				traslaRiga(numeroPedine, origP1, origP2, destP1, destP2);
			}else if(origP2 == origU2){//pedine allineate verticalmente (singola o gruppo?)
				traslaColonna(numeroPedine, origP1, origP2, destP1, destP2);
			}else if(origP2 < origU2){//movimento pedine allineate oblique dal basso verso l'alto(cosi \)
				if(scacchiera[destP1][destP2] == 1){
					scacchiera[origU1][origU2] = 1;
					scacchiera[destP1][destP2] = side1;
				}else if(scacchiera[destP1-1][destP2 - 1] == 1){
					spostaNO(origU1,origU2,destP1,destP2);
				}else if(scacchiera[destP1-1][destP2 - 1] == 0){
					attacca(origU1,origU2,destP1,destP2);
				}else if(scacchiera[destP1-1][destP2 - 1] == 1){
					spostaNO2(origU1,origU2,destP1,destP2);
				}else if(scacchiera[destP1-1][destP2 - 1] == 0){
					attacca(origU1,origU2,destP1,destP2);
					}
				}
			}
	}

	private static void traslaColonna(int numeroPedine, int i, int n1, int j, int n2) {
		System.out.println("trasla colonna");
		byte side = getSide(i, n1);
		scacchiera[j][n2] = side;
		scacchiera[j+1][n2] = side;
		scacchiera[i][n1] = 1;
		scacchiera[i+1][n1] = 1;
		if(numeroPedine == 3){
			scacchiera[j+2][n2] = side;
			scacchiera[i+2][n1] = 1;
		}
	}

	private static void traslaRiga(int numeroPedine, int i, int n1, int j, int n2) {
		System.out.println("trasla riga");
		byte side = getSide(i, n1);
		scacchiera[j][n2] = side;
		scacchiera[j][n2+1] = side;
		scacchiera[i][n1] = 1;
		scacchiera[i][n1+1] = 1;
		if(numeroPedine == 3){
			scacchiera[j][n2+2] = side;
			scacchiera[i][n1+2] = 1;
		}
	}

	private static void attacca(int k, int n1, int j, int n2) {
		byte side=getSide(k, n1);
		cattura(side);
		scacchiera[j][n2] = side;
		scacchiera[k][n1] = 1;
	}

	private static void spostaNO2(int k, int n1, int j, int n2) {
		scacchiera[j][n2]=getSide(k, n1);
		scacchiera[j-2][n2-2]=getOppSide(k, n1);
		scacchiera[k][n1]=1;	
	}
	
	private static void spostaNO(int k, int n1, int j, int n2) {
		scacchiera[j][n2]=getSide(k, n1);
		scacchiera[j-1][n2-1]=getOppSide(k, n1);
		scacchiera[k][n1]=1;	
	}

	private static void spostaSE2(int i, int n1, int l, int n2) {
		scacchiera[l][n2]=getSide(i, n1);
		scacchiera[l+2][n2+2]=getOppSide(i, n1);
		scacchiera[i][n1]=1;
	}

	private static void spostaSE(int i, int n1, int l, int n2) {
		scacchiera[l][n2]=getSide(i, n1);
		scacchiera[l+1][n2+1]=getOppSide(i, n1);
		scacchiera[i][n1]=1;
	}

	private static void spostaColonna2(boolean dir2, int k, int n1, int j, int n2) {
		if(dir2){
			scacchiera[j][n2]=getSide(k, n1);
			scacchiera[j+2][n2]=getOppSide(k, n1);
			scacchiera[k][n1]=1;
		}else{
			scacchiera[j][n2]=getSide(k, n1);
			scacchiera[j-2][n2]=getOppSide(k, n1);
			scacchiera[k][n1]=1;
		}
	}

	private static void spostaColonna(boolean dir2, int k, int n1, int j, int n2) {
		if(dir2){
			scacchiera[j][n2]=getSide(k, n1);
			scacchiera[j+1][n2]=getOppSide(k, n1);
			scacchiera[k][n1]=1;
		}else{
			scacchiera[j][n2]=getSide(k, n1);
			scacchiera[j-1][n2]=getOppSide(k, n1);
			scacchiera[k][n1]=1;
		}
	}

	private static void spostaRiga2(boolean dir1, int i, int n1, int l, int n2) {
		if(dir1){
			scacchiera[l][n2]=getSide(i, n1);
			scacchiera[l][n2-2]=getOppSide(i, n1);
			scacchiera[i][n1]=1;
		}else{
			scacchiera[l][n2]=getSide(i, n1);
			scacchiera[l][n2+2]=getOppSide(i, n1);
			scacchiera[i][n1]=1;
		}
		 
	}

	private static void spostaRiga(boolean dir1, int i, int n1, int l, int n2) {
		if(dir1){
			scacchiera[l][n2]=getSide(i, n1);
			scacchiera[l][n2-1]=getOppSide(i, n1);
			scacchiera[i][n1]=1;
		}else{
			scacchiera[l][n2]=getSide(i, n1);
			scacchiera[l][n2+1]=getOppSide(i, n1);
			scacchiera[i][n1]=1;
		}
	}

	private static void cattura(byte side) {
		if(side == bianco)
			nereCatturate ++;
		else
			biancheCatturate ++;
	}

	private static byte getOppSide(int i, int numericValue) {
		return (scacchiera[i][numericValue] == 2)? nero: bianco;
	}

	private static byte getSide(int i, int numericValue) {
		return (scacchiera[i][numericValue] == 2)? bianco: nero;
	}

	private static int corrispondenza(char indice) {
		char x = Character.toUpperCase(indice);
		switch(x){
			case 'A':
				return 1;
			case 'B':
				return 2;
			case 'C':
				return 3;
			case 'D':
				return 4;
			case 'E':
				return 5;
			case 'F':
				return 6;
			case 'G':
				return 7;
			case 'H':
				return 8;
			case 'I':
				return 9;	
			default:
				throw new IllegalStateException("Entrato in caso default dello switch"+ x);
		}
	}
	
	public static void stampa(byte[][] s){
		String x = " ABCDEFGHI ";
		System.out.println("    1 2 3 4 5 6 7 8 9  ");
		for(int i=0; i<s.length; i++){
			System.out.print(x.charAt(i)+ " ");
			for(int j=0; j<s.length; j++)
				System.out.print((s[i][j]) + " ");
		System.out.println();
		}
	}

	public static void distanza(){ 
		for(int i=0; i<scacchiera.length; i++){
			for(int j=0; j<scacchiera[0].length; j++){
				for(int k=0; k<scacchiera.length; k++){
					for(int l=0; l<scacchiera[0].length;l++){
						Integer key1=(Integer)(i*1000+j*100+k*10+l);
						if(!distance.containsKey(key1)){
							dist=(Integer) calcolaDistanza(i,j,k,l);
							distance.put(key1, dist);
						}
					}
				}
			}
		}
	}
	
	private static int calcolaDistanza(int i, int j, int k, int l){
		int col = j-l; 
		int riga = i-k;
		if(col<0 ^ riga <0)
			return Math.abs(col)+Math.abs(riga);
		return Math.max(Math.abs(col), Math.abs(riga));
	}
	
	
	
	/**
	 * Runs the client as an application.
	 */
	public static void main(String[] args) throws Exception {
//		String serverAddress = (args.length == 0) ? "localhost" : args[0];
//		int serverPort = (args.length == 0) ? 8901 : Integer.parseInt(args[1]);
//		Client client = new Client(serverAddress, serverPort);
//		client.play();
		in1 = new BufferedReader(new InputStreamReader(System.in));
		long startTime, endTime;
		double v;
		String mossa; 
		stampa(scacchiera);
		distanza();
		while(true){
			System.out.println("Inserire mossa>> ");
			mossa = in1.readLine();
			startTime = System.nanoTime();
			convertiStringaMossa(mossa);
			stampa(scacchiera);
			endTime = System.nanoTime();
			System.out.println("\nPedine nere mangiate:    " + nereCatturate);
			System.out.println("Pedine bianche mangiate: " + biancheCatturate + "\n");
			System.out.println("Ho finito in : " + (endTime-startTime) + " ns." );
			startTime = System.nanoTime();
			/*v= valutaMossa(scacchiera, "BiancO", 0, 8);
			endTime = System.nanoTime();
			System.out.println("La valutazione della scacchiera è: " + v + "\n");
			System.out.println("Ho finito in : " + (endTime-startTime) + " ns." );*/
		}
	}
}
