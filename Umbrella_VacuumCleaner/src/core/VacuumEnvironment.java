package core;

import instanceXMLParser.Instance;

import java.awt.Point;

import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.agent.EnvironmentState;
import aima.core.agent.Percept;
import aima.core.agent.impl.AbstractEnvironment;

/**
 * Artificial Intelligence A Modern Approach (3rd Edition): pg 58.<br>
 * <br>
 * Let the world contain just two locations. Each location may or may not
 * contain dirt, and the agent may be in one location or the other. There are 8
 * possible world states, as shown in Figure 3.2. The agent has three possible
 * actions in this version of the vacuum world: <em>Left</em>, <em>Right</em>,
 * and <em>Suck</em>. Assume for the moment, that sucking is 100% effective. The
 * goal is to clean up all the dirt.
 * 
 * @author Ravi Mohan
 * @author Ciaran O'Reilly
 * @author Mike Stampone
 */
public class VacuumEnvironment extends AbstractEnvironment {
	// Allowable Actions within the Vacuum Environment
	// public static final Action ACTION_MOVE_LEFT;
	// public static final Action ACTION_MOVE_RIGHT;
	// public static final Action ACTION_MOVE_UP = new DynamicAction("Up");
	// public static final Action ACTION_MOVE_DOWN = new DynamicAction("Down");
	// public static final Action ACTION_SUCK = new DynamicAction("Suck");

	public enum LocationState {
		Clean, Dirty, Obstacle
	};

	protected VacuumEnvironmentState envState = null;
	protected boolean isDone = false;

	/**
	 * Constructs a vacuum environment with two locations, in which dirt is
	 * placed at random.
	 */
	public VacuumEnvironment(final Instance instanceBean, final Agent agent) {

		this.envState = new VacuumEnvironmentState(instanceBean, agent);

		this.addAgent(agent);

		// ACTION_MOVE_LEFT = env.getActionFromName("left");
		// ACTION_MOVE_RIGHT = env.getActionFromName("left");
		// ACTION_MOVE_UP = env.getActionFromName("left");
		// ACTION_MOVE_DOWN = env.getActionFromName("left");
		// ACTION_SUCK = env.getActionFromName("left");

	}

	@Override
	public EnvironmentState executeAction(final Agent agent,
			final Action agentAction) {

		if (this.envState.getActionFromName("suck").equals(agentAction)) {
			if (LocationState.Dirty.equals(this.envState
					.getLocationState(this.envState.getAgentLocation(agent)))) {
				this.envState.setLocationState(
						this.envState.getAgentLocation(agent),
						LocationState.Clean);
				this.envState.updateCurrentEnergy(agent,
						this.envState.getEnergyCost(agentAction));
				this.updatePerformanceMeasure(agent);
			}
		} else if (agentAction.isNoOp())
			// In the Vacuum Environment we consider things done if
			// the agent generates a NoOp.
			this.isDone = true;
		else {
			this.envState.moveAgent(agent, agentAction);
			this.envState.updateCurrentEnergy(agent,
					this.envState.getEnergyCost(agentAction));
			this.updatePerformanceMeasure(agent);
		}

		return this.envState;

	}

	public Point getAgentLocation(final Agent agent) {
		return this.envState.getAgentLocation(agent);
	}

	@Override
	public EnvironmentState getCurrentState() {
		return this.envState;
	}

	public LocationState getLocationState(final Point location) {
		return this.envState.getLocationState(location);
	}

	@Override
	public Percept getPerceptSeenBy(final Agent anAgent) {
		// if (anAgent instanceof NondeterministicVacuumAgent) {
		// // Note: implements FullyObservableVacuumEnvironmentPercept
		// return new VacuumEnvironmentState(this.envState);
		// }

		return new LocalVacuumEnvironmentPercept(this.envState.getState(),
				this.envState.getAgentLocation(anAgent),
				this.envState.getBaseLocation(),
				this.envState.getInitialEnergy(),
				this.envState.getCurrentEnergy(anAgent),
				this.envState.getActionEnergyCosts(), this.envState.getN());

	}

	@Override
	public boolean isDone() {
		return super.isDone() || this.isDone;
	}

	protected void updatePerformanceMeasure(final Agent agent) {

		final double ET = this.envState.getCurrentEnergy(agent);
		final double BdT = this.envState.getDistanceFromBase(agent);
		final double E0 = this.envState.getInitialEnergy();
		final double CT = this.envState.getCleanedTiles(agent);
		final double D0 = this.envState.getDirtyInitialTiles();

		final Double performanceMeasure = Math.ceil((ET - BdT + 1) / (E0 + 1))
				* (CT + 1) / (D0 + 1) * (1 + (ET + 1) / (E0 + 1));

		this.performanceMeasures.put(agent, performanceMeasure);

	}
}