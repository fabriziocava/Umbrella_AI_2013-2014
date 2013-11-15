package agent;

import java.awt.Point;
import java.util.ArrayList;

public class Dijkstra {
	
	boolean[] visited_nodes;
	int[] weight_path;
	ArrayList<Integer> path_nodes;
	ArrayList<Point> point_path_nodes;
	int[] values;
	
	int[][] matrix;
	int size;
	Point start;
	Point end;
	
	public Dijkstra(int[][] matrix, int size) {
		this.matrix=matrix;
		this.size=size;
		visited_nodes = new boolean[size];
		weight_path = new int[size];
		path_nodes = new ArrayList<Integer>();
		point_path_nodes = new ArrayList<Point>();
		values = new int[size];
		
		for(int i=0; i<size; i++) {
			visited_nodes[i]=false;
			weight_path[i]=Integer.MAX_VALUE;
			values[i]=-1;
		}
	}
	
	/*
	public Dijkstra(int[][] matrix, int size, Point start, Point end) {
		this.matrix=matrix;
		this.size=size;
		this.start=start;
		this.end=end;
		visited_nodes = new boolean[size];
		weight_path = new int[size];
		path_nodes = new ArrayList<Integer>();
		point_path_nodes = new ArrayList<Point>();
		values = new int[size];
		for(int i=0; i<size; i++) {
			visited_nodes[i]=false;
			weight_path[i]=Integer.MAX_VALUE;
			values[i]=-1;
		}
		
	}
	*/
	
	public ArrayList<Integer> minimal_path(int start, int end) {
		weight_path[start]=0;
		int i=0;
		int visit;
		int new_weight_path;
		while(i<size && !visited_nodes[end]) {
			i++;
			visit=index_minWeightPath();
			visited_nodes[visit]=true;
			for(int j=0; j<size; j++) {
				if(matrix[visit][j]!=0) {
					new_weight_path = weight_path[visit]+matrix[visit][j];
					if(new_weight_path<weight_path[j] && !visited_nodes[j]) {
						weight_path[j]=new_weight_path;
						values[j] = visit;
					}
				}
			}
		}
		calculatePath(end);
		return path_nodes;
	}
	
	public ArrayList<Point> point_minimal_path(Point start, Point end) {
		
		return point_path_nodes;
	}
	
	private void calculatePath(int end) {
		int i=end;
		path_nodes.add(i);
		while(values[i]!=-1) {
			path_nodes.add(values[i]);
			i = values[i];
		}
	}
	
	
	/*
	 * index_minWightPath()
	 * ritorna l'indice del nodo non visitato con weight_path minimo
	 * (-1 tutti gli indici sono stati visitati)
	 */
	private int index_minWeightPath() {
		int min = Integer.MAX_VALUE;
		int index = -1;
		for(int i=0; i<size; i++)
			if(!visited_nodes[i])
				if(weight_path[i]<min) {
					min=weight_path[i];
					index=i;
				}
		return index;
	}
	
	
	public static void main(String[] args) {
		int[][] matrix= {
							{0,2,0,0,8,0,0},
							{2,0,6,2,0,0,0},
							{0,6,0,0,0,0,5},
							{0,2,0,0,2,9,0},
							{8,0,0,2,0,3,0},
							{0,0,0,9,3,0,1},
							{0,0,5,0,0,1,0}
						};
		int size=7;
		/*
		for(int i=0; i<size; i++) {
			for(int j=0; j<size; j++)
				System.out.print(matrix[i][j] + " ");
			System.out.println();
		}
		*/
		ArrayList<Integer> list = new ArrayList<Integer>();
		Dijkstra d = new Dijkstra(matrix, size);
		list = d.minimal_path(0, size-1);
		System.out.print("PATH: ");
		for(Integer i : list) {
			System.out.print(i + " ");
		}
		System.out.println();
	}

}
