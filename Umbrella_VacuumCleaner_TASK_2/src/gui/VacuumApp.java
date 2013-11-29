package gui;

import aima.gui.framework.AgentAppController;
import aima.gui.framework.AgentAppEnvironmentView;
import aima.gui.framework.AgentAppFrame;
import aima.gui.framework.SimpleAgentApp;

/**
 * Simple graphical application for experiments with vacuum cleaner agents. It
 * can be used as a template for creating other graphical agent applications.
 * 
 * @author Ruediger Lunde
 */
public class VacuumApp extends SimpleAgentApp {

	/**
	 * Starts the application.
	 */
	public static void main(final String args[]) {
		new VacuumApp().startApplication();
	}

	/** Returns a <code>VacuumController</code> instance. */
	@Override
	public AgentAppController createController() {
		return new VacuumController();
	}

	/** Returns a <code>VacuumView</code> instance. */
	@Override
	public AgentAppEnvironmentView createEnvironmentView() {
		return new VacuumView();
	}

	// ///////////////////////////////////////////////////////////////
	// main method

	/** Returns a <code>VacuumFrame</code> instance. */
	@Override
	public AgentAppFrame createFrame() {
		return new VacuumFrame();
	}
}
