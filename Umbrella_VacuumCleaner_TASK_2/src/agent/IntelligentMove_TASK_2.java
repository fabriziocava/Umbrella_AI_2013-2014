package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;

public class IntelligentMove_TASK_2 {
	
	final static int SUCK = 0;
	final static int LEFT = 1;
	final static int DOWN = 2;
	final static int RIGHT = 3;
	final static int UP = 4;
	final static int NoOP = 5;
	
	private LocalVacuumEnvironmentPerceptTaskEnvironmentB vep;
	
	private boolean firstMove;
	private int lastMovement=SUCK; /*variabile inizializzata ad un movimento onde evitare eccezioni se nella cella iniziare è presente una cella dirty*/
	
	private double THRESHOLD;
	private boolean isUnderThreshold;
	
	private int N;
	private int M;
	private MyCell[][] world;
	private Point agent;
	private boolean foundBase; /*true: se la base è stata trovata, false: altrimenti*/
	private Point base;
	
	private ArrayList<Integer> listMovements;
	private boolean canAddMovements;
	
	private ArrayList<Point> listDirtyCells;
	
	private UndirectedGraph<Point, DefaultEdge> graph;
	DijkstraShortestPath<Point, DefaultEdge> dsp;
	
	public IntelligentMove_TASK_2(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		this.vep=vep;
		
		firstMove = true;
		foundBase = false;
		
		THRESHOLD = vep.getInitialEnergy()*75/100; /*DA CAMBIARE IN BASE ALLA SIZE DELLA MAPPA*/
		isUnderThreshold = false;
		
		N = vep.getN();
		if(N%2!=0) N++;
		M = vep.getM();
		if(M%2!=0) M++;
		world = new MyCell [N*2][M*2];
		//initWorld();
		
		agent = new Point(N,M);
		
		listMovements = new ArrayList<Integer>();
		canAddMovements = true;
		
		listDirtyCells = new ArrayList<Point>();
		
		graph = new SimpleGraph<Point, DefaultEdge>(DefaultEdge.class);
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
			/**
			 * CASO IN CUI L'AGENTE PARTE DA UNA CELLA dirty
			 */
			/*
			if(vep.getState().getLocState()!=LocationState.Dirty) {
				//if(vep.isMovedLastTime())
					firstMove = false;
				movement = new Random().nextInt(5);
				while(movement==SUCK)
					movement = new Random().nextInt(5);
			}
			*/
			movement = new Random().nextInt(5);
			while(movement==SUCK)
				movement = new Random().nextInt(5);
		}
		else {
			if(foundBase) {
				generateGraph();
				int index;
				DijkstraShortestPath<Point, DefaultEdge> newDsp;
				while(listDirtyCells.size()!=0) {
					index=0;
					dsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, agent, listDirtyCells.get(index));
					for(int i=0; i<listDirtyCells.size(); i++) {
						newDsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, agent, listDirtyCells.get(i));
						if(newDsp.getPathLength()<dsp.getPathLength()) {
							dsp=newDsp;
							index=i;
						}
					}
				}
				//System.out.println(dsp.getPath());
				
				//movement = NoOP; /*cambiare strategia quando è conoscenza della base*/
			}
			else {
				movement = nextMoveToExploration();
			}
		}
		if(vep.getCurrentEnergy()<THRESHOLD)
			isUnderThreshold = true;
		if(!isUnderThreshold) {
			if(vep.getState().getLocState()==LocationState.Dirty) {
				movement=SUCK;
			}
		}
		if(vep.getCurrentEnergy()==0)
			movement = NoOP;
		if(canAddMovements)
			listMovements.add(movement);
		return movement;
	}
	
	/**
	 * return a point for the next coord given by move made
	 * 
	 * @param move the move made 
	 * @return a point for the next coord
	 */
	private Point getNextAgentCoord(int move){
				
		int x = (int)agent.getLocation().getX();
		int y = (int)agent.getLocation().getY();
		
		if(move==UP)
			x-=1;
		else if(move==DOWN)
			x+=1;
		else if(move==LEFT)
			y-=1;
		else if(move==RIGHT)
			y+=1;
		
		Point a = new Point(x,y);
		return a;
	}
				
	private boolean cellVisited(int move) {
		Point p = getNextAgentCoord(move);
		int x = (int)p.getX();
		int y = (int)p.getY();
		
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
		
		Point p = getNextAgentCoord(move);
		int x = (int)p.getX();
		int y = (int)p.getY();
		
		try {
			if(world[x][y].getState()==LocationState.Obstacle)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	private int nextMoveToExploration() {
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
		if(!isObstacleCell(movement) && !cellVisited(movement))
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
			canAddMovements = false;
			if(listMovements.get(index)==UP)
				movement=DOWN;
			else if(listMovements.get(index)==DOWN)
				movement=UP;
			else if(listMovements.get(index)==LEFT)
				movement=RIGHT;
			else if(listMovements.get(index)==RIGHT)
				movement=LEFT;
			listMovements.remove(index);
			lastMovement = movement;
		}
		else {
			canAddMovements = true;
			movement = nextMove.get(0);
		}
		return movement;
	}

	public void setVep(LocalVacuumEnvironmentPerceptTaskEnvironmentB newVep) {
		if(newVep.isMovedLastTime())
			firstMove=false;
		if(!foundBase) {
			if(newVep.isOnBase()) {
				base = new Point(agent);
				foundBase = true;
			}
		}
		int index = listMovements.size()-1;
		if(newVep.isMovedLastTime()) {
			setCell(vep.getState().getLocState());
			try {
				if(canAddMovements)
					setAgent(listMovements.get(index));
				else
					setAgent(lastMovement);
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
		if(state == LocationState.Dirty) {
			if(!containsPoint(agent))
				listDirtyCells.add(new Point(agent));
		}
	}
	
	public void setCell(Point p, LocationState state) {
		int x = (int)p.getX();
		int y = (int)p.getY();
		world[x][y]=new MyCell(state, true);
		if(state == LocationState.Dirty) {
			if(!containsPoint(p))
				listDirtyCells.add(new Point(p));
		}
	}
	
	public void setBase() {
		if(!foundBase)
			base = new Point(agent);
		foundBase=true;
	}
	
	private void setAgent(int move) {
		Point np = getNextAgentCoord(move);
		agent.setLocation(np.x, np.y);
	}
	
	private boolean containsPoint(Point p) {
		for(Point point : listDirtyCells) {
			if(point.getX()==p.getX() && point.getY()==p.getY())
				return true;
		}
		return false;
	}
	
	private void generateGraph() {
		for(int i=0; i<N*2; i++)
			for(int j=0; j<M*2; j++)
				graph.addVertex(new Point(i,j));
		Point p1, p2;
		for(int i=0; i<N*2; i++)
			for(int j=0; j<M*2-1; j++) {
				try {
					p1 = new Point(i,j);
					p2 = new Point(i,j+1);
					if(!(world[p1.x][p1.y].getState()==LocationState.Obstacle || world[p2.x][p2.y].getState()==LocationState.Obstacle))
						graph.addEdge(p1, p2);
					p1 = new Point(j,i);
					p2 = new Point(j+1,i);
					if(!(world[p1.x][p1.y].getState()==LocationState.Obstacle || world[p2.x][p2.y].getState()==LocationState.Obstacle))
						graph.addEdge(p1, p2);
				} catch (Exception e) {
					
				}
			}
	}
	
	public void print() {
		/*
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
		*/
		//System.out.println(listMovements);
		//System.out.println(base);
		System.out.println(listDirtyCells);
		//System.out.println(graph.edgeSet());
	}
	
}
