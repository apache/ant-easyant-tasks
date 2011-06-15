package org.apache.ant.debugger;

import org.apache.tools.ant.Project;

/**
 * An interface for supporting debug commands. All debug commands MUST implement this interface.
 */
public interface DebugSupport {

	/**
	 * Main execution body of the class. Pass all command parameters.
	 * 
	 * @param project
	 * @param params
	 */
	public void execute(Project project, String[] params);

	/**
	 * Prints usage of the command.
	 * 
	 * @param project
	 */
	public void printUsage(Project project);

}
