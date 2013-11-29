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
	private boolean isReturnToBase;
	private ArrayList<Point> listPoints;
	private ArrayList<Integer> listMovements;
	private ArrayList<Point> listDirtyCell;
	
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
		isReturnToBase = false;
		listPoints = new ArrayList<Point>();
		listMovements = new ArrayList<Integer>();
		listDirtyCell = getDirtyCell();
		calculatePath();
	}
	
	private void calculatePath() {
		addListPoints(vep.getAgentLocation());
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
		return list;
	}
		
	private void addListMovements(ArrayList<Point> listPoints) {
		for(int i=0; i<listPoints.size()-1; i++) {
			if(!isReturnToBase) {
				if(vep.getState().get(listPoints.get(i)).equals(LocationState.Dirty))
					listMovements.add(IntelligentMove.SUCK);
			}
			listMovements.add(neighborhood(listPoints.get(i), listPoints.get(i+1)));
		}
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
		return move;
	}
	
	private void addListPoints(Point currentPoint) {
		DijkstraShortestPath<Point, DefaultEdge> dsp;
		DijkstraShortestPath<Point, DefaultEdge> newDsp;
		DijkstraShortestPath<Point, DefaultEdge> dspReturnToBase;
		ArrayList<DefaultEdge> de_list;
		listPoints.add(currentPoint);
		int index;
		double energy = vep.getInitialEnergy();
		while(listDirtyCell.size()!=0) {
			index=0;
			dsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, currentPoint, listDirtyCell.get(index));
			for(int i=0; i<listDirtyCell.size(); i++) {
				newDsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, currentPoint, listDirtyCell.get(i));
				if(newDsp.getPathLength()<dsp.getPathLength()) {
					dsp=newDsp;
					index=i;
				}
			}
			dspReturnToBase = new DijkstraShortestPath<Point, DefaultEdge>(graph, currentPoint, vep.getBaseLocation());
			if((dsp.getPathLength()+dspReturnToBase.getPathLength()+costSuck)>=energy) {
				listDirtyCell = new ArrayList<Point>();
			}
			else {
				listDirtyCell.remove(index);
				de_list = (ArrayList<DefaultEdge>) dsp.getPath().getEdgeList();			
				for(DefaultEdge de : de_list) {
					Point pTarget = graph.getEdgeTarget(de);
					if(pTarget.equals(currentPoint))
						pTarget = graph.getEdgeSource(de);
					listPoints.add(pTarget);
					currentPoint = pTarget;
				}
				energy-=dsp.getPathLength();
				energy-=costSuck;
			}
		}
		addListMovements(listPoints);
		listPoints = new ArrayList<Point>();
		returnToBase(currentPoint);
		addListMovements(listPoints);
	}
	
	private void returnToBase(Point currentPoint) {
		isReturnToBase = true;
		listPoints.add(currentPoint);
		DijkstraShortestPath<Point, DefaultEdge> dsp;
		ArrayList<DefaultEdge> de_list;
		dsp = new DijkstraShortestPath<Point, DefaultEdge>(graph, currentPoint, vep.getBaseLocation());
		de_list = (ArrayList<DefaultEdge>) dsp.getPath().getEdgeList();			
		for(DefaultEdge de : de_list) {
			Point pTarget = graph.getEdgeTarget(de);
			if(pTarget.equals(currentPoint))
				pTarget = graph.getEdgeSource(de);
			listPoints.add(pTarget);
			currentPoint = pTarget;
		}
	}

	public ArrayList<Integer> getListMovements() {
		return listMovements;
	}
	
}