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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;

public class TypeMappings extends DataType {
    private List<Mapping> mappings = new ArrayList<Mapping>();
    private boolean useDefault=true;
    private boolean isDefaultMappingAlreadyLoaded=false;


    public Mapping createMapping() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        Mapping mapping = new Mapping();
        this.mappings.add(mapping);
        return mapping;
    }

    public List<Mapping> getMappings() {
        if (isReference()) {
            return ((TypeMappings) getRefid().getReferencedObject()).getMappings();
        } else {
            if (mappings.size() == 0) {
                log("No typeMappings found, or the typeMappings was empty. Creating default typeMappings",Project.MSG_DEBUG);
            }
            if (mappings.size() == 0 || useDefault) {
                createDefaultTypeMapping();
            }
            return mappings;
        }
    }

    public void setTypeMappings(List<Mapping> mappings) {
        this.mappings = mappings;
    }
    
    public void createDefaultTypeMapping() {
        if (!isDefaultMappingAlreadyLoaded) {
            // basic mapping
            mappings.add(new Mapping("jar"));
            mappings.add(new Mapping("war"));
            mappings.add(new Mapping("ear"));
            mappings.add(new Mapping("javadoc","zip","zip","javadoc"));
            mappings.add(new Mapping("javadoc","jar","jar","javadoc"));
            mappings.add(new Mapping("source","jar","jar","sources"));
            mappings.add(new Mapping("test-source","jar","jar","test-sources"));
            mappings.add(new Mapping("test-jar","jar","jar","test-jar"));
            // default mapping doesn't take care of type/ext, but set mvnPackaging
            // to jar
            mappings.add(new Mapping());
            isDefaultMappingAlreadyLoaded=true;
        }
    }

    public Mapping findTypeMappings(String typeToMatch, String extToMatch) {
        StringBuffer sb = new StringBuffer();
        sb.append("Looking for TypeMapping with type=").append(typeToMatch);
        sb.append(" and ext=").append(extToMatch);
        getProject().log(sb.toString(), Project.MSG_DEBUG);
        for (Mapping mapping : getMappings()) {
            boolean typeMatches = mapping.getType().equals(typeToMatch)
                    || mapping.getType().equals("*");
            boolean extMatches = mapping.getExt().equals(extToMatch)
                    || mapping.getExt().equals("*");
            if (typeMatches && extMatches) {
                sb = new StringBuffer();
                sb.append("TypeMapping found for type=").append(typeToMatch);
                sb.append(" and ext=").append(extToMatch);
                sb.append(" Result:  mvnPackaging=").append(
                        mapping.getMvnPackaging());
                getProject().log(sb.toString(), Project.MSG_DEBUG);
                return mapping;
            }
        }
        sb = new StringBuffer();
        sb.append("no TypeMapping found for type=").append(typeToMatch);
        sb.append(" and ext=").append(extToMatch);
        getProject().log(sb.toString(), Project.MSG_DEBUG);
        return null;
    }
    
    public boolean isUseDefault() {
        return useDefault;
    }

    public void setUseDefault(boolean useDefault) {
        this.useDefault = useDefault;
    }



    public class Mapping {
        private String type;
        private String ext;
        private String mvnPackaging;
        private String classifier;

        public Mapping() {
            type = "*";
            ext = "*";
            mvnPackaging = "jar";
            classifier=null;
        }

        public Mapping(String type) {
            this();
            this.type = type;
            this.ext = type;
            this.mvnPackaging = type;
        }

        public Mapping(String type, String ext, String mvnPackaging) {
            this();
            this.type = type;
            this.ext = ext;
            this.mvnPackaging = mvnPackaging;
        }
        
        public Mapping(String type, String ext, String mvnPackaging, String classifier) {
            this(type,ext,mvnPackaging);
            this.classifier = classifier;
        }


        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getExt() {
            return ext;
        }

        public void setExt(String ext) {
            this.ext = ext;
        }

        public String getMvnPackaging() {
            return mvnPackaging;
        }

        public void setMvnPackaging(String mvnPackaging) {
            this.mvnPackaging = mvnPackaging;
        }

        public String getClassifier() {
            return classifier;
        }

        public void setClassifier(String classifier) {
            this.classifier = classifier;
        }
        
        
    }

}
