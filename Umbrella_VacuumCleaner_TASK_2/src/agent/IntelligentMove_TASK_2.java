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
		
	private double THRESHOLD;
	private boolean isOverThreshold;
	
	private int N;
	private int M;
	private MyCell[][] world;
	private Point agent;
	private Point base;
	
	private ArrayList<Integer> listMovements;
	
	private LocalVacuumEnvironmentPerceptTaskEnvironmentB vep;
	
	public IntelligentMove_TASK_2(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		this.vep=vep;
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
		int randomInt = new Random().nextInt(5);
		int i=0;
		/*ATTENZIONE: modificare*/
		while(cellVisited(randomInt) && i<5) {
			i++;
			randomInt = new Random().nextInt(5);
		}
		while(randomInt==SUCK)
			randomInt = new Random().nextInt(5);
		int movement = randomInt;
		if(vep.getCurrentEnergy()<THRESHOLD)
			isOverThreshold = true;
		if(!isOverThreshold) {
			if(vep.getState().getLocState()==LocationState.Dirty) {
				movement=SUCK;
			}
		}
		if(vep.getCurrentEnergy()==0)
			movement = NoOP;
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
				if(listMovements.get(index)==SUCK)
					setCell(LocationState.Clean);
				else
					setCell(LocationState.Obstacle);
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
	
	public void setBase() {
		base = new Point(agent);
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
