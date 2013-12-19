package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import aima.core.agent.Action;
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
	
	private ArrayList<Integer> stackOfMovements;
	private boolean canAddMovements;
	
	private ArrayList<Point> listDirtyCells;
	
	private UndirectedGraph<Point, DefaultEdge> graph;
	/** Lista movimenti per pulire le celle dirty una volta trovata la base.*/
	private ArrayList<Integer> listMovementsToCleanOrReturnToBase;
	
	private double costSuck;
	/**TRUE se sto tornando alla base, FALSE altrimenti.*/
	private boolean returnToBase; 
	
	private ArrayList<Integer> ultimateReturnToBase;
	
	public IntelligentMove_TASK_2(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		this.vep=vep;
		
		firstMove = true;
		foundBase = false;
		
		THRESHOLD = vep.getInitialEnergy()*75/100;
		if(vep.getCurrentEnergy()<THRESHOLD)
			isUnderThreshold = true;
		else
			isUnderThreshold = false;
	
		N = vep.getN();
		N++;
		if(N%2!=0) N++;
		M = vep.getM();
		M++;
		if(M%2!=0) M++;
		world = new MyCell [N*2][M*2];
		//initWorld();
		
		agent = new Point(N,M);
		
		stackOfMovements = new ArrayList<Integer>();
		canAddMovements = true;
		
		listDirtyCells = new ArrayList<Point>();
		
		graph = new SimpleGraph<Point, DefaultEdge>(DefaultEdge.class);
		listMovementsToCleanOrReturnToBase = new ArrayList<Integer>();
		ultimateReturnToBase = new ArrayList<Integer>();
		
		try {
			Set<Action> actionsKeySet = vep.getActionEnergyCosts().keySet();
			costSuck=vep.getActionEnergyCosts().get(actionsKeySet.iterator().next());
		} catch (Exception e) {
			costSuck = 1;
		}
		returnToBase = false;
	}
	
	private void initWorld() {
		for(int i=0; i<N*2; i++) 
			for(int j=0; j<M*2; j++)
				world[i][j] = new MyCell(LocationState.Clean, false);
	}
	
	public int getMovement() {
		new Random();
		int move=0;

		if(firstMove) {
			move = new Random().nextInt(5);
			while(move==SUCK || isObstacleCell(move))
				move = new Random().nextInt(5);
            if(!isUnderThreshold) {
                if(vep.getState().getLocState()==LocationState.Dirty) {
                        move=SUCK;
                }
            }
		}//if(firstMove)
		else {
			if(foundBase) {
				if(!listMovementsToCleanOrReturnToBase.isEmpty()) {
					move = listMovementsToCleanOrReturnToBase.get(0);
					listMovementsToCleanOrReturnToBase.remove(0);
				}
				else if(!ultimateReturnToBase.isEmpty()) {
					move = ultimateReturnToBase.get(0);
					ultimateReturnToBase.remove(0);					
				}
				else {
					setCell(vep.getState().getLocState()); // non era sincronizzato con la posizine dell'agente
					generateGraph();
					if(!listDirtyCells.isEmpty()) {
						DijkstraShortestPath<Point, DefaultEdge> dsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, agent, listDirtyCells.get(0));
						DijkstraShortestPath<Point, DefaultEdge> newDsp;
						int index=0;
						for(int i=1; i<listDirtyCells.size(); i++) {
							newDsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, agent, listDirtyCells.get(i));
							if(newDsp.getPathLength()<dsp.getPathLength()) {
								dsp=newDsp;
								index=i;
							}
						}
						DijkstraShortestPath<Point, DefaultEdge> dspToBase = new DijkstraShortestPath<Point, DefaultEdge>(graph, agent, base);
						ArrayList<DefaultEdge> de_list;
						Point currentPoint = new Point(agent);
						ArrayList<Point> listPoints = new ArrayList<Point>();
						double energyRequired = dsp.getPathLength()*2+dspToBase.getPathLength()+costSuck;
						if(energyRequired<=vep.getCurrentEnergy()) {
							listPoints.add(currentPoint);
							de_list = (ArrayList<DefaultEdge>) dsp.getPath().getEdgeList();
							for(DefaultEdge de : de_list) {
								Point pTarget = graph.getEdgeTarget(de);
								if(pTarget.equals(currentPoint))
									pTarget = graph.getEdgeSource(de);
								listPoints.add(pTarget);
								currentPoint = pTarget;
							}
							addListMovementsToCleanOrReturnToBase(listPoints);
							/**
							 * Elimina la cella dirty dalla lista
							 */
							listDirtyCells.remove(index);
						}
						else {
							listDirtyCells = new ArrayList<Point>();
							if(agent.getX()==base.getX() && agent.getY()==base.getY())
								move = NoOP;
							else {
								returnToBase = true;
								listPoints.add(currentPoint);
								de_list = (ArrayList<DefaultEdge>) dspToBase.getPath().getEdgeList();
								for(DefaultEdge de : de_list) {
									Point pTarget = graph.getEdgeTarget(de);
									if(pTarget.equals(currentPoint))
										pTarget = graph.getEdgeSource(de);
									listPoints.add(pTarget);
									currentPoint = pTarget;
								}
								addListMovementsToCleanOrReturnToBase(listPoints);
							}
						}
						/*
						 * Prima mossa
						 */
						if(!listMovementsToCleanOrReturnToBase.isEmpty()) {
							move = listMovementsToCleanOrReturnToBase.get(0);
							listMovementsToCleanOrReturnToBase.remove(0);
						}
					}//if(!listDirtyCells.isEmpty()) 
					else { /* esplorazione nei pressi della base se ho ancora energia */
						move = nextMoveToExploration();
	                    if(vep.getState().getLocState()==LocationState.Dirty) {
                            move=SUCK;
	                    }
	                    if(move!=SUCK) {
	                    	double energyRequired;
	                    	if(agent.getX()==base.getX() && agent.getY()==base.getY()) {
	                    		energyRequired = 1.0*2 + costSuck;
	                    		if(energyRequired>vep.getCurrentEnergy())
	                    			move = NoOP;
	                    	}
	                    	else { //DA VERIFICARE   
	                    		//!!! pare non entrare qua dentro quando finisce di pulire le celle che aveva lasciato sporche...
	                    		// ricontrollare stampando l'energia:  istanza "prova1" quando all'inizio va a sinista...(o su)
	                    		
								Point nextPoint = getNextAgentCoord(move);
			                    DijkstraShortestPath<Point, DefaultEdge> dspReturnToBase = new DijkstraShortestPath<Point, DefaultEdge>(graph, agent, base);
			                    energyRequired = dspReturnToBase.getPathLength()+1.0*2+costSuck;
			                    if(energyRequired>vep.getCurrentEnergy()) {
			    					ArrayList<DefaultEdge> de_list;
									Point currentPoint = new Point(agent);
									ArrayList<Point> listPoints = new ArrayList<Point>();
									listPoints.add(currentPoint);
																
									de_list = (ArrayList<DefaultEdge>) dspReturnToBase.getPath().getEdgeList();
									for(DefaultEdge de : de_list) {
										Point pTarget = graph.getEdgeTarget(de);
										if(pTarget.equals(currentPoint))
											pTarget = graph.getEdgeSource(de);
										listPoints.add(pTarget);
										currentPoint = pTarget;
									}
									addListMovementsToUltimateReturnToBase(listPoints);
									//Prima mossa
									if(!ultimateReturnToBase.isEmpty()) {
										move = ultimateReturnToBase.get(0);
										ultimateReturnToBase.remove(0);
									}
			    				}
			    				
	                    	}
	    				}
					}
				}// listMovementsToCleanOrReturnToBase & ultimateReturnToBase EMPTY
			}//if(foundBase)
			else {
				move = nextMoveToExploration();
                if(!isUnderThreshold) {
                    if(vep.getState().getLocState()==LocationState.Dirty)
                            move=SUCK;
                }
			}
		}////if(!firstMove)
		if(vep.getCurrentEnergy()==0)
			move = NoOP;
		if(canAddMovements)
			stackOfMovements.add(move);
		return move;
	}
	
	private void addListMovementsToCleanOrReturnToBase(ArrayList<Point> listPoints) {
		for(int i=0; i<listPoints.size()-1; i++) {
			listMovementsToCleanOrReturnToBase.add(neighborhood(listPoints.get(i), listPoints.get(i+1)));
		}
		if(!returnToBase)
			listMovementsToCleanOrReturnToBase.add(SUCK);
	}
	
	private void addListMovementsToUltimateReturnToBase(ArrayList<Point> listPoints) {
		for(int i=0; i<listPoints.size()-1; i++) {
			ultimateReturnToBase.add(neighborhood(listPoints.get(i), listPoints.get(i+1)));
		}
		ultimateReturnToBase.add(NoOP);
	}
	
	private int neighborhood(Point p1, Point p2) {
		// p1 to p2
		int move=0;
		double p1_x = p1.getX();
		double p1_y = p1.getY();
		double p2_x = p2.getX();
		double p2_y = p2.getY();
		if(p1_x<p2_x)
			move=DOWN;
		else if(p1_x>p2_x)
			move=UP;
		else if(p1_y<p2_y)
			move=RIGHT;
		else if(p1_y>p2_y)
			move=LEFT;
		return move;
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
		int index = stackOfMovements.size()-1;
		int movement=lastMovement;
		new Random();
		try {
			movement=stackOfMovements.get(index);
		} catch (Exception e) {
			while(movement==SUCK || isObstacleCell(movement))
				movement = new Random().nextInt(5);
		}
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
		if(nextMove.size()==0) {
			canAddMovements = false;
			try {
				if(stackOfMovements.get(index)==UP)
					movement=DOWN;
				else if(stackOfMovements.get(index)==DOWN)
					movement=UP;
				else if(stackOfMovements.get(index)==LEFT)
					movement=RIGHT;
				else if(stackOfMovements.get(index)==RIGHT)
					movement=LEFT;
				stackOfMovements.remove(index);
			} catch (Exception e) {
				while(movement==SUCK || isObstacleCell(movement))
					movement = new Random().nextInt(5);
			}
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
		if(newVep.getCurrentEnergy()<THRESHOLD)
			isUnderThreshold = true;
		int index = stackOfMovements.size()-1;
		if(newVep.isMovedLastTime()) {
			setCell(vep.getState().getLocState());
			try {
				if(canAddMovements)
					setAgent(stackOfMovements.get(index));
				else
					setAgent(lastMovement);
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
		else {
			try {
				if(stackOfMovements.get(index)==SUCK) {
					setCell(LocationState.Clean);
					stackOfMovements.remove(index);
				}
				else {
					Point p = null;
					if(stackOfMovements.get(index)==UP)
						p = new Point((int)agent.getX()-1, (int)agent.getY());
					else if(stackOfMovements.get(index)==DOWN)
						p = new Point((int)agent.getX()+1, (int)agent.getY());
					else if(stackOfMovements.get(index)==LEFT)
						p = new Point((int)agent.getX(), (int)agent.getY()-1);
					else if(stackOfMovements.get(index)==RIGHT)
						p = new Point((int)agent.getX(), (int)agent.getY()+1);
					setCell(p,LocationState.Obstacle);
					stackOfMovements.remove(index);
				}
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
		if(!foundBase) {
			if(newVep.isOnBase()) {
				base = new Point(agent);
				foundBase = true;
				setCell(base, LocationState.Clean);
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
		graph = new SimpleGraph<Point, DefaultEdge>(DefaultEdge.class);
		for(int i=0; i<N*2; i++)
			for(int j=0; j<M*2; j++)
				graph.addVertex(new Point(i,j));
		Point p1, p2;
		for(int i=0; i<N*2; i++)
			for(int j=0; j<M*2-1; j++) {
				// a ogni eccezione del primo il secondo non veniva calcolato...
				try {
					p1 = new Point(i,j);
					p2 = new Point(i,j+1);
					if(!((world[p1.x][p1.y].getState()==LocationState.Obstacle) || (world[p2.x][p2.y].getState()==LocationState.Obstacle)))
						graph.addEdge(p1, p2);
				} catch (Exception e) {	}
				
				try {
					//dovrebbero funzionare entrambi
//					p1 = new Point(j,i);	p2 = new Point(j+1,i);
					p1 = new Point(i,j);	p2 = new Point(i+1,j);					
					if(!((world[p1.x][p1.y].getState()==LocationState.Obstacle) || (world[p2.x][p2.y].getState()==LocationState.Obstacle)))
						graph.addEdge(p1, p2);
				} catch (Exception e) {	}
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
		//System.out.println("BASE: " + agent);
		//System.out.println(listMovements);
		//System.out.println("DIRTY CELLS: " + listDirtyCells);
		//System.out.println(graph.edgeSet()); //OK
	}
	
}
