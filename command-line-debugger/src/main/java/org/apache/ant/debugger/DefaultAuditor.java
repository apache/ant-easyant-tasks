package org.apache.ant.debugger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

/**
 * Default implementation of {@link Auditor}. At the same time, this class also
 * acts as a Debug command permitting the recorded audits to be available on
 * user commands.
 * <p />
 * Optionally, this class allows a {@link DebugPrompt} instance to attach itself
 * to this auditor, so that if change to a certain property is attempted, then
 * the debugger may be chosen to be transferred the control to.
 */
public class DefaultAuditor implements Auditor, DebugSupport {

	protected Map propertyaudits = new HashMap();

	protected DebugPrompt prompt = null;

	public void setPrompt(DebugPrompt prompt) {
		this.prompt = prompt;
	}

	public void addPropertyForAudits(String property, Project project) {
		// the list in the value will keep track of all attempted changes to
		// this property
		project.log("Adding Property: " + property + " to be audited.",
				Project.MSG_DEBUG);
		propertyaudits.put(property, new LinkedList());
	}

	public void auditPropertyChange(String property, Object value,
			Project project) {
		List audits = (List) propertyaudits.get(property);
		if (audits != null) {
			project.log("Adding Attempted Change record for Property: "
					+ property, Project.MSG_DEBUG);
			// if any target executes while currentTarget = null, it indicates
			// the property
			// transition happened outside the scope of any target, directly
			// inside the build file
			PropertyHelper helper = PropertyHelper.getPropertyHelper(project);
			StringBuffer sb = new StringBuffer();
			sb.append("Property [").append(property).append(
					"] change attempted from [");
			sb.append(helper.getProperty(property)).append("] to [");
			sb.append(value).append("]");
			String message = sb.toString();
			audits.add(message);

			// if there is a debugger attached to this instance, pass the
			// control to the debugger
			if (prompt != null)
				prompt.prompt(message);
		}

	}

	public List getAuditsForProperty(String key) {
		return (List) propertyaudits.get(key);
	}

	public void execute(Project project, String[] params) {
		if (params.length > 1 && "/?".equals(params[1])) {
			printUsage(project);
			return;
		}
		if (params.length != 2) {
			project.log("Incorrect Parameters");
			printUsage(project);
			return;
		}

		String property = params[1];

		List changes = getAuditsForProperty(property);
		if (changes != null) {
			project.log("Found " + changes.size()
					+ " Attempted Changes to Property: " + property);
			Iterator it = changes.iterator();
			while (it.hasNext()) {
				project.log("  - " + (String) it.next());
			}
		} else {
			project.log("  - No records found for Property: " + property);
		}
	}

	public void printUsage(Project project) {
		project.log("Usage: trace some.property");
		project
				.log("The above command will return all modification attempts on the specified property.");
	}

}
