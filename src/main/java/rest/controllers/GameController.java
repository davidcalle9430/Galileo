package rest.controllers;


import java.util.Hashtable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.ConcurrentHashMap;

import entities.Board;
import entities.Game;
import entities.Piece;
import entities.Player;


/**
 * @author David Suarez
 *
 */
@RestController
public class GameController
{

/**--------------------------------------------------------------Attributtes--------------------------------------------------------------*/
	public final static String TOPIC_URI = "/topics/game";

	private Game game;
	private Map<Integer, Piece> eventsQueue;
	private boolean updatingQueue;
	private static int PUSH_COUNT = 0;
	private static int PULL_COUNT = 0;
	private Reviewer reviewer;
	private SimpMessagingTemplate template;


/**-------------------------------------------------------------------Comunicaction---------------------------------------------------------*/

	//---------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Constructor de GameController, donde instancia un objeto Game.
	 * @param template: Encargado de enviar mensajes usando WebSokets para actualizar la vista.
	 */
	@Autowired
	public GameController(SimpMessagingTemplate template) {
		this.game = new Game();  //QUE SE ACTAULIZAN AL ENTRAR.i
		this.eventsQueue =  new ConcurrentHashMap<>();       
        // new Hashtable<Integer, Piece>();
		this.updatingQueue = false;
		this.reviewer = new Reviewer();
		this.template = template;
		this.reviewer.start();
	}

	public synchronized boolean canObtainLock(){
		if( updatingQueue ){
			//updatingQueue = true;
			return true;
		}
		return false;
	}


	//---------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Agrega un nuevo evento, para luego notificar a la vista su respectiva actualizacion,
	 * a la cola de eventos controlando en no entrar en condicion de carrera.
	 */
	public synchronized void addEvent( Piece p )
	{
		this.eventsQueue.put( PUSH_COUNT++ , p );
	}

	//---------------------------------------------------------------------------------------------------------------------------------------
	public synchronized void sendGameUpdate( Game newGame )
	{
		template.convertAndSend( TOPIC_URI , newGame );
	}


/**--------------------------------------------------------------Creation-----------------------------------------------------------------*/
	//---------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Crea una instancia de Game para iniciar un nuevo juego.
	 * @return game: El Game creado.
	 */
	@CrossOrigin(origins="*")
	@RequestMapping(value="/api/game/new", method = RequestMethod.POST)
	public Game startNewGame(){
		this.game.setNewSuscriber(true );
		this.sendGameUpdate( game );
		return game;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Agrega el tablero que viene como datos en la peticion HTTP
	 * como un nuevo tablero reto al juego.
	 * @param board: El tablero a agregar.
	 */
	@CrossOrigin(origins="*")
	@RequestMapping(value="/api/board/new", method = RequestMethod.POST)
	public void startNewBoard( @RequestBody Board board )
	{
		this.game.addBoard( board );
		this.sendGameUpdate( game );
	}

	//---------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Delega al objeto Game agregar un nuevo jugador al juego y asociarle un tablero.
	 * Para esto le envia el objeto Player que viene como datos de la peticion HTTP.
	 * @param Player: El juagdor a agregar.
	 * @return p: Objeto Player que se agrego al juego.
	 * 			  Null si no se pudo agregar al jugador
	 * 			  debido a que no hay tableros disponibles.
	 */
	@CrossOrigin(origins="*")
	@RequestMapping(value="/api/player/new", method = RequestMethod.POST)
	public Player startNewPlayerPro( @RequestBody Player player ){
		Player p = this.game.addBoardToPlayer( player );

		if ( p != null )
			this.sendGameUpdate( game );

		return p;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Delega al objeto Game agregar un nuevo jugador al juego y asociarle un tablero.
	 * Para esto crea un Player con id @idPlayer y nombre @namePlayer y lo envia.
	 * @param idPlayer: Id del juagdor a agregar.
	 * @param namePlayer: Nombre del juagdor a agregar.
	 * @return p: Objeto Player que se agrego al juego.
	 * 			  Null si no se pudo agregar al jugador
	 * 			  debido a que no hay tableros disponibles.
	 */
	@CrossOrigin(origins="*")
	@RequestMapping(value="/api/player/{idPlayer}/new/{namePlayer}", method = RequestMethod.POST )
	public Player startNewPlayer( @PathVariable Integer idPlayer, @PathVariable String namePlayer ){
		Player player  = new Player( );
		player.setId( idPlayer );
		player.setName( namePlayer );
		player.setPoints( 0 );
		Player p = this.game.addBoardToPlayer( player );
		if ( p != null )
			this.sendGameUpdate( game );

		return p;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Notifica que ya se ha actualizado el nuevo suscriptor. //AUN NO LO USO
	 */
	@RequestMapping( value="/api/game/endUpdate" , method = RequestMethod.GET )
	public void gameStarted(){
		this.game.setNewSuscriber( false );
	}

/**--------------------------------------------------------------Assignment---------------------------------------------------------------*/

	/**
	 * Delega al objeto Game reasignar el tablero board a un Player con id idPlayer.
	 * Para luego actualizar la vista con la nueva configuracion
	 * del juagdor si el jugador existe.
	 * @param idPlayer: Identificador unico del jugador al que se le asignara el nuevo tablero.
	 * @param board: Instancia de Board, tablero que se le asiganara el jugador cuyo id es idPlayer.
	 * @return p: Objeto Player que posee el nuevo tablero asignado.
	 * 			  Null si el jugador con idPlayer no existe.
	 */
	@CrossOrigin(origins="*")
	@RequestMapping(value="/api/board/{idPlayer}/", method = RequestMethod.GET )
	public Board getCurrentBoard( @PathVariable Integer idPlayer  ){
		return this.game.getBoardByPlayer( idPlayer );
	}

	//-------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Delega al objeto Game reasignar el tablero board a un Player con id idPlayer.
	 * Para luego actualizar la vista con la nueva configuracion
	 * del juagdor si el jugador existe.
	 * @param idPlayer: Identificador unico del jugador al que se le asignara el nuevo tablero.
	 * @param board: Instancia de Board, tablero que se le asiganara el jugador cuyo id es idPlayer.
	 * @return p: Objeto Player que posee el nuevo tablero asignado.
	 * 			  Null si el jugador con idPlayer no existe.
	 */
	@CrossOrigin(origins="*")
	@RequestMapping(value="/api/player/{idPlayer}/challenge", method = RequestMethod.POST )
	public Player assignBoardToPlayer( @PathVariable Integer idPlayer, @RequestBody Board board) {
		Player p = this.game.assignBoardToPlayer( idPlayer, board );

		if ( p != null )
			this.sendGameUpdate( game );

		return p;
	}


/**--------------------------------------------------------------Movement---------------------------------------------------------------*/

	//-------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Delega al objeto Game cambiar la pieza blanca a la posicion
	 * indicada en el objeto Piece (atributo blank de Player).
	 * Para luego actualizar la vista con la nueva configuracion
	 * del tablero Taquin si el movimiento es valido.
	 * @param player: Objeto Player que contiene el tablero a actualizar.
	 * @return p: Objeto Player que posee el tablero al que se le movio la pieza blanca.
	 * 			   Null si el movimiento no es valido.
	 */
	/*@CrossOrigin(origins="*")
	@RequestMapping(value="/api/board/move", method = RequestMethod.POST)
	public Player movePieceOnBoardPro( @RequestBody Player player )
	{
		Player p = this.game.movePieceOnBoard( player, player.getBoard().getBlank() );

		if ( p != null )
			this.addEvent();

		return p;
	}*/

	/**
	 * Delega al objeto Game cambiar de posicion la pieza blanca hacia la derecha.
	 * Para luego actualizar la vista con la nueva configuracion del tablero Taquin. Si el movimiento es valido.
	 * @param idPlayer: Id del jugador que contiene el tablero a actualizar.
	 * @return p: Objeto Player que posee el tablero al que se le movio la pieza blanca.
	 * 			  Null si el movimiento no es valido.
	 */
	@CrossOrigin(origins="*")
	@RequestMapping(value="/api/player/{idPlayer}/board/move/right", method = RequestMethod.POST)
	public void movePieceOnBoardToRight( @PathVariable Integer idPlayer )
	{
		//Player p = this.game.movePieceOnBoardToRight( idPlayer );
		Piece p = new Piece( idPlayer, 0 );

		//if ( game.validateMovement( idPlayer, 0 ) )
		//{
			this.addEvent( p );
			//System.out.println("Metio DERECHA:\n");
		//}
	}

	/**
	 * Delega al objeto Game cambiar de posicion la pieza blanca hacia la izquierda.
	 * Para luego actualizar la vista con la nueva configuracion del tablero Taquin. Si el movimiento es valido.
	 * @param idPlayer: Id del jugador que contiene el tablero a actualizar.
	 * @return p: Objeto Player que posee el tablero al que se le movio la pieza blanca.
	 * 			  Null si el movimiento no es valido.
	 */
	@CrossOrigin(origins="*")
	@RequestMapping(value="/api/player/{idPlayer}/board/move/left", method = RequestMethod.POST)
	public void movePieceOnBoardToLeft( @PathVariable Integer idPlayer )
	{
		//Player p = this.game.movePieceOnBoardToLeft( idPlayer );
		Piece p = new Piece( idPlayer, 1 );

		//if ( game.validateMovement(idPlayer, 1) )
		//{
			this.addEvent( p );
			//System.out.println("Metio IZQUIERDA:\n");
		//}
	}

	/**
	 * Delega al objeto Game cambiar de posicion la pieza blanca hacia arriba.
	 * Para luego actualizar la vista con la nueva configuracion del tablero Taquin. Si el movimiento es valido.
	 * @param idPlayer: Id del jugador que contiene el tablero a actualizar.
	 * @return p: Objeto Player que posee el tablero al que se le movio la pieza blanca.
	 * 			  Null si el movimiento no es valido.
	 */
	@CrossOrigin(origins="*")
	@RequestMapping(value="/api/player/{idPlayer}/board/move/up", method = RequestMethod.POST)
	public void movePieceOnBoardToUp( @PathVariable Integer idPlayer )
	{
		//Player p = this.game.movePieceOnBoardToUp( idPlayer );
		Piece p = new Piece( idPlayer, 2 );

		//if ( game.validateMovement(idPlayer, 2 ) )
		//{
			this.addEvent( p );
			//System.out.println("Metio ARRIBA:\n");
		//}
	}

	/**
	 * Delega al objeto Game cambiar de posicion la pieza blanca hacia abajo
	 * Para luego actualizar la vista con la nueva configuracion del tablero Taquin. Si el movimiento es valido.
	 * @param idPlayer: Id del jugador que contiene el tablero a actualizar.
	 * @return p: Objeto Player que posee el tablero al que se le movio la pieza blanca.
	 * 			  Null si el movimiento no es valido.
	 */
	@CrossOrigin(origins="*")
	@RequestMapping(value="/api/player/{idPlayer}/board/move/down", method = RequestMethod.POST)
	public void movePieceOnBoardToDown( @PathVariable Integer idPlayer )
	{
		//Player p = this.game.movePieceOnBoardToDown( idPlayer );
		Piece p = new Piece( idPlayer, 3 );

		//if ( game.validateMovement(idPlayer, 3 ) )
		//{
			this.addEvent( p );
			//System.out.println("Metio ABAJO:\n");
		//}

	}

	public class Reviewer extends Thread
	{

		private boolean running;
		private final static int UPDATE_TIME = 400;
		public Reviewer()
		{
	      this.running = true;
		}

	    @Override
	    public void run()
	    {
	    	while ( this.running )
	    	{
	    		while( !updatingQueue )//{//System.out.println("esperando para sacar");};
		    	{
		    		//System.out.println("esperando para sacar");
		        	if( eventsQueue.size() > 0 )
		        	{
		        		updatingQueue = true;
		        		Piece p = eventsQueue.get( PULL_COUNT );
			        	eventsQueue.remove( PULL_COUNT++ );
			        	int typeMovement = p.getColumn();
			        	switch( typeMovement )
			        	{
				    	    case 0:
				    	    	//System.out.println("SACO TIPO MOV: DERECHA");
				    	    	game.movePieceOnBoardToRight( p.getRow() );
				    	        break;
				    	    case 1:
			        			//System.out.println("SACO TIPO MOV: IZQUIERDA");
				    	    	game.movePieceOnBoardToLeft( p.getRow() );
				    	        break;
				    	    case 2:
			        			//System.out.println("SACO TIPO MOV: ARRIBA");
				    	    	game.movePieceOnBoardToUp( p.getRow() );
				    	        break;
				    	    case 3:
			        			//System.out.println("SACO TIPO MOV: ABAJO");
				    	    	game.movePieceOnBoardToDown( p.getRow() );
					    	    break;
				        }
				        sendGameUpdate( game );
			        	updatingQueue = false;
		        	}
			    	try 
			        {
						Thread.sleep( UPDATE_TIME );
					}
			        catch (InterruptedException e) 
			        {
						e.printStackTrace();
					}
			   }
		       try 
		       {
		    	   Thread.sleep( UPDATE_TIME );
		       }
		        catch (InterruptedException e)
		       {
					e.printStackTrace();
				}
	    	}
	    }

	   public void stopReview()
	   {
		   this.running = false;
	   }
	}
}
