package org.apache.tools.ant;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.taskdefs.PathConvert;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.util.StringUtils;

/**
 * A stand alone debug task that plugs into the build through projecthelper
 * class by injecting a dependency at the end of a target. This is a POC and
 * does not yet work for Extension-Points.
 */
public class DebugTask extends Task {
	/**
	 * Debugger prompt
	 */
	public static final String PROMPT = "DEBUGGER> ";

	/**
	 * Standard target name for the internal debugger
	 */
	public static final String DEBUG_TARGET_NAME = "-internal-debugger";

	/*
	 * A static final debug target that is injected as a dependency. We may need
	 * to have more than one such target if we are to allow multiple
	 * break-points in the build.
	 */
	private static final Target debugTarget = new Target();

	/*
	 * Set of all commands that can be interpreted at runtime.
	 */
	private static Set supportedCommands = new HashSet();

	static {
		// add any more commands to be supported here
		// if need be, this can be considered to be
		// moved to a separate properties file
		supportedCommands.add("locate");
		supportedCommands.add("inspect");
		supportedCommands.add("return");
	}

	public void execute() throws BuildException {
		// expect input here
		InputRequest ir = new InputRequest(PROMPT);
		InputHandler ih = new DefaultInputHandler();
		String command = null;
		getProject().log(
				StringUtils.LINE_SEP
						+ "-------- Ant Command Line Debugger --------"
						+ StringUtils.LINE_SEP + StringUtils.LINE_SEP);

		// keep accepting inputs, until the user enters the return command
		do {
			ih.handleInput(ir);
			command = ir.getInput();
			handleCommand(command);
			getProject().log(""); // log a new line
		} while (!"return".equals(command));

		// resume build execution on this
		getProject().log(
				StringUtils.LINE_SEP
						+ "--------- Resuming Ant Execution ----------"
						+ StringUtils.LINE_SEP);
	}

	/**
	 * Static method to create a runtime-debug target. For purposes of Command
	 * Line Debugger (CLD), the target returned by this method, and identified
	 * by DEBUG_TARGET_NAME must be added at the end of the dependency list of
	 * the target where the break point exists.
	 * 
	 * @param project
	 * @return
	 */
	public static Target createDebugTarget(Project project) {
		// see what is the best value for the Location to assume?
		// Location loc = new Location(??);

		debugTarget.setProject(project);
		debugTarget.setName(DEBUG_TARGET_NAME);
		project.addTarget(debugTarget);
		// create an instance of debug task and attach it to this project
		Task debugtask = project.createTask("debug");
		debugtask.setProject(project);
		debugtask.setTaskName("Debugger");
		debugTarget.addTask(debugtask);
		return debugTarget;
	}

	/*
	 * Interprets user input and decides if the command is supported or should
	 * be rejected.
	 */
	protected void handleCommand(String command) {
		if (command == null || command.trim().length() == 0) {
			getProject().log("Invalid command. Use /? for more information.");
		}
		command = command.trim();
		if (command.equals("/?")) {
			printUsage();
			return;
		}

		String[] tokens = command.split(" ");
		if (!supportedCommands.contains(tokens[0])) {
			printUsage();
			return;
		}

		DebugSupport[] debuggers = new DebugSupport[] { new NoOp(),
				new Inspector(), new Locator() };
		DebugSupport selected = null;
		for (int j = 0; j < debuggers.length; j++) {
			if (debuggers[j].commandSupported(tokens[0]))
				selected = debuggers[j];
		}
		selected.execute(getProject(), tokens);
	}

	protected void printUsage() {
		// log all help stuff here
		getProject()
				.log(
						"You may use one of the following commands: locate, inspect, return");
		getProject()
				.log(
						"Type the command followed by /? for more information. Eg. inspect /?");
	}

	protected static List searchTask(Class expectedTaskClass, Project project) {
		List result = new ArrayList();
		for (Iterator iterator = project.getTargets().values().iterator(); iterator
				.hasNext();) {
			Target t = (Target) iterator.next();
			for (int i = 0; i < t.getTasks().length; i++) {
				Task task = t.getTasks()[i];
				Class taskClass = ComponentHelper.getComponentHelper(project)
						.getComponentClass(task.getTaskType());
				// will need to see in what cases it could return a null type
				// perhaps failing when the task is using a custom antlib
				// defined task
				if (taskClass != null && taskClass.equals(expectedTaskClass)) {
					result.add(task);
				}
			}
		}
		return result;
	}

	/**
	 * An interface for supporting debug commands.
	 */
	public static interface DebugSupport {

		/**
		 * Check if this command is supported.
		 * 
		 * @param command
		 * @return
		 */
		public boolean commandSupported(String command);

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

	/**
	 * Used to implement commands that should not be handled by
	 * {@link DebugSupport} at all. Example, the 'return' command
	 */
	public static final class NoOp implements DebugSupport {

		public boolean commandSupported(String command) {
			return "return".equalsIgnoreCase(command);
		}

		public void execute(Project project, String[] params) {
			// do nothing
		}

		public void printUsage(Project project) {
		};
	}

	/**
	 * Locates properties / paths in static build sources
	 */
	public static final class Locator implements DebugSupport {

		public boolean commandSupported(String command) {
			return "locate".equalsIgnoreCase(command);
		}

		public void execute(Project project, String[] params) {
			// the command syntax is 'locate property some.property'
			// or 'locate path some.path
			if (params.length != 3 || "/?".equals(params[1])) {
				printUsage(project);
				return;
			}

			List matches = null;
			String key = null;
			if ("property".equalsIgnoreCase(params[1])) {
				// locate and publish the property
				matches = DebugTask.searchTask(Property.class, project);
				key = "name";
			} else if ("path".equalsIgnoreCase(params[1])) {
				// locate and publish the path
				matches = DebugTask.searchTask(Path.class, project);
				key = "id";
			} else {
				// see if any other component may be supported
				project.log("Unexpected component: " + params[1]);
				project.log("Supported components are property, path.");
				return;
			}

			// probably accept some kind of a query from end user and select the
			// target object based on the query
			for (Iterator iterator = matches.iterator(); iterator.hasNext();) {
				Task task = (Task) iterator.next();
				// display attributes
				Map attributeMap = task.getWrapper().getAttributeMap();
				if (!params[2].equals(attributeMap.get(key))) {
					continue;
				}
				String value = (String) attributeMap.get("value");
				project.log("Detected a property by name [" + params[2]
						+ "]. Build file value: " + value);
				// and their respected location
				project.log("Located at: " + task.getLocation().toString());
			}
		}

		public void printUsage(Project project) {
			project.log("Incorrect Parameters");
			project.log("Usage: locate property/path propertyname/pathname");
		}
	}

	/**
	 * Inspects the current value of a property, path or some reference.
	 */
	public static final class Inspector implements DebugSupport {

		public boolean commandSupported(String command) {
			return "inspect".equalsIgnoreCase(command);
		}

		public void execute(Project project, String[] params) {
			if (params.length < 3 || "/?".equals(params[1])) {
				printUsage(project);
			}

			if ("property".equalsIgnoreCase(params[1])) {
				// show all matches for a property
				Object value = PropertyHelper.getProperty(project, params[2]);
				if (value != null) {
					project.log("Detected a property by name [" + params[1]
							+ "]. Current value: " + value);
				} else {
					project.log("Found no such property.");
				}
			} else if ("path".equalsIgnoreCase(params[1])) {
				// look optional component
				// the remaining part of the string could be:
				// id=<someid> or refid=<somerefid>
				Object ref = project.getReference(params[2]);
				if (ref instanceof ResourceCollection) {
					if (ref != null) {
						PathConvert path = (PathConvert) project
								.createTask("pathconvert");
						path.setProject(project);
						path.setPathSep(StringUtils.LINE_SEP + "    - ");
						path.add((ResourceCollection) ref);
						path.execute();
					} else {
						project.log("No path-reference found for " + params[2]);
					}
				} else {
					project.log("No path found for reference id: " + params[2]);
				}
			}

		}

		public void printUsage(Project project) {
			project.log("Incorrect Parameters");
			project.log("Usage: inspect property some.property");
			project.log("       inspect path path.id");
		}
	}
}
