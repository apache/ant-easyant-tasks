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

public class InfoAdapterTest extends BuildFileTest {

    public InfoAdapterTest() {
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
        configureProject("src/test/resources/org/apache/easyant/tasks/adapters/InfoAdapter/build.xml");
    }
    
    @Test
    public void testBasicUsage() {
        executeTarget("basicUsage");
        assertDebuglogContaining("Setting groupId to : org.mycompany");
        assertDebuglogContaining("Setting name to : myProject");
        assertDebuglogContaining("Setting artifactId to : myProject");

        assertDebuglogContaining("Setting version to : 0.1");

        assertDebuglogContaining("Setting description to : foobar description");

        assertDebuglogContaining("No typeMappings found, or the typeMappings was empty. Creating default typeMappings");

        assertDebuglogContaining("Looking for TypeMapping with type=jar and ext=jar");
        assertDebuglogContaining("TypeMapping found for type=jar and ext=jar Result:  mvnPackaging=jar");
    }
    
    @Test
    public void testCustomMapping() {
        executeTarget("customMapping");
        assertDebuglogContaining("Setting groupId to : org.mycompany");
        assertDebuglogContaining("Setting name to : myProject");
        assertDebuglogContaining("Setting artifactId to : myProject");

        assertDebuglogContaining("Setting version to : 0.1");

        assertDebuglogContaining("Setting description to : foobar description");
        assertDebugLogNotContaining("No typeMappings found, or the typeMappings was empty. Creating default typeMappings");

        assertDebuglogContaining("Looking for TypeMapping with type=jar and ext=jar");
        assertDebuglogContaining("TypeMapping found for type=jar and ext=jar Result:  mvnPackaging=myPackaging");
    }

    @Test
    public void testContainingMvnMetadata() {
        executeTarget("containingMvnMetadata");
        //those three information are already set by the <pom> task 
        assertDebugLogNotContaining("Setting groupId to : org.mycompany");
        assertDebugLogNotContaining("Setting artifactId to : myProject");
        assertDebugLogNotContaining("Setting version to : 0.1");
        
        assertDebuglogContaining("Setting name to : myProject");

        assertDebuglogContaining("Setting description to : foobar description");
        assertDebuglogContaining("Looking for TypeMapping with type=jar and ext=jar");
        assertDebuglogContaining("TypeMapping found for type=jar and ext=jar Result:  mvnPackaging=jar");
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
