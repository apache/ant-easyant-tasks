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

import java.util.Iterator;

import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.plugins.parser.m2.PomModuleDescriptorWriter.ConfigurationScopeMapping;
import org.apache.maven.model.Dependency;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.codehaus.plexus.util.StringUtils;

public class DependencyAdapter extends AbstractMavenIvyAdapter {

    private ScopeMappings scopeMapping;

    @Override
    public void doExecute() throws BuildException {
        prepareAndCheck();
        //create a configuration scope mapping helper with our mapping
        ConfigurationScopeMapping configurationScopeMapping= getScopeMapping().getConfigurationScopeMapping();
        //loop on project dependencies to build maven dependencies entries
        for (Iterator iterator = getResolvedReport().getDependencies().iterator(); iterator.hasNext();) {
            IvyNode node = (IvyNode) iterator.next();
            ModuleRevisionId dependencyRevisionId =node.getResolvedId();
            Dependency mavenDependency = new Dependency();
            log("Building maven dependency entry with " + dependencyRevisionId.toString(), Project.MSG_DEBUG);
            mavenDependency.setGroupId(dependencyRevisionId.getOrganisation());
            mavenDependency.setArtifactId(dependencyRevisionId.getName());
            mavenDependency.setVersion(dependencyRevisionId.getRevision());
            String confToCheck = StringUtils.join(node.getRootModuleConfigurations(),", ");
            log("Checking mapping for configuration : " + confToCheck,Project.MSG_DEBUG);
            String scope = configurationScopeMapping.getScope(node.getRootModuleConfigurations());
            if (scope != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Mapping found for configuration ").append(confToCheck);
                sb.append( " with scope ").append(scope);
                log(sb.toString(),Project.MSG_DEBUG);
                mavenDependency.setScope(scope);
            }

            if (configurationScopeMapping.isOptional(node.getRootModuleConfigurations())) {
                log("Setting " + dependencyRevisionId.toString() + " as optional" ,Project.MSG_DEBUG);
                mavenDependency.setOptional(true);
            }

            getPom().addConfiguredDependency(mavenDependency);
        }

    }

    public void add(ScopeMappings scopeMappings) {
        this.scopeMapping= scopeMappings;
    }



    public ScopeMappings getScopeMapping() {
        if (scopeMapping == null) {
            scopeMapping=  new ScopeMappings();
            scopeMapping.setProject(getProject());
        }
        return scopeMapping;
    }

    public void setScopeMapping(ScopeMappings scopeMapping) {
        this.scopeMapping = scopeMapping;
    }

}
