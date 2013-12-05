package agent;

import java.util.Iterator;
import java.util.Set;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.AbstractAgent;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;

public class VacuumAgent1 extends AbstractAgent {
	
	private LocalVacuumEnvironmentPerceptTaskEnvironmentB vep;
	private Set<Action> actionsKeySet;
	private IntelligentMove_TASK_2 intelligentMoveTask2;
	private boolean firstStep;
	
	public VacuumAgent1() {
		firstStep = true;
		this.program = new AgentProgram() {
			@Override
			public Action execute(final Percept percept) {
				if(firstStep) {
					vep = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;
					actionsKeySet = vep.getActionEnergyCosts().keySet();
					intelligentMoveTask2 = new IntelligentMove_TASK_2(vep);
					firstStep = false;
				}
				int move = intelligentMoveTask2.getMovement();
				final Iterator<Action> iterator = actionsKeySet.iterator();
				for (int i = 0; i < move; i++)
					iterator.next();
				return iterator.next();
			}
		};
	}

}
