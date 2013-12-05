package agent;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.AbstractAgent;
import core.LocalVacuumEnvironmentPercept;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;

public class VacuumAgent1 extends AbstractAgent {

	public VacuumAgent1() {
		super(new AgentProgram() {
			@Override
			public Action execute(final Percept percept) {
				final LocalVacuumEnvironmentPerceptTaskEnvironmentB vep = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;
				final Set<Action> actionsKeySet = vep.getActionEnergyCosts().keySet();


				new Random();
				final int randomInt = new Random().nextInt(actionsKeySet.size());
				final Iterator<Action> iterator = actionsKeySet.iterator();
				for (int i = 0; i < randomInt; i++)
					iterator.next();
				return iterator.next();
			}
		});
	}

}
