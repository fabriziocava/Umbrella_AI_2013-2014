package agent;

import java.util.Iterator;
import java.util.Set;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.AbstractAgent;
import aima.core.agent.impl.NoOpAction;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;

public class VacuumAgent1 extends AbstractAgent {
	
	private IntelligentMove_TASK_2 intelligentMoveTask2;
	private boolean firstStep;
	
	public VacuumAgent1() {
		firstStep = true;
		this.program = new AgentProgram() {
			@Override
			public Action execute(final Percept percept) {
				final LocalVacuumEnvironmentPerceptTaskEnvironmentB vep = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;
				final Set<Action> actionsKeySet = vep.getActionEnergyCosts().keySet();
				if(firstStep) {
					intelligentMoveTask2 = new IntelligentMove_TASK_2(vep);
					firstStep = false;
				}
				intelligentMoveTask2.setVep(vep);			
				int move = intelligentMoveTask2.getMovement();
				if(move!=IntelligentMove_TASK_2.NoOP) {
					final Iterator<Action> iterator = actionsKeySet.iterator();
					for (int i = 0; i < move; i++)
						iterator.next();
					return iterator.next();
				}
				else {
					intelligentMoveTask2.print();
				}
				return NoOpAction.NO_OP;
			}
		};
	}

}
