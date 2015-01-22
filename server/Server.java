package server;

import java.net.ServerSocket;

class Parameter{
	
	public int thres_move = 200;
	public int thres_time = 1000;
	public int port = 8901;
	public int blackId = 0;
	public int whiteId = 1;
	
	public Parameter(String[] args) throws ParsingException{
		for(int i=0; i<args.length; i+=2){
			if(args[i].charAt(0)!='-')
				throw new ParsingException();
			try{
				if(args[i].equals("-tm"))
					thres_move = Integer.valueOf(args[i+1]);
				else if(args[i].equals("-tt"))
					thres_time = Integer.valueOf(args[i+1]);
				else if(args[i].equals("-p"))
					port = Integer.valueOf(args[i+1]);
				else if(args[i].equals("-black"))
					blackId = Integer.valueOf(args[i+1]);
				else if(args[i].equals("-white"))
					whiteId = Integer.valueOf(args[i+1]);
				else
					throw new ParsingException();
			}catch(Exception e){
				throw new ParsingException();
			}
			
		}
	}
}

enum Colour {
	Black, White
};

/**
 * Messages from Server to Client
 * * WELCOME <colour>
 * * MESSAGE<message>
 * * OPPONENT_MOVE <move>
 * * YOUR_TURN
 * * VICTORY
 * * TIE
 * * DEFEAT
 * * ILLEGAL_MOVE
 * * VALID_MOVE
 * 
 * Messages from Client to Server
 * * MOVE <move>
 */

public class Server {

	public static void main(String[] args){
		ServerSocket listener = null;
		
		try{
			Parameter pars = new Parameter(args);
		
			int thres_move = pars.thres_move; //maximum number of moves per player
			int thres_time = pars.thres_time; //in milliseconds
			int port = pars.port;
			
			int playerBlackId = pars.blackId;
			int playerWhiteId = pars.whiteId;
	
			listener = new ServerSocket(port);
			System.out.println("Server started");
			
			Game game = new Game(thres_move, thres_time);
			Game.Player playerBlack = game.new Player(listener.accept(),
					Colour.Black, playerBlackId);
			Game.Player playerWhite = game.new Player(listener.accept(),
					Colour.White, playerWhiteId);
			
			game.setPlayers(playerBlack, playerWhite);
			
			game.turn = Colour.Black.ordinal();
			
			game.start();
		}catch(Exception e){
			if( e instanceof ParsingException){
				System.out.println("PARSING ERROR");
				System.out.println("Usage:");
				System.out.println("\t-tm <thres_move> (default 200 moves)");
				System.out.println("\t-tt <thres_time> (default 1000 ms)");
				System.out.println("\t-p <port> (default 8901)");
				System.out.println("\t-black <group_id> (default 0)");
				System.out.println("\t-white <group_id> (default 1)");
			}
			else
				e.printStackTrace();
			
		}
		 finally {
			try{
				listener.close();
			}catch(Exception ignored){}
		}
	}

}

class ParsingException extends Exception{
	private static final long serialVersionUID = 7646739985905674599L;	
}