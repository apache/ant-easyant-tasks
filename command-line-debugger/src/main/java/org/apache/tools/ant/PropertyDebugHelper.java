package org.apache.tools.ant;

import org.apache.ant.debugger.Auditor;

/**
 * Derives almost everything from parent {@link PropertyHelper}. Only difference
 * being, that it allows an {@link Auditor} instance to attach to itself, which
 * is informed about attempted property changes, which auditor can then choose
 * to record them, and probably present them to the end user as needed.
 * <p />
 */
public class PropertyDebugHelper extends PropertyHelper {

	protected Auditor auditor;

	protected Project project;

	public void setAuditor(Auditor auditor) {
		this.auditor = auditor;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	/*
	 * Implement a callback mechanism here for all these methods that will allow
	 * the control to pass back to the listener
	 */

	public boolean setProperty(String name, Object value, boolean verbose) {
		project.log("Auditing change to property: " + name, Project.MSG_DEBUG);
		auditor.auditPropertyChange(name, value, project);
		// call back can take control before the actual property change is
		// invoked?
		return super.setProperty(name, value, verbose);
	}

	public void setNewProperty(String name, Object value) {
		project.log("Auditing change to property: " + name, Project.MSG_DEBUG);
		auditor.auditPropertyChange(name, value, project);
		super.setNewProperty(name, value);
	}

	public void setUserProperty(String name, Object value) {
		project.log("Auditing change to property: " + name, Project.MSG_DEBUG);
		auditor.auditPropertyChange(name, value, project);
		super.setUserProperty(name, value);
	}

	public void setInheritedProperty(String name, Object value) {
		project.log("Auditing change to property: " + name);
		auditor.auditPropertyChange(name, value, project);
		super.setInheritedProperty(name, value);
	}

}
