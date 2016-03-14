package entities;

public class Player {
	public final static Integer PLAYER_1 = 1;
	public final static Integer PLAYER_2 = 2	;
	
	private Integer id;
	private String name;
	private Integer points;
	private Board board;
	
	
	public Player()
	{
		
	}

	/**
	 * @return the id
	 */
	public Integer getId() 
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) 
	{
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() 
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the points
	 */
	public Integer getPoints()
	{
		return points;
	}

	/**
	 * @param points the points to set
	 */
	public void setPoints(Integer points) 
	{
		this.points = points;
	}

	/**
	 * @return the board
	 */
	public Board getBoard() 
	{
		return board;
	}

	/**
	 * @param board the board to set
	 */
	public void setBoard(Board board)
	{
		this.board = board;
	}

	
	
}
