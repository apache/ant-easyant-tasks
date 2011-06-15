package org.apache.tools.ant;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ant.debugger.DebugSupport;
import org.apache.ant.debugger.DebugUtils;

import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.Path;

/**
 * Locates properties / paths in static build sources
 */
public class Locator implements DebugSupport {

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
			matches = DebugUtils.searchTask(Property.class, project);
			key = "name";
		} else if ("path".equalsIgnoreCase(params[1])) {
			// locate and publish the path
			matches = DebugUtils.searchTask(Path.class, project);
			key = "id";
		} else {
			// see if any other component may be supported
			project.log("Unexpected component: " + params[1]);
			project.log("Supported components are property, path.");
			return;
		}

		boolean found = false;

		// probably accept some kind of a query from end user and select the
		// target object based on the query
		for (Iterator iterator = matches.iterator(); iterator.hasNext();) {
			Task task = (Task) iterator.next();
			// display attributes
			Map attributeMap = task.getWrapper().getAttributeMap();
			if (!params[2].equals(attributeMap.get(key))) {
				continue;
			}
			found = true;
			String value = (String) attributeMap.get("value");
			project.log("Detected a property by name [" + params[2]
					+ "]. Build file value: " + value);
			// and their respected location
			project.log("Located at: " + task.getLocation().toString());
		}

		if (!found) {
			project.log("No property by name [" + params[2] + "] found.");
		}
	}

	public void printUsage(Project project) {
		project.log("Incorrect Parameters");
		project.log("Usage: locate property/path propertyname/pathname");
	}
}