package agent;

import core.VacuumEnvironment.LocationState;

public class MyCell {
	
	private LocationState state;
	private boolean visited;
	
	public MyCell() {
		state = LocationState.Clean;
		visited = false;
	}
	
	public MyCell(LocationState state, boolean visited) {
		this.state=state;
		this.visited=visited;
	}

	public LocationState getState() {
		return state;
	}

	public void setState(LocationState state) {
		this.state = state;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	@Override
	public String toString() {
		String s;
		if(visited)
			s = state + "-" + "V";
		else
			s = state + "-" + "N";
		return s;
	}

}
