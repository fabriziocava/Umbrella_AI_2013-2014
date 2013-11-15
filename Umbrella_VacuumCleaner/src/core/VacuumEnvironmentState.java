package core;

import instanceXMLParser.Instance;

import java.awt.Point;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.agent.EnvironmentState;
import aima.core.agent.impl.DynamicAction;
import core.VacuumEnvironment.LocationState;

/**
 * Represents a state in the Vacuum World
 * 
 * @author Ciaran O'Reilly
 * @author Andrew Brown
 */
public class VacuumEnvironmentState implements EnvironmentState,
		FullyObservableVacuumEnvironmentPercept {

	private final Map<Point, VacuumEnvironment.LocationState> state;
	private final Map<Agent, Point> agentLocations;
	private Point baseLocation;
	private double initialEnergy;
	private final Map<Agent, Double> currentEnergy;
	private final Map<Action, Double> actionEnergyCosts;
	private int N;

	/**
	 * Constructor
	 */
	public VacuumEnvironmentState(final Instance instanceBean, final Agent a) {

		this.state = new LinkedHashMap<Point, VacuumEnvironment.LocationState>();
		this.agentLocations = new LinkedHashMap<Agent, Point>();
		this.currentEnergy = new LinkedHashMap<Agent, Double>();
		this.actionEnergyCosts = new LinkedHashMap<Action, Double>();

		this.loadFromBean(instanceBean, a);

	}

	/**
	 * Copy Constructor.
	 * 
	 * @param toCopyState
	 *            Vacuum Environment State to copy.
	 */
	public VacuumEnvironmentState(final VacuumEnvironmentState toCopyState) {

		this.state = new LinkedHashMap<Point, VacuumEnvironment.LocationState>();
		this.agentLocations = new LinkedHashMap<Agent, Point>();
		this.currentEnergy = new LinkedHashMap<Agent, Double>();
		this.actionEnergyCosts = new LinkedHashMap<Action, Double>();

		this.state.putAll(toCopyState.state);
		this.agentLocations.putAll(toCopyState.agentLocations);
		this.baseLocation.setLocation(toCopyState.baseLocation);
		this.initialEnergy = toCopyState.initialEnergy;
		this.currentEnergy.putAll(toCopyState.currentEnergy);
		this.actionEnergyCosts.putAll(toCopyState.actionEnergyCosts);

	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof VacuumEnvironmentState) {
			final VacuumEnvironmentState s = (VacuumEnvironmentState) o;
			if (this.state.equals(s.state)
					&& this.agentLocations.equals(s.agentLocations))
				return true;
		}
		return false;
	}

	public Map<Action, Double> getActionEnergyCosts() {
		return Collections.unmodifiableMap(this.actionEnergyCosts);
	}

	public Action getActionFromName(final String actionName) {

		for (final Action a : this.actionEnergyCosts.keySet())
			if (((DynamicAction) a).getName().equals(actionName))
				return a;

		return null;

	}

	@Override
	public Point getAgentLocation(final Agent a) {
		return this.agentLocations.get(a);
	}

	public Point getBaseLocation() {
		return this.baseLocation;
	}

	public Double getCurrentEnergy(final Agent agent) {
		return this.currentEnergy.get(agent);
	}

	public double getEnergyCost(final Action action) {
		return this.actionEnergyCosts.get(action);
	}

	public double getInitialEnergy() {
		return this.initialEnergy;
	}

	@Override
	public VacuumEnvironment.LocationState getLocationState(final Point location) {
		return this.state.get(location);
	}

	public int getN() {
		return this.N;
	}

	public Map<Point, VacuumEnvironment.LocationState> getState() {

		return Collections.unmodifiableMap(this.state);

	}

	/**
	 * Override hashCode()
	 * 
	 * @return the hash code for this object.
	 */
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 13 * hash + (this.state != null ? this.state.hashCode() : 0);
		hash = 53
				* hash
				+ (this.agentLocations != null ? this.agentLocations.hashCode()
						: 0);
		return hash;
	}

	private void loadFromBean(final Instance instanceBean, final Agent a) {
		this.N = instanceBean.getSize();
		for (int i = 0; i < this.N; i++)
			for (int j = 0; j < this.N; j++)
				this.state.put(new Point(i, j),
						instanceBean.getBoardState()[i][j]);
		this.baseLocation = new Point(instanceBean.getBasePos().getX(),
				instanceBean.getBasePos().getY());
		this.initialEnergy = instanceBean.getEnergy();
		this.currentEnergy.put(a, this.initialEnergy);
		for (final String name : instanceBean.getActionCosts().keySet())
			this.actionEnergyCosts.put(new DynamicAction(name), instanceBean
					.getActionCosts().get(name));
		this.agentLocations.put(a, new Point(instanceBean.getAgentPos().getX(),
				instanceBean.getAgentPos().getY()));
	}

	public void moveAgent(final Agent agent, final Action action) {

		final Point current_location = this.agentLocations.get(agent);
		final Point new_location = (Point) current_location.clone();

		if (this.getActionFromName("left").equals(action)) {
			if (current_location.y > 0)
				new_location.y--;
		} else if (this.getActionFromName("right").equals(action)) {
			if (current_location.y < this.N - 1)
				new_location.y++;
		} else if (this.getActionFromName("up").equals(action)) {
			if (current_location.x > 0)
				new_location.x--;
		} else if (this.getActionFromName("down").equals(action))
			if (current_location.x < this.N - 1)
				new_location.x++;

		if (this.state.get(new_location) != LocationState.Obstacle)
			this.agentLocations.put(agent, new_location);

	}

	/**
	 * Sets the location state
	 * 
	 * @param location
	 * @param s
	 */
	public void setLocationState(final Point location,
			final VacuumEnvironment.LocationState s) {
		this.state.put(location, s);
	}

	/**
	 * Returns a string representation of the environment
	 * 
	 * @return a string representation of the environment
	 */
	@Override
	public String toString() {
		return this.state.toString();
	}

	public void updateCurrentEnergy(final Agent agent, final double cost) {
		this.currentEnergy.put(agent, this.currentEnergy.get(agent) - cost);
	}

}