package org.apache.ant.debugger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

public class DebugUtils {
	
	public static List searchTask(Class expectedTaskClass, Project project) {
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

}
