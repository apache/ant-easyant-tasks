package org.apache.ant.debugger;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.taskdefs.PathConvert;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.util.StringUtils;

/**
 * Inspects the current value of a property or path.
 */
public class Inspector implements DebugSupport {

	public void execute(Project project, String[] params) {
		if (params.length > 1 && "/?".equals(params[1])) {
			printUsage(project);
			return;
		}
		if (params.length < 3) {
			project.log("Incorrect Parameters");
			printUsage(project);
			return;
		}

		if ("property".equalsIgnoreCase(params[1])) {
			// show all matches for a property
			Object value = PropertyHelper.getProperty(project, params[2]);
			if (value != null) {
				project.log("Detected a property by name [" + params[2]
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
		project.log("Usage: inspect property some.property");
		project.log("       inspect path path.id");
	}
}