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
package org.apache.easyant.tasks;

import org.apache.ivy.ant.IvyTask;
import org.apache.ivy.plugins.trigger.AbstractTrigger;
import org.apache.ivy.plugins.trigger.Trigger;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.ClasspathUtils;

public class RegisterTrigger extends IvyTask {
    private String event;
    private String eventFilter;
    private String className;
    private Path classpath;
    
    @Override
    public void doExecute() throws BuildException {
        
        Trigger trigger = (Trigger) ClasspathUtils.newInstance(getClassName(),
                getClassLoader(), Trigger.class);
        if (trigger instanceof AbstractTrigger) {
            AbstractTrigger abstractTrigger = (AbstractTrigger) trigger;
            abstractTrigger.setEvent(getEvent());
            getSettings().addTrigger(abstractTrigger);
            getIvyInstance().getEventManager().addIvyListener(trigger, trigger.getEventFilter());
        }

    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
    
    

    public String getEventFilter() {
        return eventFilter;
    }

    public void setEventFilter(String eventFilter) {
        this.eventFilter = eventFilter;
    }

    /**
     * Get the classname to register
     * 
     * @return a classname
     */
    public String getClassName() {
        return className;
    }

    /**
     * Set the classname to register
     * 
     * @param className
     *            a classname
     */
    public void setClassName(String className) {
        this.className = className;
    }

    protected AntClassLoader getClassLoader() {
        // defining a new specialized classloader and setting it as the thread
        // context classloader
        AntClassLoader loader = null;
        if (classpath != null) {
            loader = new AntClassLoader(this.getClass().getClassLoader(),
                    getProject(), classpath, true);
        } else {
            loader = new AntClassLoader(this.getClass().getClassLoader(), true);
        }
        loader.setThreadContextLoader();
        return loader;
    }

    /**
     * Get the classpath used to locate the specified classname
     * 
     * @return a classpath
     */
    public Path getClasspath() {
        return classpath;
    }

    /**
     * The the classpath used to locate the specified classname
     * 
     * @param classpath
     */
    public void setClasspath(Path classpath) {
        createClasspath().append(classpath);
    }

    /**
     * Classpath to use, by reference, when compiling the rulebase
     * 
     * @param a
     *            reference to an existing classpath
     */
    public void setClasspathref(Reference r) {
        createClasspath().setRefid(r);
    }

    /**
     * Adds a path to the classpath.
     * 
     * @return created classpath
     */
    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return this.classpath.createPath();
    }

}
