package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;

public class IntelligentMove_TASK_2 {
	
	final static int SUCK = 0;
	final static int LEFT = 1;
	final static int DOWN = 2;
	final static int RIGHT = 3;
	final static int UP = 4;
	final static int NoOP = 5;
	
	private boolean firstMove;
	//private int lastMovement=SUCK; /*variabile inizializzata ad un movimento onde evitare eccezioni se nella cella iniziare è presente una cella dirty*/
	
	private double THRESHOLD;
	private boolean isOverThreshold;
	
	private int N;
	private int M;
	private MyCell[][] world;
	private Point agent;
	private boolean foundBase; /*true: se la base è stata trovata, false: altrimenti*/
	private Point base;
	
	private ArrayList<Integer> listMovements;
	
	private LocalVacuumEnvironmentPerceptTaskEnvironmentB vep;
	
	public IntelligentMove_TASK_2(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		this.vep=vep;
		
		firstMove = true;
		foundBase = false;
		
		THRESHOLD = vep.getInitialEnergy()/2; /*DA CAMBIARE IN BASE ALLA SIZE DELLA MAPPA*/
		isOverThreshold = false;
		
		N = vep.getN();
		if(N%2!=0) N++;
		M = vep.getM();
		if(M%2!=0) N++;
		world = new MyCell [N*2][M*2];
		//initWorld();
		
		agent = new Point(N,M);
		
		listMovements = new ArrayList<Integer>();
	}
	
	private void initWorld() {
		for(int i=0; i<N*2; i++) 
			for(int j=0; j<M*2; j++)
				world[i][j] = new MyCell(LocationState.Clean, false);
	}
	
	public int getMovement() {
		new Random();
		int movement=0;
		if(firstMove) {
			firstMove = false;
			movement = new Random().nextInt(5);
			while(movement==SUCK)
				movement = new Random().nextInt(5);
		}
		else {
			/*
			if(vep.isMovedLastTime())
				movement = nextMoveToCellNotVisited();
			else {
				if(listMovements.get(listMovements.size()-1)!=SUCK) {
					//movement = new Random().nextInt(5);
					//while(movement==SUCK || movement==lastMovement || isObstacleCell(movement) || movement!=nextMoveToCellNotVisited())
					//	movement = new Random().nextInt(5);
					
					movement = nextMoveToCellNotVisited();
				}
				else {
					movement = lastMovement;
				}
			}
			*/
			movement = nextMoveToCellNotVisited();
			if(vep.getCurrentEnergy()<THRESHOLD)
				isOverThreshold = true;
			if(!isOverThreshold) {
				if(vep.getState().getLocState()==LocationState.Dirty) {
					movement=SUCK;
				}
			}
		}
		if(vep.getCurrentEnergy()==0)
			movement = NoOP;
		//if(movement!=SUCK)
			//lastMovement = movement;
		listMovements.add(movement);
		return movement;
	}
	
	private boolean cellVisited(int move) {
		int x = (int)agent.getX();
		int y = (int)agent.getY();
		if(move==UP)
			x-=1;
		else if(move==DOWN)
			x+=1;
		else if(move==LEFT)
			y-=1;
		else if(move==RIGHT)
			y+=1;
		try {
			if(world[x][y].isVisited())
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean isObstacleCell(int move) {
		int x = (int)agent.getX();
		int y = (int)agent.getY();
		if(move==UP)
			x-=1;
		else if(move==DOWN)
			x+=1;
		else if(move==LEFT)
			y-=1;
		else if(move==RIGHT)
			y+=1;
		try {
			if(world[x][y].getState()==LocationState.Obstacle)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	private int nextMoveToCellNotVisited() {
		int index = listMovements.size()-1;
		int movement=listMovements.get(index);
		int x = (int)agent.getX();
		int y = (int)agent.getY();
		int northX = x-1;
		int northY = y;
		int southX = x+1;
		int southY = y;
		int westX = x;
		int westY = y-1;
		int eastX = x;
		int eastY = y+1;
		ArrayList<Integer> nextMove = new ArrayList<Integer>();
		if(movement!=SUCK && !isObstacleCell(movement) && !cellVisited(movement))
			nextMove.add(movement);
		try {
			if(!world[northX][northY].isVisited()) {
				nextMove.add(UP);
			}
		} catch (Exception e) {
			nextMove.add(UP);
		}
		try {
			if(!world[southX][southY].isVisited()) {
				nextMove.add(DOWN);
			}
		} catch (Exception e) {
			nextMove.add(DOWN);
		}
		try {
			if(!world[westX][westY].isVisited()) {
				nextMove.add(LEFT);
			}
		} catch (Exception e) {
			nextMove.add(LEFT);
		}
		try {
			if(!world[eastX][eastY].isVisited()) {
				nextMove.add(RIGHT);
			}
		} catch (Exception e) {
			nextMove.add(RIGHT);
		}
		new Random();
		if(nextMove.size()==0) {
			/*
			movement = lastMovement;
			while(movement==SUCK || isObstacleCell(movement))
				movement = new Random().nextInt(5);
			System.out.println(movement + " <--- MOVE");
			*/
			if(listMovements.get(index)==SUCK) { /* controllo superfluo */
				listMovements.remove(index);
				index=listMovements.size()-1;
			}
			if(listMovements.get(index)==UP)
				movement=DOWN;
			else if(listMovements.get(index)==DOWN)
				movement=UP;
			else if(listMovements.get(index)==LEFT)
				movement=RIGHT;
			else if(listMovements.get(index)==RIGHT)
				movement=LEFT;
			listMovements.remove(index);
		}
		else {
			movement = nextMove.get(0);
		}
		//System.out.println(nextMove);
		return movement;
	}

	public void setVep(LocalVacuumEnvironmentPerceptTaskEnvironmentB newVep) {
		if(vep.isOnBase()) {
			base = new Point(agent);
			System.out.println(base);
		}
		int index = listMovements.size()-1;
		if(newVep.isMovedLastTime()) {
			setCell(vep.getState().getLocState());
			try {
				setAgent(listMovements.get(index));
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
		else {
			try {
				if(listMovements.get(index)==SUCK) {
					setCell(LocationState.Clean);
					listMovements.remove(index);
				}
				else {
					Point p = null;
					if(listMovements.get(index)==UP)
						p = new Point((int)agent.getX()-1, (int)agent.getY());
					else if(listMovements.get(index)==DOWN)
						p = new Point((int)agent.getX()+1, (int)agent.getY());
					else if(listMovements.get(index)==LEFT)
						p = new Point((int)agent.getX(), (int)agent.getY()-1);
					else if(listMovements.get(index)==RIGHT)
						p = new Point((int)agent.getX(), (int)agent.getY()+1);
					if(listMovements.get(index)!=SUCK)
						setCell(p,LocationState.Obstacle);
					listMovements.remove(index);
				}
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
		this.vep=newVep;
	}

	
	public void setCell(LocationState state) {
		int x = (int)agent.getX();
		int y = (int)agent.getY();
		world[x][y]=new MyCell(state, true);
	}
	
	public void setCell(Point p, LocationState state) {
		int x = (int)p.getX();
		int y = (int)p.getY();
		world[x][y]=new MyCell(state, true);
	}
	
	public void setBase() {
		if(!foundBase)
			base = new Point(agent);
		foundBase=true;
	}
	
	private void setAgent(int move) {
		int x = (int)agent.getX();
		int y = (int)agent.getY();
		if(move==UP)
			x-=1;
		else if(move==DOWN)
			x+=1;
		else if(move==LEFT)
			y-=1;
		else if(move==RIGHT)
			y+=1;
		agent.setLocation(x, y);
	}
	
	public void print() {
		for(int i=0; i<N*2; i++) {
			for(int j=0; j<M*2; j++) {
				try {
					System.err.print(world[i][j].toString()+" ");
				} catch (Exception e) {
					System.err.print("NaN ");
				}
			}
			System.err.println();
		}
	}
	
}
