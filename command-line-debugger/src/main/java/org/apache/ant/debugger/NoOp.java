package org.apache.ant.debugger;

import org.apache.tools.ant.Project;

/**
 * Used to implement commands that should not be handled by {@link DebugSupport}
 * at all. Example, the 'return' command
 */
public final class NoOp implements DebugSupport {

	public boolean commandSupported(String command) {
		return "return".equalsIgnoreCase(command);
	}

	public void execute(Project project, String[] params) {
		// do nothing
	}

	public void printUsage(Project project) {
	};
}
