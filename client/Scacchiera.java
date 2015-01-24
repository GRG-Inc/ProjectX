package client;

public class Scacchiera {
	
	//0= casella non valida, 1=casella vuota, 2=pedina bianca, 3=pedina nera
	private byte[][] scacchiera;
	private int nereCatturate=0, biancheCatturate=0;//nereCatturate e' il numero di pedine perse da side1	
//	private HashMap<Integer, Integer> distance = new HashMap<Integer, Integer>();
//	private byte[] direzioni = {1,2,3,4,5,6};//N,NO,O,S,SE,E
	private final static int[] minColumn = { 1, 1, 1, 1, 1, 1, 2, 3, 4, 5}; //da che colonna inizia la scacchiera per ogni riga compresa cornice
	private final static int[] maxColumn = { 5, 5, 6, 7, 8, 9, 9, 9, 9, 9}; //a che colonna finisce la scacchiera per ogni riga compresa cornice
	private final byte bianco=2, nero=3;
//	private Integer dist;
	
	
		  
	
	public Scacchiera(){
		this.scacchiera = new byte[][]{ { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
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
		
		//this.nereCatturate = 0;
		//this.biancheCatturate = 0;
	}
	
	public Scacchiera clona(){
		Scacchiera clone=new Scacchiera();
		byte[][] t = new byte[11][11];
		for(int i = 0 ; i < 11 ; i++)
			for(int j = 0 ; j < 11 ; j++){
				t[i][j] = scacchiera[i][j];
			}
		clone.scacchiera = t;
		clone.nereCatturate = nereCatturate;
		clone.biancheCatturate = biancheCatturate;
		return clone;
	}
	
	public int getBiancheCatturate(){
		return biancheCatturate;
	}
	
	public int getNereCatturate(){
		return nereCatturate;
	}
	
	public byte[][] getScacchiera(){
		return scacchiera;
	}
	
	//FIXME ===============> da testare
	//� il caso di farlo statico?
	public boolean esisteCella(int riga, int colonna){
		return (riga >= 1 && riga <= 9 && colonna >= minColumn[riga] && colonna<= maxColumn[riga]);
	}

	
	
	public void aggiornaScacchiera(int origP1, int origP2, int origU1, int origU2, int destP1, int destP2, int destU1, int destU2){
			
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
					traslaDiagonale(numeroPedine, origP1, origP2, destP1, destP2);
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
					traslaDiagonale(numeroPedine, origP1, origP2, destP1, destP2);
					
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

	
	private void traslaDiagonale (int numeroPedine, int i, int n1, int j, int n2){
		byte side = getSide(i, n1);
		scacchiera[j][n2] = side;
		scacchiera[j+1][n2+1] = side;
		scacchiera[i][n1] = 1;
		scacchiera[i+1][n1+1] = 1;
		if(numeroPedine == 3){
			scacchiera[j+2][n2+2] = side;
			scacchiera[i+2][n1+2] = 1;
		}
	}
	
	private void traslaColonna(int numeroPedine, int i, int n1, int j, int n2) {
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
	
	private void traslaRiga(int numeroPedine, int i, int n1, int j, int n2) {
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

	private void attacca(int k, int n1, int j, int n2) {
		byte side=getSide(k, n1);
		cattura(side);
		scacchiera[j][n2] = side;
		scacchiera[k][n1] = 1;
	}

	private void spostaNO2(int k, int n1, int j, int n2) {
		scacchiera[j][n2]=getSide(k, n1);
		scacchiera[j-2][n2-2]=getOppSide(k, n1);
		scacchiera[k][n1]=1;	
	}
	
	private void spostaNO(int k, int n1, int j, int n2) {
		scacchiera[j][n2]=getSide(k, n1);
		scacchiera[j-1][n2-1]=getOppSide(k, n1);
		scacchiera[k][n1]=1;	
	}

	private void spostaSE2(int i, int n1, int l, int n2) {
		scacchiera[l][n2]=getSide(i, n1);
		scacchiera[l+2][n2+2]=getOppSide(i, n1);
		scacchiera[i][n1]=1;
	}

	private void spostaSE(int i, int n1, int l, int n2) {
		scacchiera[l][n2]=getSide(i, n1);
		scacchiera[l+1][n2+1]=getOppSide(i, n1);
		scacchiera[i][n1]=1;
	}

	private void spostaColonna2(boolean dir2, int k, int n1, int j, int n2) {
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

	private void spostaColonna(boolean dir2, int k, int n1, int j, int n2) {
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

	private void spostaRiga2(boolean dir1, int i, int n1, int l, int n2) {
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

	private void spostaRiga(boolean dir1, int i, int n1, int l, int n2) {
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

	private void cattura(byte side) {
		if(side == bianco)
			nereCatturate ++;
		else
			biancheCatturate ++;
	}

	private byte getOppSide(int i, int numericValue) {
		return (scacchiera[i][numericValue] == 2)? nero: bianco;
	}

	private byte getSide(int i, int numericValue) {
		return (scacchiera[i][numericValue] == 2)? bianco: nero;
	}
	
	public void stampa(byte[][] s){
		String x = " ABCDEFGHI ";
		System.out.println("    1 2 3 4 5 6 7 8 9  ");
		for(int i=0; i<s.length; i++){
			System.out.print(x.charAt(i)+ " ");
			for(int j=0; j<s.length; j++)
				System.out.print((s[i][j]) + " ");
		System.out.println();
		}
	}

}
