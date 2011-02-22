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

import java.io.File;

import org.apache.easyant.ivy.MavenPublishTrigger;
import org.apache.easyant.ivy.PublishedArtifact;
import org.apache.easyant.tasks.adapters.TypeMappings.Mapping;
import org.apache.maven.artifact.ant.AttachedArtifact;
import org.apache.maven.artifact.ant.Authentication;
import org.apache.maven.artifact.ant.DeployTask;
import org.apache.maven.artifact.ant.RemoteRepository;
import org.apache.maven.settings.Server;
import org.apache.tools.ant.Project;

public class Deploy extends DeployTask {
    
    private TypeMappings typeMappings;

    @Override
    protected void doExecute() {
        MavenPublishTrigger trigger =  (MavenPublishTrigger) getProject().getReference(MavenPublishTrigger.MAVEN_PUBLISH_TRIGGER_REFERENCE);

        RemoteRepository repo = getRemoteRepository();
        if (getRemoteRepository().getRefid() != null) {
            repo = (RemoteRepository) getProject().getReference( getRemoteRepository().getRefid());
        }
        boolean repoUrlIsEmpty = repo.getUrl() ==null || repo.getUrl().equals("");
        if (repoUrlIsEmpty && trigger.getPublishUrl() != null) {
            log("No repository url was specified, will use same as ivy publication context", Project.MSG_VERBOSE);
            log("Repository url is now set to " + trigger.getPublishUrl(),Project.MSG_VERBOSE);
            repo.setUrl(trigger.getPublishUrl());
        }
        
        //the main artifact
        PublishedArtifact mainArtifact = trigger.getPublishedArtifacts().get(0);
        setFile(new File(mainArtifact.getFile()));
        for (PublishedArtifact publishedArtifact : trigger.getPublishedArtifacts()) {
            boolean isIvyFile = "ivy".equals(publishedArtifact.getType())
                        && "xml".equals(publishedArtifact.getExt());
            //ignore main artifact and ivy.xml file
            if (!(publishedArtifact.equals(mainArtifact) || isIvyFile)){
                Mapping mapping = getTypeMappings().findTypeMappings(
                        publishedArtifact.getType(), publishedArtifact.getExt());
                if (mapping != null) {
                    if (mapping.getMvnPackaging() != null) {
                        AttachedArtifact artifact = createAttach();
                        artifact.setFile(new File(publishedArtifact.getFile()));
                        artifact.setType(mapping.getMvnPackaging());
                        artifact.setClassifier(mapping.getClassifier());
                        
                        StringBuilder sb = new StringBuilder();
                        sb.append("publishing ").append(publishedArtifact.getFile());
                        sb.append(" type='").append(artifact.getType()).append("'");
                        if (artifact.getClassifier() != null) {
                            sb.append(" classifier='").append(artifact.getClassifier()).append("'");
                        }
                        log(sb.toString());
                    }
                }
            }
        }
        
        super.doExecute();
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
    
    @Override
    protected void updateRepositoryWithSettings( RemoteRepository repository )
    {
        if ( repository.getAuthentication() == null )
        {
            Server server = getSettings().getServer( repository.getId() );
            if ( repository.getRefid() != null && server != null)
             {
                 RemoteRepository instance = (RemoteRepository) getProject().getReference( repository.getRefid());
                 instance.addAuthentication( new Authentication( server ) );
             }
        }
         
        super.updateRepositoryWithSettings(repository);
    }


}
