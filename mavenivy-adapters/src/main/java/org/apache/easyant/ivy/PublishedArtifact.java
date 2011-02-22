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

public class PublishedArtifact {
    
    private String artifact;
    private String type;
    private String ext;
    private String resolver;
    private String file;
    
    public String getArtifact() {
        return artifact;
    }
    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }
    public String getExt() {
        return ext;
    }
    public void setExt(String ext) {
        this.ext = ext;
    }
    public String getResolver() {
        return resolver;
    }
    public void setResolver(String resolver) {
        this.resolver = resolver;
    }
    public String getFile() {
        return file;
    }
    public void setFile(String file) {
        this.file = file;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    

}
