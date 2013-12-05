package agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.AbstractAgent;
import aima.core.agent.impl.NoOpAction;
import aima.core.environment.map.Map;
import aima.core.util.datastructure.Pair;
import core.LocalVacuumEnvironmentPercept;
import core.VacuumEnvironment.LocationState;

public class VacuumAgent1 extends AbstractAgent {
	
	IntelligentMove intelligentMove;
	ArrayList<Integer> listMovements;
	private boolean firstStep;
	
	public VacuumAgent1() {
		firstStep = true;
		this.program = new AgentProgram() {
			@Override
			public Action execute(final Percept percept) {
				final LocalVacuumEnvironmentPercept vep = (LocalVacuumEnvironmentPercept) percept;
				final Set<Action> actionsKeySet = vep.getActionEnergyCosts().keySet();
				if(firstStep) {
					intelligentMove = new IntelligentMove(vep);
					listMovements = intelligentMove.getListMovements();
					firstStep=false;
				}
				int moveInt=0;
				while(listMovements.size()!=0 && vep.getCurrentEnergy()>0) {
					moveInt=listMovements.get(0);
					final Iterator<Action> iterator = actionsKeySet.iterator();
					for (int i = 0; i < moveInt; i++) {
						iterator.next();
					}
					listMovements.remove(0);
					return iterator.next();
				}
				System.out.println("ENERGY: " + vep.getCurrentEnergy());
				return NoOpAction.NO_OP;
			}
		};
	}	

}
