package core;

import instanceXMLParser.CellLogicalState;

import java.util.Map;

import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.agent.impl.DynamicPercept;

/**
 * Represents a local percept in the vacuum environment (i.e. details the
 * agent's location and the state of the square the agent is currently at).
 * 
 * @author Ravi Mohan
 * @author Ciaran O'Reilly
 * @author Mike Stampone
 * @author Andrew Brown
 */
public class LocalVacuumEnvironmentPerceptTaskEnvironmentB extends
		DynamicPercept {

	public static final String ATTRIBUTE_STATE = "state";
	public static final String ATTRIBUTE_INITIAL_ENERGY = "initialEnergy";
	public static final String ATTRIBUTE_CURRENT_ENERGY = "currentEnergy";
	public static final String ATTRIBUTE_ACTION_ENERGY_COST = "actionEnergyCost";
	public static final String ATTRIBUTE_N = "N";
	public static final String ATTRIBUTE_M = "M";
	public static final String ATTRIBUTE_MOVED_LAST_TIME = "movedLastTime";
	public static final String ATTRIBUTE_ON_BASE = "onBase";

	/**
	 * Construct a vacuum environment percept from the agent's perception of the
	 * current location and state.
	 * 
	 */

	public LocalVacuumEnvironmentPerceptTaskEnvironmentB(
			final CellLogicalState cellLogicalState,
			final double initialEnergy, final Double currentEnergy,
			final Map<Action, Double> actionEnergyCosts, final int N,
			final int M, final boolean movedLastTime, final boolean onBase) {

		this.setAttribute(
				LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_STATE,
				cellLogicalState);
		this.setAttribute(
				LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_INITIAL_ENERGY,
				initialEnergy);
		this.setAttribute(
				LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_CURRENT_ENERGY,
				currentEnergy);
		this.setAttribute(
				LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_ACTION_ENERGY_COST,
				actionEnergyCosts);
		this.setAttribute(
				LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_N, N);
		this.setAttribute(
				LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_M, M);
		this.setAttribute(
				LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_MOVED_LAST_TIME,
				movedLastTime);
		this.setAttribute(
				LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_ON_BASE,
				onBase);

	}

	public Map<Action, Double> getActionEnergyCosts() {
		return (Map<Action, Double>) this
				.getAttribute(LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_ACTION_ENERGY_COST);
	}

	public Double getCurrentEnergy() {
		return (Double) this
				.getAttribute(LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_CURRENT_ENERGY);
	}

	public Double getInitialEnergy() {
		return (Double) this
				.getAttribute(LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_INITIAL_ENERGY);
	}

	public Integer getM() {
		return (Integer) this
				.getAttribute(LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_N);
	}

	public Integer getN() {
		return (Integer) this
				.getAttribute(LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_N);
	}

	public CellLogicalState getState() {
		return (CellLogicalState) this
				.getAttribute(LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_STATE);
	}

	public boolean isMovedLastTime() {
		return (boolean) this
				.getAttribute(LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_MOVED_LAST_TIME);
	}

	public boolean isOnBase() {
		return (boolean) this
				.getAttribute(LocalVacuumEnvironmentPerceptTaskEnvironmentB.ATTRIBUTE_ON_BASE);
	}

	/**
	 * Determine whether this percept matches an environment state
	 * 
	 * @param state
	 * @param agent
	 * @return true of the percept matches an environment state, false
	 *         otherwise.
	 */
	public boolean matches(final VacuumEnvironmentState state, final Agent agent) {
		// FIXME
		// if (!this.getAgentLocation().equals(state.getAgentLocation(agent)))
		// return false;
		// if (!this.getState().equals(
		// state.getLocationState(this.getAgentLocation())))
		// return false;
		return true;
	}

	/**
	 * Return string representation of this percept.
	 * 
	 * @return a string representation of this percept.
	 */
	@Override
	public String toString() {
		// FIXME
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		// sb.append(this.getAgentLocation());
		sb.append(", ");
		sb.append(this.getState());
		sb.append("]");
		return sb.toString();
	}
}
