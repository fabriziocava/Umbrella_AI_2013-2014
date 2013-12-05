package agent;

import java.awt.Point;
import java.util.Random;

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
	
	private MyCell[][] world;
	private Point agent;
	
	private LocalVacuumEnvironmentPerceptTaskEnvironmentB vep;
	
	public IntelligentMove_TASK_2(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		this.vep=vep;
		THRESHOLD = vep.getInitialEnergy()/2; /*DA CAMBIARE IN BASE ALLA SIZE DELLA MAPPA*/
		isOverThreshold = false;
		
		int N = vep.getN();
		if(N%2!=0) N++;
		int M = vep.getM();
		if(M%2!=0) N++;
		world = new MyCell [N*2][M*2];
		agent = new Point(N, M);	
	}
	
	public int getMovement() {
		new Random();
		int randomInt = new Random().nextInt(5);
		while(randomInt==SUCK)
			randomInt = new Random().nextInt(5);
		int movement = randomInt;
		System.out.println(vep.getCurrentEnergy());
		if(vep.getCurrentEnergy()<THRESHOLD)
			isOverThreshold = true;
		if(!isOverThreshold) {
			if(vep.getState().getLocState()==LocationState.Dirty) {
				movement=SUCK;
			}
		}
		return movement;
	}

	public void setVep(LocalVacuumEnvironmentPerceptTaskEnvironmentB vep) {
		this.vep = vep;
	}

	public void insertToMyCell(int move) {
		/*inserire cella in world*/
	}
	
}
