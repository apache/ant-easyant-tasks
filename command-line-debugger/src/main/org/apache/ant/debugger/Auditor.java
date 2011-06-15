package org.apache.ant.debugger;

import java.util.List;

import org.apache.tools.ant.Project;

/**
 * Audits all calls to change property values
 * 
 * Right now only properties are audited: We can try to figure a way to audit
 * other data-types if it adds any value.
 */
public interface Auditor {

	/**
	 * Marks a property to be tracked for attempted changes.
	 * 
	 * @param property
	 */
	public void addPropertyForAudits(String property, Project project);

	/**
	 * Audits change to a property by name <code>property</code>.
	 * 
	 * @param property
	 * @param value
	 * @param project
	 */
	public void auditPropertyChange(String property, Object value,
			Project project);

	/**
	 * Returns all records for the key.
	 * 
	 * @param key
	 * @return
	 */
	public List getAuditsForProperty(String key);

	/**
	 * Provide a hook for debuggers to attach to this auditor so that it may be
	 * able to pass back the control to the Debugger in case of a Property
	 * Breakpoint.
	 * 
	 * @param debugger
	 */
	public void setPrompt(DebugPrompt debugger);

}
