package core;

import java.awt.Point;
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
public class LocalVacuumEnvironmentPercept extends DynamicPercept {

	public static final String ATTRIBUTE_STATE = "state";
	public static final String ATTRIBUTE_AGENT_LOCATION = "agentLocation";
	public static final String ATTRIBUTE_BASE_LOCATION = "baseLocation";
	public static final String ATTRIBUTE_INITIAL_ENERGY = "initialEnergy";
	public static final String ATTRIBUTE_CURRENT_ENERGY = "currentEnergy";
	public static final String ATTRIBUTE_ACTION_ENERGY_COST = "actionEnergyCost";
	public static final String ATTRIBUTE_N = "N";

	/**
	 * Construct a vacuum environment percept from the agent's perception of the
	 * current location and state.
	 * 
	 */

	public LocalVacuumEnvironmentPercept(
			final Map<Point, VacuumEnvironment.LocationState> state,
			final Point agentLocations, final Point baseLocation,
			final double initialEnergy, final Double currentEnergy,
			final Map<Action, Double> actionEnergyCosts, final int N) {

		this.setAttribute(LocalVacuumEnvironmentPercept.ATTRIBUTE_STATE, state);
		this.setAttribute(
				LocalVacuumEnvironmentPercept.ATTRIBUTE_AGENT_LOCATION,
				agentLocations);
		this.setAttribute(
				LocalVacuumEnvironmentPercept.ATTRIBUTE_BASE_LOCATION,
				baseLocation);
		this.setAttribute(
				LocalVacuumEnvironmentPercept.ATTRIBUTE_INITIAL_ENERGY,
				initialEnergy);
		this.setAttribute(
				LocalVacuumEnvironmentPercept.ATTRIBUTE_CURRENT_ENERGY,
				currentEnergy);
		this.setAttribute(
				LocalVacuumEnvironmentPercept.ATTRIBUTE_ACTION_ENERGY_COST,
				actionEnergyCosts);
		this.setAttribute(LocalVacuumEnvironmentPercept.ATTRIBUTE_N, N);

	}

	public Map<Action, Double> getActionEnergyCosts() {
		return (Map<Action, Double>) this
				.getAttribute(LocalVacuumEnvironmentPercept.ATTRIBUTE_ACTION_ENERGY_COST);
	}

	public Point getAgentLocation() {
		return (Point) this
				.getAttribute(LocalVacuumEnvironmentPercept.ATTRIBUTE_AGENT_LOCATION);
	}

	public Point getBaseLocation() {
		return (Point) this
				.getAttribute(LocalVacuumEnvironmentPercept.ATTRIBUTE_BASE_LOCATION);
	}

	public Double getCurrentEnergy() {
		return (Double) this
				.getAttribute(LocalVacuumEnvironmentPercept.ATTRIBUTE_CURRENT_ENERGY);
	}

	public Double getInitialEnergy() {
		return (Double) this
				.getAttribute(LocalVacuumEnvironmentPercept.ATTRIBUTE_INITIAL_ENERGY);
	}

	public Integer getN() {
		return (Integer) this
				.getAttribute(LocalVacuumEnvironmentPercept.ATTRIBUTE_N);
	}

	public Map<Point, VacuumEnvironment.LocationState> getState() {
		return (Map<Point, VacuumEnvironment.LocationState>) this
				.getAttribute(LocalVacuumEnvironmentPercept.ATTRIBUTE_STATE);
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
		if (!this.getAgentLocation().equals(state.getAgentLocation(agent)))
			return false;
		if (!this.getState().equals(
				state.getLocationState(this.getAgentLocation())))
			return false;
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
		sb.append(this.getAgentLocation());
		sb.append(", ");
		sb.append(this.getState());
		sb.append("]");
		return sb.toString();
	}
}
