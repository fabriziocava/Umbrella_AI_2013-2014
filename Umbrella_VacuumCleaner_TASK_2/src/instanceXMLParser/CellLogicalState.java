package instanceXMLParser;

import core.VacuumEnvironment.LocationState;

public class CellLogicalState {

	LocationState locState;
	int dirtyAmount;
	
	public CellLogicalState(int dirtyAmount, LocationState locState) {
		this.locState=locState;
		this.dirtyAmount=dirtyAmount;
	}
	
	public CellLogicalState() {
		dirtyAmount=0;
		locState=LocationState.Clean;
	}

	public LocationState getLocState() {
		return locState;
	}

	public void setLocState(LocationState locState) {
		this.locState = locState;
	}

	public int getDirtyAmount() {
		return dirtyAmount;
	}

	public void setDirtyAmount(int dirtyAmount) {
		this.dirtyAmount = dirtyAmount;
	}
	
	
	
}
