package gui;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

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

		final List<String> environments = new LinkedList<String>();

		final String path = "environment";
		final File folder = new File(path);
		final File[] listOfFiles = folder.listFiles();

		for (final File listOfFile : listOfFiles)
			if (listOfFile.isFile()) {
				final String file = listOfFile.getName();
				if (file.endsWith(".xml") || file.endsWith(".XML"))
					environments.add(file);
			}

		this.setSelectorItems(VacuumFrame.ENV_SEL,
				environments.toArray(new String[environments.size()]), 0);
		this.setSelectorItems(VacuumFrame.AGENT_SEL, new String[] { "Agent1" },
				0);
		this.setEnvView(new VacuumView());
		this.setSize(800, 400);
	}
}