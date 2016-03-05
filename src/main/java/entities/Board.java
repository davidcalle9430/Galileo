package entities;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Board {
	
	public final static Integer BOARD_SIZE = 3;
	
	private String[][] currentState;
	
	@JsonIgnore
	private String[][] solution;
	
	private Integer movements;
	
	
	
	public Board(){
		movements = 0;
		currentState = new String [BOARD_SIZE][BOARD_SIZE];
		solution = new String [BOARD_SIZE][BOARD_SIZE];
		for (int i = 0; i < BOARD_SIZE ; i++) {
			for (int j = 0; j < BOARD_SIZE ; j++) {
				Random rnumber = new Random();
				int number	 = rnumber.nextInt(10) + 1; 
				currentState[i][j] = number+"";
				solution[i][j] = number + "";
			}
		}
	}
	
	

	public Integer getMovements() {
		return movements;
	}



	public void setMovements(Integer movements) {
		this.movements = movements;
	}



	public String[][] getCurrentState() {
		return currentState;
	}

	public void setCurrentState(String[][] currentState) {
		this.currentState = currentState;
	}

	public String[][] getSolution() {
		return solution;
	}

	public void setSolution(String[][] solution) {
		this.solution = solution;
	}
	
	
	public void addMovement(){
		movements++;
	}

}
