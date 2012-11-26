package org.apache.ant.debugger;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.util.StringUtils;

public class DebugPrompt {

	protected Project project = null;

	protected DebugCommandSet commandHandler;

	public DebugPrompt(Project project, DebugCommandSet commandHandler) {
		this.project = project;
		this.commandHandler = commandHandler;
	}

	public void prompt(String message) {
		InputRequest ir = new InputRequest("Debugger> ");
		InputHandler ih = new DefaultInputHandler();
		String command = null;
		project.log(StringUtils.LINE_SEP
				+ "-------- Ant Command Line Debugger --------"
				+ StringUtils.LINE_SEP + StringUtils.LINE_SEP);

		// print a friendly message to allow the user to understand why this
		// breakpoint occurred
		project.log(message);
		project.log("");
		// keep accepting inputs, until the user enters the return command
		do {
			ih.handleInput(ir);
			command = ir.getInput();
			commandHandler.handleCommand(command);
			project.log(""); // log a new line
		} while (!"return".equals(command));

		// resume build execution on this
		project.log(StringUtils.LINE_SEP
				+ "--------- Resuming Ant Execution ----------"
				+ StringUtils.LINE_SEP);
	}

}
