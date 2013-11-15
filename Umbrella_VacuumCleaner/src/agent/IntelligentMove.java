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
import aima.core.agent.Agent;
import core.LocalVacuumEnvironmentPercept;
import core.VacuumEnvironment.LocationState;

public class IntelligentMove {
	
	final static int SUCK = 0;
	final static int LEFT = 1;
	final static int DOWN = 2;
	final static int RIGHT = 3;
	final static int UP = 4;
	final static int NoOP = 5;
	
	private LocalVacuumEnvironmentPercept vep;
	private Set<Action> actionsKeySet;
	private int n;
	private UndirectedGraph<Point, DefaultEdge> graph;
	private double costSuck;
	
	public IntelligentMove(LocalVacuumEnvironmentPercept vep) {
		this.vep=vep;
		actionsKeySet = vep.getActionEnergyCosts().keySet();
		n = vep.getN();
		graph = new SimpleGraph<Point, DefaultEdge>(DefaultEdge.class);
		generateGraph();
		try {
			costSuck=vep.getActionEnergyCosts().get(actionsKeySet.iterator().next());
		}
		catch (Exception e) {
			costSuck=2;
		}
	}

	
	private void generateGraph() {
		Point p1 = new Point();
		Point p2 = new Point();
		for(int i=0; i<n; i++)
			for(int j=0; j<n; j++) {
				p1=new Point();
				p1.setLocation(i,j);
				graph.addVertex(p1);
			}
		//System.out.println(graph.vertexSet());
		for(int i=0; i<n; i++) {
			for(int j=0; j<n-1; j++) {
				p1=new Point(i,j);
				p2=new Point(i,j+1);
				if(!(vep.getState().get(p1).equals(LocationState.Obstacle)||vep.getState().get(p2).equals(LocationState.Obstacle)))
					graph.addEdge(p1, p2);
				//System.err.println("("+j+","+i+")"+"-->"+"("+(j+1)+","+i+")");
				p1=new Point(j,i);
				p2=new Point(j+1,i);
				if(!(vep.getState().get(p1).equals(LocationState.Obstacle)||vep.getState().get(p2).equals(LocationState.Obstacle)))
					graph.addEdge(p1, p2);
			}
		}
		
	}
	
	private ArrayList<Point> getDirtyCell() {
		ArrayList<Point> list = new ArrayList<Point>();
		Point p=new Point();
		for(int i=0; i<n; i++)
			for(int j=0; j<n; j++) {
				p=new Point(i,j);
				if(vep.getState().get(p).equals(LocationState.Dirty))
					list.add(p);
			}
		//System.out.println(list);
		return list;
	}
	
	public int move() {
		int moveInt;
		if(vep.getState().get(vep.getAgentLocation()).equals(LocationState.Dirty))
			moveInt=IntelligentMove.SUCK;
		else {
			//moveInt = new Random().nextInt(actionsKeySet.size());
			moveInt = nextMove_dijkstra();
		}
		//System.err.println(vep.getAgentLocation());
		return moveInt;
	}
	
	public ArrayList<Integer> getListMovements() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		ArrayList<Point> listPoints = getListPoints();
		for(int i=0; i<listPoints.size()-1; i++) {
			if(vep.getState().get(listPoints.get(i)).equals(LocationState.Dirty))
				list.add(IntelligentMove.SUCK);
			list.add(neighborhood(listPoints.get(i), listPoints.get(i+1)));
		}
		//System.err.println(list.toString());
		//list.add(IntelligentMove.NoOP);
		return list;
	}
	
	private int nextMoveNeighborhood() {
		int move=0;
		while(move==0) {
			move=new Random().nextInt(actionsKeySet.size());
		}
		/*
		 * neighborhood
		 */
		double x = vep.getAgentLocation().getX();
		double y = vep.getAgentLocation().getY();
		double north=x-1;
		if(north<0) 
			north=0;
		double south=x+1;
		if(south==n)
			south=n-1;
		double west=y-1;
		if(west<0)
			west=0;
		double east=y+1;
		if(east==n)
			east=n-1;
		Point pN = new Point();
		pN.setLocation(north, y);
		Point pS = new Point();
		pS.setLocation(south, y);
		Point pW = new Point();
		pW.setLocation(x, west);
		Point pE = new Point();
		pE.setLocation(x, east);
		if(vep.getState().get(pN).equals(LocationState.Dirty))
			move=IntelligentMove.UP;
		else if(vep.getState().get(pS).equals(LocationState.Dirty))
			move=IntelligentMove.DOWN;
		else if(vep.getState().get(pW).equals(LocationState.Dirty))
			move=IntelligentMove.LEFT;
		else if(vep.getState().get(pE).equals(LocationState.Dirty))
			move=IntelligentMove.RIGHT;
		return move;
	}
	
	private int nextMove_dijkstra() {
		int move=0;
		ArrayList<Point> dirtyList = getDirtyCell();
		if(dirtyList.size()==0 && vep.getAgentLocation().equals(vep.getBaseLocation())) {
			move=IntelligentMove.NoOP;	
		}
		else if(dirtyList.size()!=0) {
			DijkstraShortestPath<Point, DefaultEdge> dsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, vep.getAgentLocation(), dirtyList.get(0));
			DijkstraShortestPath<Point, DefaultEdge> newDsp;
			for(Point dl : dirtyList) {
				newDsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, vep.getAgentLocation(), dl);
				if(newDsp.getPathLength()<dsp.getPathLength())
					dsp=newDsp;
			}
			//System.out.println(dsp.getPath().getEndVertex());
			if(dsp.getPathLength()==1.0)
				move = neighborhood(dsp.getPath().getStartVertex(), dsp.getPath().getEndVertex());
			else {
				System.err.println(dsp.getPath().toString());
				ArrayList<DefaultEdge> de_list = (ArrayList<DefaultEdge>) dsp.getPath().getEdgeList();			
				for(DefaultEdge de : de_list) {
					Point pTarget = graph.getEdgeTarget(de);
					if(pTarget.equals(vep.getAgentLocation()))
						pTarget = graph.getEdgeSource(de);
					move = neighborhood(vep.getAgentLocation(), pTarget);
					System.err.println("MOVE F: " + move);
				}
			}
		}
		else {
			/*
			 * Come back to base
			 * NOTA: il seguente codice è uguale a quello precente --> risorvere
			 */
			DijkstraShortestPath<Point, DefaultEdge> dsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, vep.getAgentLocation(), vep.getBaseLocation());
			if(dsp.getPathLength()==1.0)
				move = neighborhood(dsp.getPath().getStartVertex(), dsp.getPath().getEndVertex());
			else {
				//System.err.println(dsp.getPath().toString());
				ArrayList<DefaultEdge> de_list = (ArrayList<DefaultEdge>) dsp.getPath().getEdgeList();			
				for(DefaultEdge de : de_list) {
					Point pTarget = graph.getEdgeTarget(de);
					if(pTarget.equals(vep.getAgentLocation()))
						pTarget = graph.getEdgeSource(de);
					move = neighborhood(vep.getAgentLocation(), graph.getEdgeTarget(de));				
				}
			}
		}
		return move;
	}
	
	private int neighborhood(Point p1, Point p2) {
		// p1 to p2
		int move=0;
		//System.err.println(p1+"-->"+p2);
		double p1_x = p1.getX();
		double p1_y = p1.getY();
		double p2_x = p2.getX();
		double p2_y = p2.getY();
		if(p1_x<p2_x)
			move=IntelligentMove.DOWN;
		else if(p1_x>p2_x)
			move=IntelligentMove.UP;
		else if(p1_y<p2_y)
			move=IntelligentMove.RIGHT;
		else if(p1_y>p2_y)
			move=IntelligentMove.LEFT;
		//System.err.println("Move: " + move);
		//System.err.println("----");
		return move;
	}
	
	
	public ArrayList<Point> getListPoints() {
		ArrayList<Point> list = new ArrayList<Point>();
		ArrayList<Point> dirtyList = getDirtyCell();
		DijkstraShortestPath<Point, DefaultEdge> dsp;
		DijkstraShortestPath<Point, DefaultEdge> newDsp;
		DijkstraShortestPath<Point, DefaultEdge> dspReturnToBase;
		ArrayList<DefaultEdge> de_list;
		Point currentPoint = vep.getAgentLocation();
		list.add(currentPoint);
		int index;
		double energy = vep.getInitialEnergy();
		while(dirtyList.size()!=0) {	
			index=0;
			dsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, currentPoint, dirtyList.get(index));
			for(int i=0; i<dirtyList.size(); i++) {
				newDsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, currentPoint, dirtyList.get(i));
				if(newDsp.getPathLength()<dsp.getPathLength()) {
					dsp=newDsp;
					index=i;
				}
			}
			dspReturnToBase = new DijkstraShortestPath<Point, DefaultEdge>(graph, currentPoint, vep.getBaseLocation());
			if((dsp.getPathLength()+dspReturnToBase.getPathLength())>energy) {
				dirtyList = new ArrayList<Point>();
			}
			else {
				dirtyList.remove(index);
				de_list = (ArrayList<DefaultEdge>) dsp.getPath().getEdgeList();			
				for(DefaultEdge de : de_list) {
					Point pTarget = graph.getEdgeTarget(de);
					if(pTarget.equals(currentPoint))
						pTarget = graph.getEdgeSource(de);
					list.add(pTarget);
					currentPoint = pTarget;
					energy-=dsp.getPathLength();
					energy-=costSuck;
				}
			}
		}
		returnToBase(list, currentPoint);
		//System.err.println(list);
		return list;
	}
	
	
	private void returnToBase(ArrayList<Point> list, Point currentPoint) {
		DijkstraShortestPath<Point, DefaultEdge> dsp;
		ArrayList<DefaultEdge> de_list;
		dsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, currentPoint, vep.getBaseLocation());
		de_list = (ArrayList<DefaultEdge>) dsp.getPath().getEdgeList();			
		for(DefaultEdge de : de_list) {
			Point pTarget = graph.getEdgeTarget(de);
			if(pTarget.equals(currentPoint))
				pTarget = graph.getEdgeSource(de);
			list.add(pTarget);
			currentPoint = pTarget;
		}
	}
	
	
}
