/* 
 *  Copyright 2008-2010 the EasyAnt project
 * 
 *  See the NOTICE file distributed with this work for additional information
 *  regarding copyright ownership. 
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package org.apache.easyant.tasks.adapters;

import org.apache.ivy.ant.IvyPostResolveTask;
import org.apache.maven.artifact.ant.Pom;
import org.apache.maven.model.Model;
import org.apache.tools.ant.BuildException;

public abstract class AbstractMavenIvyAdapter extends IvyPostResolveTask {

	private String pomRefId;
	private Pom pom;

	public AbstractMavenIvyAdapter() {
		super();
	}

	public String getPomRefId() {
		return pomRefId;
	}

	public void setPomRefId(String pomRefId) {
		this.pomRefId = pomRefId;
	}

	public Pom getPom() {
		if (pom == null) {
			pom = (Pom) getProject().getReference(pomRefId);
		}
		return pom;
	}

	public Model getModel() {
		return getPom().getModel();
	}

	@Override
	protected void prepareTask() {
		super.prepareTask();
		if (pomRefId == null) {
			throw new BuildException("pomRefId is required !");
		}
		if (getPom() == null) {
			throw new BuildException("pomRefId references an unexisting pom instance !");
		}
	}



}