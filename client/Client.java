package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class Client {
	
	private Socket socket;
	private String colour;
	private static String mossaFinale;
	private static BufferedReader in,in1;
	private static PrintWriter out;
	private static final byte[] CostiCattura = { 0, 4, 6, 8, 12, 18, 100 };
	private final static int[] minColumn = { 1, 1, 1, 1, 1, 1, 2, 3, 4, 5}; //da che colonna inizia la scacchiera per ogni riga compresa cornice
	private final static int[] maxColumn = { 5, 5, 6, 7, 8, 9, 9, 9, 9, 9}; //a che colonna finisce la scacchiera per ogni riga compresa cornice
	private static byte[] direzioni = {1,2,3,4,5,6};//N,NO,O,S,SE,E
	private static HashMap<Integer, Integer> distance = new HashMap<Integer, Integer>();
	private static Integer dist;
	static final byte bianco=2, nero=3;
	
	
//	private Thread attesaMessaggi;
//	private Thread operazioni;

	public static Scacchiera scacchiera; 
	
	public Client(String serverAddress, int port) throws Exception {
		
		scacchiera = new Scacchiera();
		// Setup networking
//		socket = new Socket(serverAddress, port);
//		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//		out = new PrintWriter(socket.getOutputStream(), true);
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
					String mossa = generaProssimaMossa(scacchiera, colour, depth);
					
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
	
	private static String generaStringaMossa(int i, int j, int k, int l, int m, int n, int o, int p){
		StringBuilder sb = new StringBuilder();
		sb.append(corrispondenzaR(i));
		sb.append(j);
		sb.append(corrispondenzaR(k));
		sb.append(l);
		sb.append(corrispondenzaR(m));
		sb.append(n);
		sb.append(corrispondenzaR(o));
		sb.append(p);
		return sb.toString();
	}
	
	public static String generaProssimaMossa(Scacchiera s, String side, int d){
		valutaMossa(s, side, d, Double.NEGATIVE_INFINITY);	
		return mossaFinale;
	}

	public static double valutaMossa(Scacchiera scacchiera2, String side1, int depth, double alfabeta) {
		byte[][] scacc= scacchiera2.getScacchiera();
		byte s1, s2;
		String side2;
		if(side1.equalsIgnoreCase("bianco")){
			s1 = bianco;
			s2 = nero;
			side2 = "nero";
		}
		else{
			s1 = nero;
			s2 = bianco;
			side2 = "bianco";
		}
			
		if(depth == 0){
			//assegna valore a configurazione corrente
			double w1 = 1,w2 = 1,w3 = 1; //pesi
			double centerDist = 0, coesione = 0, premioCatt = 0, penaleCatt = 0;
			if(s1==2){
				if(scacchiera2.getBiancheCatturate()<7 && scacchiera2.getNereCatturate()<7){//FIXME // mangia 7 pedine bianche e da errore
					premioCatt = CostiCattura[scacchiera2.getNereCatturate()];
					penaleCatt = CostiCattura[scacchiera2.getBiancheCatturate()];
				}
			}else{
				premioCatt = CostiCattura[scacchiera2.getBiancheCatturate()];
				penaleCatt = CostiCattura[scacchiera2.getNereCatturate()];
			}
			for(int i = 1; i<10; i++)
				for(int j = minColumn[i]; j <= maxColumn[i]; j++){
					if(scacc[i][j] == s1){
						centerDist += 0.1;
						coesione += calcolaCoesione(scacc,i,j);
					}else if(scacc[i][j] == s2){
						centerDist -= 0.1;
						coesione -= calcolaCoesione(scacc,i,j);
					}
				}
			return w1*centerDist + w2*coesione + w3*premioCatt + w3*penaleCatt;
		}else{
			//genera configurazione futura
			double bestValue = Double.POSITIVE_INFINITY, currValue, ab = alfabeta;
			String m = null, mossa = null;
			Scacchiera scacFuturaClass = scacchiera2.clona();
			byte[][] scacFutura= scacFuturaClass.getScacchiera();
			aleg: for(int i=1; i<10; i++){
					for(int j = minColumn[i]; j<=maxColumn[i]; j++){
						if(scacc[i][j] == s1){
							for(int k = 0; k < direzioni.length; k++){
								if(direzioni[k]==1){//NORD
									//va fatto il pruning e va valutata la funzione restituita
									if(scacFuturaClass.esisteCella(i-1, j)){//FIXME
										if(scacFutura[i-1][j] == 1){//una pedina
											scacFuturaClass.aggiornaScacchiera(i, j, i, j, i-1, j, i-1, j);
											m = generaStringaMossa(i, j, i, j, i-1, j, i-1, j);
											ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
											currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
											if(currValue < bestValue){
												bestValue = currValue;
												mossa = m;
											}
											if (alfabeta > bestValue)
												break aleg;
											ab = bestValue;
										}else if(scacFutura[i-1][j] == s1){// due pedine allineate
												if(scacFuturaClass.esisteCella(i-2, j)){
													if(scacFutura[i-2][j] == 1){// la cella controllata è vuota quindi mi sposto li
														scacFuturaClass.aggiornaScacchiera(i, j, i-1, j, i-1, j, i-2, j);// da controllare
														m = generaStringaMossa(i, j, i-1, j, i-1, j, i-2, j);
														ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
														currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
														if(currValue < bestValue){
															bestValue = currValue;
															mossa = m;
														}
														if (alfabeta > bestValue)
															break aleg;
														ab = bestValue;
													}else if(scacFutura[i-2][j] == s2 && ((scacFuturaClass.esisteCella(i-3, j) && scacFutura[i-3][j] == 1) || scacFutura[i-3][j] == 0 )){//NOTA: if innestati per controllare una sola volta esistenza celle per i-3, i-4 ecc ecc 
															scacFuturaClass.aggiornaScacchiera(i, j, i-1, j, i-1, j, i-2, j);// da controllare, la pedina avversaria non viene toccata
															m = generaStringaMossa(i, j, i-1, j, i-1, j, i-2, j);
															ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
															currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
															if(currValue < bestValue){
																bestValue = currValue;
																mossa = m;
															}
															if (alfabeta > bestValue)
																break aleg;
															ab = bestValue;
														}else if(scacFutura[i-2][j] == s1 ){//tre pedine allineate
															if(scacFuturaClass.esisteCella(i-3, j)){
																if(scacFutura[i-3][j] == 1){//cella vuota
																	scacFuturaClass.aggiornaScacchiera(i, j, i-2, j, i-1, j, i-3, j);// da controllare
																	//FIXME alfabeta da definire
																	m = generaStringaMossa(i, j, i-2, j, i-1, j, i-3, j);
																	ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																	currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																	if(currValue < bestValue){
																		bestValue = currValue;
																		mossa = m;
																	}
																	if (alfabeta > bestValue)
																		break aleg;
																	ab = bestValue;
																}else if(scacFutura[i-3][j] == s2 && scacFuturaClass.esisteCella(i-4, j)){//c'è avversario
																		if(scacFutura[i-4][j] == 1){
																			scacFuturaClass.aggiornaScacchiera(i, j, i-2, j, i-1, j, i-3, j);// da controllare
																			m = generaStringaMossa(i, j, i-2, j, i-1, j, i-3, j);
																			ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																			currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																			if(currValue < bestValue){
																				bestValue = currValue;
																				mossa = m;
																			}
																			if (alfabeta > bestValue)
																				break aleg;
																			ab = bestValue;
																		}else if(scacFutura[i-4][j] == s2){
																				if(scacFuturaClass.esisteCella(i-5, j) && scacFutura[i-5][j] == 1){
																					scacFuturaClass.aggiornaScacchiera(i, j, i-2, j, i-1, j, i-3, j);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i-2, j, i-1, j, i-3, j);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}else if(scacFutura[i-5][j] == 0){
																					scacFuturaClass.aggiornaScacchiera(i, j, i-2, j, i-1, j, i-3, j);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i-2, j, i-1, j, i-3, j);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}
																		}
																	}else if(scacFutura[i-3][j] == s2 && scacFutura[i-4][j] == 0){
																		scacFuturaClass.aggiornaScacchiera(i, j, i-2, j, i-1, j, i-3, j);// da controllare
																		m = generaStringaMossa(i, j, i-2, j, i-1, j, i-3, j);
																		ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																		currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																		if(currValue < bestValue){
																			bestValue = currValue;
																			mossa = m;
																		}
																		if (alfabeta > bestValue)
																			break aleg;
																		ab = bestValue;
																	}
																}
														}
												}
										}
									}
								}else if(direzioni[k]==2){
									//NORD-OVEST
									if(scacFuturaClass.esisteCella(i-1, j-1)){//FIXME
										if(scacFutura[i-1][j-1] == 1){//una pedina
											scacFuturaClass.aggiornaScacchiera(i, j, i, j, i-1, j-1, i-1, j-1);
											m = generaStringaMossa(i, j, i, j, i-1, j-1, i-1, j-1);
											ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
											currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
											if(currValue < bestValue){
												bestValue = currValue;
												mossa = m;
											}
											if (alfabeta > bestValue)
												break aleg;
											ab = bestValue;
										}else if(scacFutura[i-1][j-1] == s1){// due pedine allineate
												if(scacFuturaClass.esisteCella(i-2, j-2)){
													if(scacFutura[i-2][j-2] == 1){// la cella controllata è vuota quindi mi sposto li
														scacFuturaClass.aggiornaScacchiera(i, j, i-1, j-1, i-1, j-1, i-2, j-2);// da controllare
														m = generaStringaMossa(i, j, i-1, j-1, i-1, j-1, i-2, j-2);
														ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
														currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
														if(currValue < bestValue){
															bestValue = currValue;
															mossa = m;
														}
														if (alfabeta > bestValue)
															break aleg;
														ab = bestValue;
													}else if(scacFutura[i-2][j-2] == s2 && (scacFuturaClass.esisteCella(i-3, j-3) && scacFutura[i-3][j-3] == 1 || scacFutura[i-3][j-3] == 0)){//NOTA: if annestati per controllare una sola volta esistenza celle per i-3, i-4 ecc ecc 
															scacFuturaClass.aggiornaScacchiera(i, j, i-1, j-1, i-1, j-1, i-2, j-2);// da controllare, la pedina avversaria non viene toccata
															m = generaStringaMossa(i, j, i-1, j-1, i-1, j-1, i-2, j-2);
															ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
															currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
															if(currValue < bestValue){
																bestValue = currValue;
																mossa = m;
															}
															if (alfabeta > bestValue)
																break aleg;
															ab = bestValue;
															}else if(scacFutura[i-2][j-2] == s1 ){//tre pedine allineate
															if(scacFuturaClass.esisteCella(i-3, j-3)){
																if(scacFutura[i-3][j-3] == 1){//cella vuota
																	scacFuturaClass.aggiornaScacchiera(i, j, i-2, j-2, i-1, j-1, i-3, j-3);// da controllare
																	m = generaStringaMossa(i, j, i-2, j-2, i-1, j-1, i-3, j-3);
																	ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																	currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																	if(currValue < bestValue){
																		bestValue = currValue;
																		mossa = m;
																	}
																	if (alfabeta > bestValue)
																		break aleg;
																	ab = bestValue;
																}else if(scacFutura[i-3][j-3] == s2 && scacFuturaClass.esisteCella(i-4, j-4)){//c'è avversario
																		if(scacFutura[i-4][j-4] == 1){
																			scacFuturaClass.aggiornaScacchiera(i, j, i-2, j-2, i-1, j-1, i-3, j-3);// da controllare
																			m = generaStringaMossa(i, j, i-2, j-2, i-1, j-1, i-3, j-3);
																			ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																			currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																			if(currValue < bestValue){
																				bestValue = currValue;
																				mossa = m;
																			}
																			if (alfabeta > bestValue)
																				break aleg;
																			ab = bestValue;
																		}else if(scacFutura[i-4][j-4] == s2){
																				if(scacFuturaClass.esisteCella(i-5, j-5) && scacFutura[i-5][j-5] == 1){
																					scacFuturaClass.aggiornaScacchiera(i, j, i-2, j-2, i-1, j-1, i-3, j-3);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i-2, j-2, i-1, j-1, i-3, j-3);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}else if(scacFutura[i-5][j-5] == 0){
																					scacFuturaClass.aggiornaScacchiera(i, j, i-2, j-2, i-1, j-1, i-3, j-3);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i-2, j-2, i-1, j-1, i-3, j-3);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}
																		}
																	}else if(scacFutura[i-3][j-3] == s2 && scacFutura[i-4][j-4] == 0){
																		scacFuturaClass.aggiornaScacchiera(i, j, i-2, j-2, i-1, j-1, i-3, j-3);// da controllare
																		m = generaStringaMossa(i, j, i-2, j-2, i-1, j-1, i-3, j-3);
																		ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																		currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																		if(currValue < bestValue){
																			bestValue = currValue;
																			mossa = m;
																		}
																		if (alfabeta > bestValue)
																			break aleg;
																		ab = bestValue;
																	}
																}
														}
												}
										}
									}
									
								}else if(direzioni[k]==3){
									//OVEST
									if(scacFuturaClass.esisteCella(i, j-1)){//FIXME
										if(scacFutura[i][j-1] == 1){//una pedina
											scacFuturaClass.aggiornaScacchiera(i, j, i, j, i, j-1, i, j-1);
											m = generaStringaMossa(i, j, i, j, i, j-1, i, j-1);
											ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
											currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
											if(currValue < bestValue){
												bestValue = currValue;
												mossa = m;
											}
											if (alfabeta > bestValue)
												break aleg;
											ab = bestValue;
										}else if(scacFutura[i][j-1] == s1){// due pedine allineate
												if(scacFuturaClass.esisteCella(i, j-2)){
													if(scacFutura[i][j-2] == 1){// la cella controllata è vuota quindi mi sposto li
														scacFuturaClass.aggiornaScacchiera(i, j, i, j-1, i, j-1, i, j-2);// da controllare
														m = generaStringaMossa(i, j, i, j-1, i, j-1, i, j-2);
														ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
														currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
														if(currValue < bestValue){
															bestValue = currValue;
															mossa = m;
														}
														if (alfabeta > bestValue)
															break aleg;
														ab = bestValue;
													}else if(scacFutura[i][j-2] == s2 && (scacFuturaClass.esisteCella(i, j-3) && scacFutura[i][j-3] == 1 || scacFutura[i][j-3] == 0)){//NOTA: if annestati per controllare una sola volta esistenza celle per i-3, i-4 ecc ecc 
															scacFuturaClass.aggiornaScacchiera(i, j, i, j-1, i, j-1, i, j-2);// da controllare, la pedina avversaria non viene toccata
															m = generaStringaMossa(i, j, i, j-1, i, j-1, i, j-2);
															ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
															currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
															if(currValue < bestValue){
																bestValue = currValue;
																mossa = m;
															}
															if (alfabeta > bestValue)
																break aleg;
															ab = bestValue;
															}else if(scacFutura[i][j-2] == s1 ){//tre pedine allineate
															if(scacFuturaClass.esisteCella(i, j-3)){
																if(scacFutura[i][j-3] == 1){//cella vuota
																	scacFuturaClass.aggiornaScacchiera(i, j, i, j-2, i, j-1, i, j-3);// da controllare
																	m = generaStringaMossa(i, j, i, j-2, i, j-1, i, j-3);
																	ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																	currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																	if(currValue < bestValue){
																		bestValue = currValue;
																		mossa = m;
																	}
																	if (alfabeta > bestValue)
																		break aleg;
																	ab = bestValue;
																}else if(scacFutura[i][j-3] == s2 && scacFuturaClass.esisteCella(i, j-4)){//c'è avversario
																		if(scacFutura[i][j-4] == 1){
																			scacFuturaClass.aggiornaScacchiera(i, j, i, j-2, i, j-1, i, j-3);// da controllare
																			m = generaStringaMossa(i, j, i, j-2, i, j-1, i, j-3);
																			ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																			currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																			if(currValue < bestValue){
																				bestValue = currValue;
																				mossa = m;
																			}
																			if (alfabeta > bestValue)
																				break aleg;
																			ab = bestValue;
																		}else if(scacFutura[i][j-4] == s2){
																				if(scacFuturaClass.esisteCella(i, j-5) && scacFutura[i][j-5] == 1){
																					scacFuturaClass.aggiornaScacchiera(i, j, i, j-2, i, j-1, i, j-3);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i, j-2, i, j-1, i, j-3);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}else if(scacFutura[i][j-5] == 0){
																					scacFuturaClass.aggiornaScacchiera(i, j, i, j-2, i, j-1, i, j-3);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i, j-2, i, j-1, i, j-3);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}
																		}
																	}else if(scacFutura[i][j-3] == s2 && scacFutura[i][j-4] == 0){
																		scacFuturaClass.aggiornaScacchiera(i, j, i, j-2, i, j-1, i, j-3);// da controllare
																		m = generaStringaMossa(i, j, i, j-2, i, j-1, i, j-3);
																		ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																		currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																		if(currValue < bestValue){
																			bestValue = currValue;
																			mossa = m;
																		}
																		if (alfabeta > bestValue)
																			break aleg;
																		ab = bestValue;
																	}
																}
														}
												}
										}
									}
									
								}else if(direzioni[k]==4){
									//SUD
									if(scacFuturaClass.esisteCella(i+1, j)){//FIXME
										if(scacFutura[i+1][j] == 1){//una pedina
											scacFuturaClass.aggiornaScacchiera(i, j, i, j, i+1, j, i+1, j);
											m = generaStringaMossa(i, j, i, j, i+1, j, i+1, j);
											ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
											currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
											if(currValue < bestValue){
												bestValue = currValue;
												mossa = m;
											}
											if (alfabeta > bestValue)
												break aleg;
											ab = bestValue;
										}else if(scacFutura[i+1][j] == s1){// due pedine allineate
												if(scacFuturaClass.esisteCella(i+2, j)){
													if(scacFutura[i+2][j] == 1){// la cella controllata è vuota quindi mi sposto li
														scacFuturaClass.aggiornaScacchiera(i, j, i+1, j, i+1, j, i+2, j);// da controllare
														m = generaStringaMossa(i, j, i+1, j, i+1, j, i+2, j);
														ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
														currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
														if(currValue < bestValue){
															bestValue = currValue;
															mossa = m;
														}
														if (alfabeta > bestValue)
															break aleg;
														ab = bestValue;
													}else if(scacFutura[i+2][j] == s2 && (scacFuturaClass.esisteCella(i+3, j) && scacFutura[i+3][j] == 1 || scacFutura[i+3][j] == 0)){//NOTA: if annestati per controllare una sola volta esistenza celle per i-3, i-4 ecc ecc 
															scacFuturaClass.aggiornaScacchiera(i, j, i+1, j, i+1, j, i+2, j);// da controllare, la pedina avversaria non viene toccata
															m = generaStringaMossa(i, j, i+1, j, i+1, j, i+2, j);
															ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
															currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
															if(currValue < bestValue){
																bestValue = currValue;
																mossa = m;
															}
															if (alfabeta > bestValue)
																break aleg;
															ab = bestValue;
															}else if(scacFutura[i+2][j] == s1 ){//tre pedine allineate
															if(scacFuturaClass.esisteCella(i+3, j)){
																if(scacFutura[i+3][j] == 1){//cella vuota
																	scacFuturaClass.aggiornaScacchiera(i, j, i+2, j, i+1, j, i+3, j);// da controllare
																	m = generaStringaMossa(i, j, i+2, j, i+1, j, i+3, j);
																	ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																	currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																	if(currValue < bestValue){
																		bestValue = currValue;
																		mossa = m;
																	}
																	if (alfabeta > bestValue)
																		break aleg;
																	ab = bestValue;
																}else if(scacFutura[i+3][j] == s2 && scacFuturaClass.esisteCella(i+4, j)){//c'è avversario
																		if(scacFutura[i+4][j] == 1){
																			scacFuturaClass.aggiornaScacchiera(i, j, i+2, j, i+1, j, i+3, j);// da controllare
																			m = generaStringaMossa(i, j, i+2, j, i+1, j, i+3, j);
																			ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																			currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																			if(currValue < bestValue){
																				bestValue = currValue;
																				mossa = m;
																			}
																			if (alfabeta > bestValue)
																				break aleg;
																			ab = bestValue;
																		}else if(scacFutura[i+4][j] == s2){
																				if(scacFuturaClass.esisteCella(i+5, j) && scacFutura[i+5][j] == 1){
																					scacFuturaClass.aggiornaScacchiera(i, j, i+2, j, i+1, j, i+3, j);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i+2, j, i+1, j, i+3, j);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}else if(scacFutura[i+5][j] == 0){
																					scacFuturaClass.aggiornaScacchiera(i, j, i+2, j, i+1, j, i+3, j);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i+2, j, i+1, j, i+3, j);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}
																		}
																	}else if(scacFutura[i+3][j] == s2 && scacFutura[i+4][j] == 0){
																		scacFuturaClass.aggiornaScacchiera(i, j, i+2, j, i+1, j, i+3, j);// da controllare
																		m = generaStringaMossa(i, j, i+2, j, i+1, j, i+3, j);
																		ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																		currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																		if(currValue < bestValue){
																			bestValue = currValue;
																			mossa = m;
																		}
																		if (alfabeta > bestValue)
																			break aleg;
																		ab = bestValue;
																	}
																}	
														}
												}
										}
									}	
								}else if(direzioni[k]==5){
									//SUD-EST
									if(scacFuturaClass.esisteCella(i+1, j+1)){//FIXME
										if(scacFutura[i+1][j+1] == 1){//una pedina
											scacFuturaClass.aggiornaScacchiera(i, j, i, j, i+1, j+1, i+1, j+1);
											m = generaStringaMossa(i, j, i, j, i+1, j+1, i+1, j+1);
											ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
											currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
											if(currValue < bestValue){
												bestValue = currValue;
												mossa = m;
											}
											if (alfabeta > bestValue)
												break aleg;
											ab = bestValue;
										}else if(scacFutura[i+1][j+1] == s1){// due pedine allineate
												if(scacFuturaClass.esisteCella(i+2, j+2)){
													if(scacFutura[i+2][j+2] == 1){// la cella controllata è vuota quindi mi sposto li
														scacFuturaClass.aggiornaScacchiera(i, j, i+1, j+1, i+1, j+1, i+2, j+2);// da controllare
														m = generaStringaMossa(i, j, i+1, j+1, i+1, j+1, i+2, j+2);
														ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
														currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
														if(currValue < bestValue){
															bestValue = currValue;
															mossa = m;
														}
														if (alfabeta > bestValue)
															break aleg;
														ab = bestValue;
													}else if(scacFutura[i+2][j+2] == s2 && (scacFuturaClass.esisteCella(i+3, j+3) && scacFutura[i+3][j+3] == 1 || scacFutura[i+3][j+3] == 0)){//NOTA: if annestati per controllare una sola volta esistenza celle per i-3, i-4 ecc ecc 
															scacFuturaClass.aggiornaScacchiera(i, j, i+1, j+1, i+1, j+1, i+2, j+2);// da controllare, la pedina avversaria non viene toccata
															m = generaStringaMossa(i, j, i+1, j+1, i+1, j+1, i+2, j+2);
															ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
															currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
															if(currValue < bestValue){
																bestValue = currValue;
																mossa = m;
															}
															if (alfabeta > bestValue)
																break aleg;
															ab = bestValue;
															}else if(scacFutura[i+2][j+2] == 1 ){//tre pedine allineate
															if(scacFuturaClass.esisteCella(i+3, j+3)){
																if(scacFutura[i+3][j+3] == s1){//cella vuota
																	scacFuturaClass.aggiornaScacchiera(i, j, i+2, j+2, i+1, j+1, i+3, j+3);// da controllare
																	m = generaStringaMossa(i, j, i+2, j+2, i+1, j+1, i+3, j+3);
																	ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																	currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																	if(currValue < bestValue){
																		bestValue = currValue;
																		mossa = m;
																	}
																	if (alfabeta > bestValue)
																		break aleg;
																	ab = bestValue;
																}else if(scacFutura[i+3][j+3] == s2 && scacFuturaClass.esisteCella(i+4, j+4)){//c'è avversario
																		if(scacFutura[i+4][j+4] == 1){
																			scacFuturaClass.aggiornaScacchiera(i, j, i+2, j+2, i+1, j+1, i+3, j+3);// da controllare
																			m = generaStringaMossa(i, j, i+2, j+2, i+1, j+1, i+3, j+3);
																			ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																			currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																			if(currValue < bestValue){
																				bestValue = currValue;
																				mossa = m;
																			}
																			if (alfabeta > bestValue)
																				break aleg;
																			ab = bestValue;
																		}else if(scacFutura[i+4][j+4] == s2){
																				if(scacFuturaClass.esisteCella(i+5, j+5) && scacFutura[i+5][j+5] == 1){
																					scacFuturaClass.aggiornaScacchiera(i, j, i+2, j+2, i+1, j+1, i+3, j+3);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i+2, j+2, i+1, j+1, i+3, j+3);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}else if(scacFutura[i+5][j+5] == 0){
																					scacFuturaClass.aggiornaScacchiera(i, j, i+2, j+2, i+1, j+1, i+3, j+3);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i+2, j+2, i+1, j+1, i+3, j+3);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}
																		}
																	}else if(scacFutura[i+3][j+3] == s2 && scacFutura[i+4][j+4] == 0){
																		scacFuturaClass.aggiornaScacchiera(i, j, i+2, j+2, i+1, j+1, i+3, j+3);// da controllare
																		m = generaStringaMossa(i, j, i+2, j+2, i+1, j+1, i+3, j+3);
																		ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																		currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																		if(currValue < bestValue){
																			bestValue = currValue;
																			mossa = m;
																		}
																		if (alfabeta > bestValue)
																			break aleg;
																		ab = bestValue;
																	}
																}
														}
												}
										}
									}
								}else{
									//EST
									if(scacFuturaClass.esisteCella(i, j+1)){//FIXME
										if(scacFutura[i][j+1] == 1){//una pedina
											scacFuturaClass.aggiornaScacchiera(i, j, i, j, i, j+1, i, j+1);
											m = generaStringaMossa(i, j, i, j, i, j+1, i, j+1);
											ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
											currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
											if(currValue < bestValue){
												bestValue = currValue;
												mossa = m;
											}
											if (alfabeta > bestValue)
												break aleg;
											ab = bestValue;
										}else if(scacFutura[i][j+1] == s1){// due pedine allineate
												if(scacFuturaClass.esisteCella(i, j+2)){
													if(scacFutura[i][j+2] == 1){// la cella controllata è vuota quindi mi sposto li
														scacFuturaClass.aggiornaScacchiera(i, j, i, j+1, i, j+1, i, j+2);// da controllare
														m = generaStringaMossa(i, j, i, j+1, i, j+1, i, j+2);
														ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
														currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
														if(currValue < bestValue){
															bestValue = currValue;
															mossa = m;
														}
														if (alfabeta > bestValue)
															break aleg;
														ab = bestValue;
													}else if(scacFutura[i][j+2] == s2 && (scacFuturaClass.esisteCella(i, j+3) && scacFutura[i][j+3] == 1 || scacFutura[i][j+3] == 0)){//NOTA: if annestati per controllare una sola volta esistenza celle per i-3, i-4 ecc ecc 
															scacFuturaClass.aggiornaScacchiera(i, j, i, j+1, i, j+1, i, j+2);// da controllare, la pedina avversaria non viene toccata
															m = generaStringaMossa(i, j, i, j+1, i, j+1, i, j+2);
															ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
															currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
															if(currValue < bestValue){
																bestValue = currValue;
																mossa = m;
															}
															if (alfabeta > bestValue)
																break aleg;
															ab = bestValue;
															}else if(scacFutura[i][j+2] == 1 ){//tre pedine allineate
															if(scacFuturaClass.esisteCella(i, j+3)){
																if(scacFutura[i][j-3] == s1){//cella vuota
																	scacFuturaClass.aggiornaScacchiera(i, j, i, j+2, i, j+1, i, j+3);// da controllare
																	m = generaStringaMossa(i, j, i, j+2, i, j+1, i, j+3);
																	ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																	currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																	if(currValue < bestValue){
																		bestValue = currValue;
																		mossa = m;
																	}
																	if (alfabeta > bestValue)
																		break aleg;
																	ab = bestValue;
																}else if(scacFutura[i][j+3] == s2 && scacFuturaClass.esisteCella(i, j+4)){//c'è avversario
																		if(scacFutura[i][j+4] == 1){
																			scacFuturaClass.aggiornaScacchiera(i, j, i, j+2, i, j+1, i, j+3);// da controllare
																			m = generaStringaMossa(i, j, i, j+2, i, j+1, i, j+3);
																			ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																			currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																			if(currValue < bestValue){
																				bestValue = currValue;
																				mossa = m;
																			}
																			if (alfabeta > bestValue)
																				break aleg;
																			ab = bestValue;
																		}else if(scacFutura[i][j+4] == s2){
																				if(scacFuturaClass.esisteCella(i, j+5) && scacFutura[i][j+5] == 1){
																					scacFuturaClass.aggiornaScacchiera(i, j, i, j+2, i, j+1, i, j+3);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i, j+2, i, j+1, i, j+3);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}else if(scacFutura[i][j+5] == 0){
																					scacFuturaClass.aggiornaScacchiera(i, j, i, j+2, i, j+1, i, j+3);// da controllare, la pedina avversaria non viene toccata
																					m = generaStringaMossa(i, j, i, j+2, i, j+1, i, j+3);
																					ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																					currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																					if(currValue < bestValue){
																						bestValue = currValue;
																						mossa = m;
																					}
																					if (alfabeta > bestValue)
																						break aleg;
																					ab = bestValue;
																				}
																		}
																	}else if(scacFutura[i][j+3] == s2 && scacFutura[i][j+4] == 0){
																		scacFuturaClass.aggiornaScacchiera(i, j, i, j+2, i, j+1, i, j+3);// da controllare
																		m = generaStringaMossa(i, j, i, j+2, i, j+1, i, j+3);
																		ab = ab == Double.NEGATIVE_INFINITY ? ab: -ab;
																		currValue = valutaMossa(scacFuturaClass, side2, depth-1, ab);
																		if(currValue < bestValue){
																			bestValue = currValue;
																			mossa = m;
																		}
																		if (alfabeta > bestValue)
																			break aleg;
																		ab = bestValue;
																	
																}
														}
												}
											}
										}
									}
								}
							}
						}
					}
			}
			mossaFinale = mossa;
			return -bestValue;
		}
	}
	
	public static void distanza(){
		int [][] scacc = new int [11][11];
		for(int i=0; i<scacc.length; i++){
			for(int j=0; j<scacc[0].length; j++){
				for(int k=0; k<scacc.length; k++){
					for(int l=0; l<scacc[0].length;l++){
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
		scacchiera.aggiornaScacchiera(i,Character.getNumericValue(vm[1]),k,Character.getNumericValue(vm[3]),j,Character.getNumericValue(vm[5]),l,Character.getNumericValue(vm[7]));
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
				throw new IllegalStateException("Entrato in caso default dello switch 1 "+ indice);
		}
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
				throw new IllegalStateException("Entrato in caso default dello switch 2 "+ x);
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
		scacchiera = new Scacchiera();
		stampa(scacchiera.getScacchiera());
		distanza();
		while(true){
			System.out.println("Inserire mossa>> ");
			mossa = in1.readLine();
			startTime = System.nanoTime();
			convertiStringaMossa(mossa);
			stampa(scacchiera.getScacchiera());
			endTime = System.nanoTime();
			System.out.println("\nPedine nere mangiate:    " + scacchiera.getNereCatturate());
			System.out.println("Pedine bianche mangiate: " + scacchiera.getBiancheCatturate() + "\n");
			System.out.println("Ho finito in : " + (endTime-startTime) + " ns." );
			startTime = System.nanoTime();
			System.out.println("Mo ce pens.... \n");
			Client.generaProssimaMossa(scacchiera, "bianco", 4);
			convertiStringaMossa(mossaFinale);
			stampa(scacchiera.getScacchiera());
			endTime = System.nanoTime();
			System.out.println("La mossa generata è: " + mossaFinale + "\n");
			System.out.println("Ho finito in di elaborarela mossa in: " + (endTime-startTime) + " ns." );
		}
	}
}
