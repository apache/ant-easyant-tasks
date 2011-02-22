/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.easyant.tasks.adapters;

import org.apache.easyant.tasks.adapters.TypeMappings.Mapping;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class InfoAdapter extends AbstractMavenIvyAdapter {

    private TypeMappings typeMappings;
    private static final String DEFAULT_GROUP_ID = "org.apache.maven";
    private static final String DEFAULT_ARTIFACT_ID = "super-pom";
    private static final String DEFAULT_VERSION = "2.0";
    private static final String DEFAULT_APPLICATION_NAME = "Maven Default Project";

    @Override
    public void doExecute() throws BuildException {
        prepareAndCheck();

        ModuleRevisionId mrid = getResolvedReport().getModuleDescriptor()
                .getModuleRevisionId();
        if (getModel().getGroupId().equals(DEFAULT_GROUP_ID)) {
            log("Setting groupId to : " + mrid.getOrganisation(),
                    Project.MSG_DEBUG);
            getModel().setGroupId(mrid.getOrganisation());
        }
        if (getModel().getName().equals(DEFAULT_APPLICATION_NAME)) {
            log("Setting name to : " + mrid.getName(), Project.MSG_DEBUG);
            getModel().setName(mrid.getName());
        }
        if (getModel().getArtifactId().equals(DEFAULT_ARTIFACT_ID)) {
            log("Setting artifactId to : " + mrid.getName(), Project.MSG_DEBUG);
            getModel().setArtifactId(mrid.getName());
        }
        if (getModel().getVersion().equals(DEFAULT_VERSION)) {
            log("Setting version to : " + mrid.getRevision(), Project.MSG_DEBUG);
            getModel().setVersion(mrid.getRevision());
        }
        if (getModel().getDescription() == null) {
            log("Setting description to : "
                    + getResolvedReport().getModuleDescriptor()
                            .getDescription(), Project.MSG_DEBUG);
            getModel().setDescription(
                    getResolvedReport().getModuleDescriptor().getDescription());
        }
        // TODO: Handle multiple artifacts
        if (getResolvedReport().getModuleDescriptor().getAllArtifacts().length >= 1
                && getResolvedReport().getModuleDescriptor().getAllArtifacts()[0] != null) {
            Artifact artifact = (Artifact) getResolvedReport()
                    .getModuleDescriptor().getAllArtifacts()[0];
            Mapping mapping = getTypeMappings().findTypeMappings(
                    artifact.getType(), artifact.getExt());
            if (mapping != null) {
                if (mapping.getMvnPackaging() != null) {
                    getModel().setPackaging(mapping.getMvnPackaging());
                }
            }
        }

    }

    public void addTypeMappings(TypeMappings typeMappings) {
        this.typeMappings = typeMappings;
    }

    public TypeMappings getTypeMappings() {
        if (typeMappings == null) {
            // create a default one
            typeMappings = new TypeMappings();
            typeMappings.setProject(getProject());
        }

        return typeMappings;
    }

    public void setTypeMappings(TypeMappings typeMappings) {
        this.typeMappings = typeMappings;
    }

}
