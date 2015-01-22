package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;


public class DistanzaCelle {


	public static Integer dist;
	
	public static void distanza(int[][] m, HashMap<Integer, Integer> hm){ 
		for(int i=0; i<m.length; i++){
			for(int j=0; j<m[0].length; j++){
				for(int k=0; k<m.length; k++){
					for(int l=0; l<m[0].length;l++){
						Integer key1=(Integer)(i*1000+j*100+k*10+l);
						if(!hm.containsKey(key1)){
							dist=(Integer) calcolaDistanza(i,j,k,l);
							hm.put(key1, dist);
						}
					}
				}
			}
		}
	}
	
	public static int calcolaDistanza(int i, int j, int k, int l){
		int col = j-l; 
		int riga = i-k;
		if(col<0 ^ riga <0)
			return Math.abs(col)+Math.abs(riga);
		return Math.max(Math.abs(col), Math.abs(riga));
	}
	
	public static void main(String[] argv) throws IOException {
		long startTime = System.nanoTime();
		int [][] m = new int[11][11];
		
		HashMap<Integer, Integer> hm = new HashMap<>(121*121);
				
		distanza(m, hm);
		long endTime = System.nanoTime();
		System.out.println("Ho finito e ci ho messo "+(endTime - startTime) + " ns");
		
		Integer x=null;
		while(true){
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter Integer: \n");
        try{
            x = Integer.parseInt(br.readLine());
        }catch(NumberFormatException nfe){
            System.err.println("Invalid Format!");
        }
    
		System.out.println(hm.get(x)+"\n");
		}
	}
}
