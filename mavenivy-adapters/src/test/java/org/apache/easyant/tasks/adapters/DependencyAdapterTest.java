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

import org.apache.tools.ant.BuildFileTest;
import org.junit.Test;

public class DependencyAdapterTest extends BuildFileTest {

    public DependencyAdapterTest() {
        super();
    }
    
    /**
     * Assert that the given substring is not in the log messages.
     */
    public void assertDebugLogNotContaining(String substring) {
        String realLog = getFullLog();
        assertFalse("didn't expect debug log to contain \"" + substring + "\" debug log was \""
                    + realLog + "\"",
                    realLog.indexOf(substring) >= 0);
    }


    @Override
    protected void setUp() throws Exception {
        configureProject("src/test/resources/org/apache/easyant/tasks/adapters/DependencyAdapter/build.xml");
    }
    
    @Test
    public void testBasicUsage() {
        executeTarget("basicUsage");
        assertDebuglogContaining("Building maven dependency entry with org.apache.ivy#ivy;2.1.0");
        assertDebuglogContaining("Checking mapping for configuration : default, runtime");
        assertDebuglogContaining("Mapping found for configuration default, runtime with scope runtime");
        
        assertDebuglogContaining("Building maven dependency entry with hsqldb#hsqldb;1.8.0.7");
        assertDebuglogContaining("Checking mapping for configuration : default");
        assertDebuglogContaining("Mapping found for configuration default with scope compile");
        assertDebuglogContaining("Building maven dependency entry with junit#junit;4.4");
        assertDebuglogContaining("Checking mapping for configuration : test");
        assertDebuglogContaining("Mapping found for configuration test with scope test");
        
    }
    
    @Test
    public void testCustomMapping() {
        executeTarget("customMapping");
        assertDebuglogContaining("Building maven dependency entry with hsqldb#hsqldb;1.8.0.7");
        assertDebuglogContaining("Checking mapping for configuration : default");
        assertDebuglogContaining("Mapping found for configuration default with scope compile");
        assertDebuglogContaining("Building maven dependency entry with junit#junit;4.4");
        assertDebuglogContaining("Checking mapping for configuration : test");
        assertDebuglogContaining("Mapping found for configuration test with scope test");
    
    }

    
    @Test
    public void testWithoutPomRef() {
        expectBuildException("withoutPomRef", "pomRefId is required !");
            
    }
    
    @Test
    public void testWrongPomRef() {
        expectBuildException("wrongPomRef", "pomRefId references an unexisting pom instance !");
            
    }

    
}
