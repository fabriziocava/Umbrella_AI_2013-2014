package gui;

import instanceXMLParser.Instance;
import agent.VacuumAgent1;
import aima.core.agent.impl.AbstractAgent;
import aima.gui.framework.AgentAppController;
import aima.gui.framework.AgentAppFrame;
import aima.gui.framework.MessageLogger;
import aima.gui.framework.SimulationThread;
import core.VacuumEnvironment;

/**
 * Defines how to react on user button events.
 * 
 * @author Ruediger Lunde
 */
public class VacuumController extends AgentAppController {

	protected VacuumEnvironment env = null;
	protected AbstractAgent agent = null;
	protected boolean isPrepared = false;

	/** Prepares next simulation if that makes sense. */
	@Override
	public void clear() {
		if (!this.isPrepared())
			this.prepare(null);
	}

	/** Checks whether simulation can be started. */
	@Override
	public boolean isPrepared() {
		return this.isPrepared && !this.env.isDone();
	}

	/**
	 * Creates a vacuum environment and a corresponding agent based on the state
	 * of the selectors and finally passes the environment to the viewer.
	 */
	@Override
	public void prepare(final String changedSelector) {
		final AgentAppFrame.SelectionState selState = this.frame.getSelection();
		final Instance i = new Instance();
		i.buildINstanceJDom("instance_EXAMPLE.xml");
		this.env = null;
		this.agent = null;
		switch (selState.getValue(VacuumFrame.AGENT_SEL)) {
		default:
			this.agent = new VacuumAgent1();
			break;
		}
		switch (selState.getValue(VacuumFrame.ENV_SEL)) {
		default:
			this.env = new VacuumEnvironment(i, this.agent);
			break;
		}
		if (this.env != null && this.agent != null) {
			this.frame.getEnvView().setEnvironment(this.env);
			this.isPrepared = true;
		}
	}

	/** Starts simulation. */
	@Override
	public void run(final MessageLogger logger) {
		logger.log("<simulation-log>");
		try {
			while (!this.env.isDone() && !this.frame.simulationPaused()) {
				Thread.sleep(500);
				this.env.step();
			}
		} catch (final InterruptedException e) {
		}
		logger.log("Performance: " + this.env.getPerformanceMeasure(this.agent));
		logger.log("</simulation-log>\n");
	}

	/** Executes one simulation step. */
	@Override
	public void step(final MessageLogger logger) {
		this.env.step();
	}

	/** Updates the status of the frame after simulation has finished. */
	@Override
	public void update(final SimulationThread simulationThread) {
		if (simulationThread.isCanceled()) {
			this.frame.setStatus("Task canceled.");
			this.isPrepared = false;
		} else if (this.frame.simulationPaused())
			this.frame.setStatus("Task paused.");
		else
			this.frame.setStatus("Task completed.");
	}

}
