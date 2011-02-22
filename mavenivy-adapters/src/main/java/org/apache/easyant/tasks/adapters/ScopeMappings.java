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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ivy.plugins.parser.m2.PomModuleDescriptorWriter.ConfigurationScopeMapping;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;

public class ScopeMappings extends DataType {

    private List<Mapping> mappings = new ArrayList<Mapping>();
    private boolean useDefault=true;
    private boolean isDefaultMappingAlreadyLoaded=false;

    public Mapping createMapping() {
        Mapping mapping = new Mapping();
        this.mappings.add(mapping);
        return mapping;
    }

    public List<Mapping> getMappings() {
        if (isReference()) {
            return ((ScopeMappings) getRefid().getReferencedObject())
                    .getMappings();
        } else {
            return mappings;
        }
    }

    public void setMappings(List<Mapping> mappings) {
        this.mappings = mappings;
    }

    public Map<String, String> getMappingsMap() {
        Map<String, String> mappingsMap = new HashMap<String, String>();

        for (Iterator<Mapping> iter = getMappings().iterator(); iter.hasNext();) {
            Mapping mapping = (Mapping) iter.next();
            mappingsMap.put(mapping.getConf(), mapping.getScope());
        }
        return mappingsMap;
    }

    public ConfigurationScopeMapping getConfigurationScopeMapping() {
        if (getMappings().size() == 0) {
            log("no configurationScopeMapping found, will use the default one",
                    Project.MSG_DEBUG);
        }
        
        if (getMappings().size() == 0 || isUseDefault()) {
            createDefaultConfigurationScopeMapping();
        }
        return new ConfigurationScopeMapping(getMappingsMap());
        
    }

    /**
     * Creates the default configuration scope mapping
     */
    public void createDefaultConfigurationScopeMapping() {
        if (!isDefaultMappingAlreadyLoaded) {
            mappings.add(new Mapping("default", "compile"));
            // default that extends compile
            mappings.add(new Mapping("default, compile", "compile"));
            // default that extends runtime
            mappings.add(new Mapping("default, runtime", "runtime"));
            // default that extends runtime that extends compile
            mappings.add(new Mapping("default, compile, runtime", "compile"));
            // runtime that extends compile
            mappings.add(new Mapping("compile, runtime", "compile"));
            mappings.add(new Mapping("runtime", "runtime"));
            mappings.add(new Mapping("provided", "provided"));
            mappings.add(new Mapping("test", "test"));
            mappings.add(new Mapping("system", "system"));
            isDefaultMappingAlreadyLoaded=true;
        }
    }
    
    public boolean isUseDefault() {
        return useDefault;
    }

    public void setUseDefault(boolean useDefault) {
        this.useDefault = useDefault;
    }



    public class Mapping {
        private String conf;
        private String scope;

        public Mapping() {
        }
        

        public Mapping(String conf, String scope) {
            this.conf=conf;
            this.scope=scope;
        }

        public String getConf() {
            return conf;
        }

        public void setConf(String conf) {
            this.conf = conf;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }
    }

}
