package org.apache.tools.ant.listener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.ant.debugger.Auditor;
import org.apache.ant.debugger.DebugCommandSet;
import org.apache.ant.debugger.DebugPrompt;
import org.apache.ant.debugger.DebugSupport;
import org.apache.ant.debugger.DefaultAuditor;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyDebugHelper;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.input.InputHandler;

// The input-listener should itself be pluggable
/*
 * We can probably build up a stack of targets in execution to allow dumping a stack of a target
 * when a break point occurs?
 *
 * This class also doubles as a DebugSupport Command that can be used to add new targets / properties
 * to debug at runtime.
 */
public class DebuggerListener implements BuildListener, DebugSupport {

	/**
	 * The project being debugged.
	 */
	protected Project project = null;

	/**
	 * List of all targets at which break points have been applied.
	 */
	protected List debugtargets = new LinkedList();

	/**
	 * Indicates if this listener instance has been initialized.
	 */
	protected boolean inited = false;

	/**
	 * Debugger instance.
	 */
	protected DebugPrompt prompt = null;

	/**
	 * Contains the current target being executed.
	 * 
	 * TODO: This is not being used now but there is a possibility for its use.
	 */
	protected Target currentTarget = null;

	/**
	 * An Auditor instance that keeps track of all changes to properties
	 * identified by the user.
	 * 
	 * 
	 * TODO: The auditor instance itself should be pluggable, I think to allow
	 * different PropertyHelpers to be used.
	 */
	protected Auditor auditor = null;

	/**
	 * InputHandler instance that may be different than the configured one.
	 */
	protected InputHandler inputHandler;

	/**
	 * {@link DebugCommandSet} instance that is a container for all
	 * {@link DebugSupport} commands that may be used with this listener.
	 */
	protected DebugCommandSet commandHandler = new DebugCommandSet();

	// List of all break point facilities to be available
	// ==================================================
	/**
	 * Target break point property name
	 */
	public static final String DEBUG_TARGET_PROPERTY = "ant.debug.target";

	/**
	 * Exception break point property name
	 */
	public static final String DEBUG_EXCEPTION_PROPERTY = "ant.debug.exception";

	/**
	 * Break point when the value of a property changes or is attempted to be
	 * changed.
	 */
	public static final String DEBUG_PROPERTY_CHANGE_PROPERTY = "ant.debug.property";

	/**
	 * Request to audit all attempted changes to a property
	 */
	public static final String AUDIT_PROPERTY_PROPERTY = "ant.audit.property";

	// End of all available break points
	// =================================

	public void init(BuildEvent event) {
		project = event.getProject();
		project.log("Attaching DebuggerListener to the build.");

	}

	public void buildFinished(BuildEvent event) {
		// Anything to do?
	}

	protected Map getDefaultCommandSupport() {
		if (auditor instanceof DebugSupport) {
			Map defaults = new HashMap();
			defaults.put("trace", auditor);
			defaults.put("break", this);
			defaults.put("watch", this);
			return defaults;
		}
		return null;
	}

	public void buildStarted(BuildEvent event) {
		// set audit property helper that will help keep track of properties
		// being set everywhere.
		project = event.getProject();
		PropertyHelper helper = new PropertyDebugHelper();
		helper.setProject(project);
		auditor = new DefaultAuditor();
		((PropertyDebugHelper) helper).setAuditor(auditor);
		// Is it better to set it as a project helper or as a delegate to the
		// helper?
		project.addReference(MagicNames.REFID_PROPERTY_HELPER, helper);
		commandHandler.setProject(project);
		commandHandler.init(getDefaultCommandSupport());
		prompt = new DebugPrompt(project, commandHandler);
		auditor.setPrompt(prompt);

		// this is how the debugging starts
		prompt.prompt("Type /? to get any help.");
	}

	public void messageLogged(BuildEvent event) {
		// Do Nothing
	}

	public void targetFinished(BuildEvent event) {
		currentTarget = null;
	}

	public void targetStarted(BuildEvent event) {
		currentTarget = event.getTarget();
		Target target = event.getTarget();
		if (debugtargets.contains(target.getName())) {
			prompt.prompt("Break Point at Target: " + target.getName());
		}
	}

	public void taskFinished(BuildEvent event) {
	}

	public void taskStarted(BuildEvent event) {
	}

	/*
	 * The following section of code customizes this class to double up as a
	 * DebugSupport command instance so that it is possible to add
	 * target/property break points at runtime.
	 */

	/*
	 * TODO INFO command is yet to be implemented. The idea is to output a list
	 * of all target break points and properties being monitored with their
	 * statuses (Pending / Done etc).
	 */
	public void execute(Project project, String[] params) {
		if (params.length > 1 && "/?".equals(params[1])) {
			printUsage(project);
			return;
		}
		if (params.length < 2) {
			project.log("Incorrect Parameters");
			printUsage(project);
			return;
		}

		String command = params[0];
		if ("break".equalsIgnoreCase(command)) {
			for (int i = 1; i < params.length; i++) {
				debugtargets.add(params[i]);
				project.log("Added BreakPoint at Target: " + params[i]);
			}
		} else if ("watch".equalsIgnoreCase(command)) {
			/*
			 * watch points for properties
			 */
			for (int i = 1; i < params.length; i++) {
				auditor.addPropertyForAudits(params[i], project);
				project.log("Added BreakPoint at Property: " + params[i]);
			}
		} else if ("info".equalsIgnoreCase(command)) {
			// TODO show all break points and watch points here
		}
	}

	public void printUsage(Project project) {
		project.log("Usage: break property some.property.1 some.property.2");
		project.log("       break target some.target.1 some.target.2");
		project
				.log("The above command will add all properties/targets as breakpoints.");
	}

}
