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
package org.apache.easyant.ivy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ivy.ant.IvyTask;
import org.apache.ivy.core.IvyContext;
import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.event.IvyEvent;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.plugins.resolver.URLResolver;
import org.apache.ivy.plugins.trigger.AbstractTrigger;
import org.apache.ivy.plugins.trigger.Trigger;
import org.apache.tools.ant.Project;

public class MavenPublishTrigger extends AbstractTrigger implements Trigger {
    
    public static final String MAVEN_PUBLISH_TRIGGER_REFERENCE ="maven.publish.trigger.ref";
    private List<PublishedArtifact> publishedArtifacts = new ArrayList<PublishedArtifact>();
    private String publishUrl = null;

    public MavenPublishTrigger() {
        super();
    }
    
    @Override
    public void progress(IvyEvent event) {
    
        Project project = (Project) IvyContext
                .peekInContextStack(IvyTask.ANT_PROJECT_CONTEXT_KEY);
        if (project == null) {
            return;
        }
        if (project.getReference(MAVEN_PUBLISH_TRIGGER_REFERENCE) == null) {
            project.addReference(MAVEN_PUBLISH_TRIGGER_REFERENCE, this);
        }

        Map attributes = event.getAttributes();
        String artifact = (String) attributes.get("artifact");
        String artifactFile = (String) attributes.get("file");
        String type = (String) attributes.get("type");
        String ext = (String) attributes.get("ext");
        String resolver = (String) attributes.get("resolver");
        
        PublishedArtifact publishedArtifact = new PublishedArtifact();
        publishedArtifact.setArtifact(artifact);
        publishedArtifact.setExt(ext);
        publishedArtifact.setFile(artifactFile);
        publishedArtifact.setType(type);
        
        publishedArtifacts.add(publishedArtifact);
        
        extractDeployUrl(resolver);

    }

    /**
     * @param resolver
     */
    protected void extractDeployUrl(String resolver) {
        //try to get publish url if the Resolver used to publish was a IBiblioResolver
        if (publishUrl == null) {
            DependencyResolver dependencyResolver = IvyContext.getContext()
                    .getSettings().getResolver(resolver);
            if (dependencyResolver instanceof IBiblioResolver) {
                IBiblioResolver ibiblioResolver = (IBiblioResolver) dependencyResolver;
                publishUrl = ibiblioResolver.getRoot();
            } else  if (dependencyResolver instanceof URLResolver) {
                URLResolver urlResolver = (URLResolver) dependencyResolver;
                //get the whole pattern
                publishUrl = (String) urlResolver.getArtifactPatterns().get(0);
                //only keep the token root
                publishUrl = IvyPatternHelper.getTokenRoot(publishUrl);
                
            }
        }
    }

    public List<PublishedArtifact> getPublishedArtifacts() {
        return publishedArtifacts;
    }

    public void setPublishedArtifacts(List<PublishedArtifact> publishedArtifacts) {
        this.publishedArtifacts = publishedArtifacts;
    }

    public String getPublishUrl() {
        return publishUrl;
    }

    public void setPublishUrl(String publishUrl) {
        this.publishUrl = publishUrl;
    }
    
    

}
