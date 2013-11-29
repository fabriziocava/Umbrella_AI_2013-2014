package gui;

import aima.gui.framework.AgentAppFrame;

/**
 * Adds some selectors to the base class and adjusts its size.
 * 
 * @author Ruediger Lunde
 */
public class VacuumFrame extends AgentAppFrame {
	private static final long serialVersionUID = 1L;
	public static String ENV_SEL = "EnvSelection";
	public static String AGENT_SEL = "AgentSelection";

	public VacuumFrame() {
		this.setTitle("Vacuum Agent Application");
		this.setSelectors(new String[] { VacuumFrame.ENV_SEL,
				VacuumFrame.AGENT_SEL }, new String[] { "Select Environment",
				"Select Agent" });
		this.setSelectorItems(VacuumFrame.ENV_SEL,
				new String[] { "A/B Deterministic Environment" }, 0);
		this.setSelectorItems(VacuumFrame.AGENT_SEL, new String[] { "Agent1" },
				0);
		this.setEnvView(new VacuumView());
		this.setSize(800, 400);
	}
}