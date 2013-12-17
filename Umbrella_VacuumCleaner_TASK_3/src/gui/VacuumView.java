package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.agent.EnvironmentState;
import aima.gui.framework.EmptyEnvironmentView;
import core.VacuumEnvironment;
import core.VacuumEnvironment.LocationState;
import core.VacuumEnvironmentState;

/**
 * Displays the informations provided by a <code>VacuumEnvironment</code> on a
 * panel using 2D-graphics.
 * 
 * @author Ruediger Lunde
 */
public class VacuumView extends EmptyEnvironmentView {

	private static final long serialVersionUID = 1L;
	private final Hashtable<Agent, Action> lastActions = new Hashtable<Agent, Action>();
	private Image imgDirty;
	private Image imgObstacle;
	private Image imgBase;
	private Image imgAgent;

	public VacuumView() {
		this.setVisible(true);
		this.setBackground(Color.WHITE);
		this.uploadPictures();
	}

	@Override
	public void agentActed(final Agent agent, final Action action,
			final EnvironmentState resultingState) {
		this.lastActions.put(agent, action);
		String prefix = "";
		if (this.env.getAgents().size() > 1)
			prefix = "A" + this.env.getAgents().indexOf(agent) + ": ";
		this.notify(prefix + action.toString());
		super.agentActed(agent, action, resultingState);
	}

	public LocationState getLocationState(final int i, final int j) {
		return ((VacuumEnvironmentState) this.getVacuumEnv().getCurrentState())
				.getLocationState(new Point(i, j));
	}

	public int getM() {
		return ((VacuumEnvironmentState) this.getVacuumEnv().getCurrentState())
				.getM();
	}

	public int getN() {
		return ((VacuumEnvironmentState) this.getVacuumEnv().getCurrentState())
				.getN();
	}

	protected VacuumEnvironment getVacuumEnv() {
		return (VacuumEnvironment) this.env;
	}

	public boolean isAgentPosition(final int x, final int y) {

		for (final Agent agent : this.getVacuumEnv().getAgents())
			if (((VacuumEnvironmentState) this.getVacuumEnv().getCurrentState())
					.getAgentLocation(agent).equals(new Point(x, y)))
				return true;

		return false;

	}

	public boolean isBase(final int x, final int y) {
		return ((VacuumEnvironmentState) this.getVacuumEnv().getCurrentState())
				.getBaseLocation().equals(new Point(x, y));
	}

	public int optimalSizeCellSize() {
		return Math.min(this.getHeight() / this.getN(),
				this.getWidth() / this.getN());
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		for (int i = 0; i < this.getN(); i++)
			for (int j = 0; j < this.getM(); j++) {
				if (this.getLocationState(i, j) == LocationState.Dirty)
					g.drawImage(this.imgDirty, j * this.optimalSizeCellSize(),
							i * this.optimalSizeCellSize(),
							this.optimalSizeCellSize(),
							this.optimalSizeCellSize(), null);
				if (this.getLocationState(i, j) == LocationState.Obstacle)
					g.drawImage(this.imgObstacle,
							j * this.optimalSizeCellSize(),
							i * this.optimalSizeCellSize(),
							this.optimalSizeCellSize(),
							this.optimalSizeCellSize(), null);
				if (this.isBase(i, j))
					g.drawImage(this.imgBase, j * this.optimalSizeCellSize(), i
							* this.optimalSizeCellSize(),
							this.optimalSizeCellSize(),
							this.optimalSizeCellSize(), null);
				if (this.isAgentPosition(i, j))
					g.drawImage(
							this.imgAgent,
							j * this.optimalSizeCellSize()
									+ this.optimalSizeCellSize() / 6,
							i * this.optimalSizeCellSize()
									+ this.optimalSizeCellSize() / 6,
							this.optimalSizeCellSize() * 2 / 3,
							this.optimalSizeCellSize() * 2 / 3, null);
				g.drawRect(j * this.optimalSizeCellSize(),
						i * this.optimalSizeCellSize(),
						this.optimalSizeCellSize(), this.optimalSizeCellSize());
			}
	}

	/**
	 * Creates a 2D-graphics showing the agent in its environment. Locations are
	 * represented as rectangles, dirt as grey background color, and the agent
	 * as red Pacman.
	 */

	private void uploadPictures() {
		try {
			// this.imgDirty = ImageIO
			// .read(new File(".//img//polvere_625833.jpg"));
			// this.imgWall = ImageIO.read(new File(".//img//wall.jpg"));
			// this.imgBase = ImageIO.read(new File(".//img//base.jpg"));
			// this.imgAgent = ImageIO.read(new File(".//img//agent.png"));
			this.imgDirty = ImageIO.read(new File(".//img//dirty.png"));
			this.imgObstacle = ImageIO.read(new File(".//img//obstacle.png"));
			this.imgBase = ImageIO.read(new File(".//img//base.png"));
			this.imgAgent = ImageIO.read(new File(".//img//agent.png"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks whether an agent is currently at the specified location and
	 * returns one of them if any.
	 * */
	// protected Agent getAgent(Object location) {
	// VacuumEnvironment e = getVacuumEnv();
	// for (Agent a : e.getAgents())
	// if (location.equals(e.getAgentLocation(a)))
	// return a;
	// return null;
	// }
}
