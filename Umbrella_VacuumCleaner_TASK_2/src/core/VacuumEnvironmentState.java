package core;

import instanceXMLParser.CellLogicalState;
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

	private final Map<Point, CellLogicalState> state;
	private final Map<Agent, Point> agentLocations;
	private Point baseLocation;
	private double initialEnergy;
	private final Map<Agent, Double> currentEnergy;
	private final Map<Action, Double> actionEnergyCosts;
	private int N;
	private int M;
	private int dirtyInitialTiles;
	private int cleanedTiles;
	private boolean movedLastTime;

	/**
	 * Constructor
	 */
	public VacuumEnvironmentState(final Instance instanceBean, final Agent a) {

		this.state = new LinkedHashMap<Point, CellLogicalState>();
		this.agentLocations = new LinkedHashMap<Agent, Point>();
		this.currentEnergy = new LinkedHashMap<Agent, Double>();
		this.actionEnergyCosts = new LinkedHashMap<Action, Double>();

		this.loadFromBean(instanceBean, a);

		this.dirtyInitialTiles = 0;
		for (final Point point : this.state.keySet())
			if (this.state.get(point).getLocState() == LocationState.Dirty)
				this.dirtyInitialTiles++;

		this.cleanedTiles = 0;

		this.movedLastTime = false;

	}

	/**
	 * Copy Constructor.
	 * 
	 * @param toCopyState
	 *            Vacuum Environment State to copy.
	 */
	public VacuumEnvironmentState(final VacuumEnvironmentState toCopyState) {

		this.state = new LinkedHashMap<Point, CellLogicalState>();
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

	public CellLogicalState getCellLogicalState(final Point location) {
		return this.state.get(location);
	}

	public double getCleanedTiles(final Agent agent) {
		return this.cleanedTiles;
	}

	public Double getCurrentEnergy(final Agent agent) {
		return this.currentEnergy.get(agent);
	}

	public double getDirtyInitialTiles() {
		return this.dirtyInitialTiles;
	}

	public double getDistanceFromBase(final Agent agent) {
		// FIXME return minimum path
		return 0;
	}

	public double getEnergyCost(final Action action) {
		return this.actionEnergyCosts.get(action);
	}

	public double getInitialEnergy() {
		return this.initialEnergy;
	}

	@Override
	public VacuumEnvironment.LocationState getLocationState(final Point location) {
		return this.state.get(location).getLocState();
	}

	public int getM() {
		return this.M;
	}

	public double getMaxDistanceToTheBase() {
		// FIXME return maximum of the distances from each tile to the base
		return this.N * this.M;
	}

	public int getN() {
		return this.N;
	}

	public Map<Point, CellLogicalState> getState() {

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

	public boolean isMovedLastTime() {
		return this.movedLastTime;
	}

	private void loadFromBean(final Instance instanceBean, final Agent a) {
		this.N = instanceBean.getSize_n();
		this.M = instanceBean.getSize_m();
		for (int i = 0; i < this.N; i++)
			for (int j = 0; j < this.M; j++)
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
			if (current_location.y < this.M - 1)
				new_location.y++;
		} else if (this.getActionFromName("up").equals(action)) {
			if (current_location.x > 0)
				new_location.x--;
		} else if (this.getActionFromName("down").equals(action))
			if (current_location.x < this.N - 1)
				new_location.x++;

		if (!new_location.equals(current_location)
				&& this.state.get(new_location).getLocState() != LocationState.Obstacle) {
			this.agentLocations.put(agent, new_location);
			this.movedLastTime = true;
		} else
			this.movedLastTime = false;

	}

	/**
	 * Sets the location state
	 * 
	 * @param location
	 * @param s
	 */
	public void setLocationState(final Point location, final CellLogicalState s) {
		if (this.state.get(location).getLocState() == LocationState.Dirty
				&& s.getLocState() == LocationState.Clean)
			this.cleanedTiles++;
		this.state.put(location, s);
	}

	public void suckTile(final Point location) {

		final CellLogicalState cellLogicalState = this.state.get(location);

		if (cellLogicalState.getLocState() == LocationState.Dirty) {
			cellLogicalState
					.setDirtyAmount(cellLogicalState.getDirtyAmount() - 1);

			if (cellLogicalState.getDirtyAmount() <= 0) {
				this.cleanedTiles++;
				this.state.put(location, new CellLogicalState(0,
						LocationState.Clean));
			}

		}

		this.movedLastTime = false;

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