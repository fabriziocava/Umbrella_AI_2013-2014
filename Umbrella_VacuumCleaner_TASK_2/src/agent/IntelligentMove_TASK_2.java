package agent;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import aima.core.agent.Action;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;

public class IntelligentMove_TASK_2 {
	
	final static int SUCK = 0;
	final static int LEFT = 1;
	final static int DOWN = 2;
	final static int RIGHT = 3;
	final static int UP = 4;
	
	private double THRESHOLD;
	private boolean isOverThreshold;
	
	private LocalVacuumEnvironmentPerceptTaskEnvironmentB vep;
	private Set<Action> actionsKeySet;
	
	public IntelligentMove_TASK_2(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		this.vep=vep;
		THRESHOLD = vep.getInitialEnergy()/2; /*DA CAMBIARE IN BASE ALLA SIZE DELLA MAPPA*/
		isOverThreshold = false;
	}
	
	public int getMovement() {
		new Random();
		int randomInt = new Random().nextInt(5);
		while(randomInt==SUCK)
			randomInt = new Random().nextInt(5);
		int movement = randomInt;
		if(vep.getCurrentEnergy()<THRESHOLD)
			isOverThreshold = true;
		if(!isOverThreshold) {
			System.out.println(vep.getState().getLocState());
			if(vep.getState().getLocState()==LocationState.Dirty)
				movement=SUCK;	
		}
		return movement;
	}

}
