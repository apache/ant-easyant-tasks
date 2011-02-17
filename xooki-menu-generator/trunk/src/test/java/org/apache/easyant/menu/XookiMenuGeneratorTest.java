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
package org.apache.easyant.menu;

import static org.junit.Assert.*;

import org.apache.tools.ant.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class XookiMenuGeneratorTest {

    private File menuFile;
    private XookiMenuGenerator generator;

    @Before
    public void setUp() throws IOException {
        menuFile = File.createTempFile("XookiMenuGeneratorTest", ".json");
        menuFile.deleteOnExit();
        generator = new XookiMenuGenerator();
    }

    @After
    public void tearDown() throws IOException {
        generator = null;
        if (menuFile.exists()) {
            assertTrue(menuFile.delete());
        }
        menuFile = null;
    }

    /** test behavior of calling generator methods out-of-order */
    @Test
    public void testMenuLifecycle() throws IOException {
        //should not be able to append an unopened menu.
        try {
            generator.addEntry("should", "fail");
            fail("should not be able to add an entry to a menu that has not been started");
        } catch (IllegalStateException expected) {}

        //should not be able to append an unopened menu.
        XookiMenuGenerator subMenu = new XookiMenuGenerator();
        try {
            generator.addSubMenu("unopened", subMenu);
            fail("should not be able to add a submenu to a menu that has not been started");
        } catch (IllegalStateException expected) {}

        //should not be able to end an unopened menu
        try {
            generator.endMenu();
            fail("should not be able to end a menu that has not been started");
        } catch (IllegalStateException expected) {}

        //start the menu.
        generator.startMenu("Test", menuFile.getAbsolutePath());

        //should not be able to add an unopened submenu
        try {
            generator.addSubMenu("unopened", subMenu);
            fail("should not be able to add a submenu that is not yet opened");
        } catch (IllegalStateException expected) {}

        //should not be able to start a menu twice
        File dupFile = new File(menuFile.getParentFile(), menuFile.getPath() + ".dup");
        try {
            generator.startMenu("Test", dupFile.getAbsolutePath());
            fail("should not be able to start a menu twice");
        } catch (IllegalStateException expected) {}
        assertFalse("duplicate menu file should not have been created", dupFile.exists());

        //add an entry, close the menu
        generator.addEntry("lonely", "link");
        generator.endMenu();

        try {
            generator.endMenu();
            fail("should not be able to end a menu twice");
        } catch (IllegalStateException expected) {}

        //verify that the menu has the correct content.
        assertEquals("menu contains only data delivered in correct order",
                slurpMenu("testMenuLifecycle.json"), slurpMenu());
    }

    @Test
    public void testEmptyMenu() throws IOException {
        generator.startMenu("empty", menuFile.getAbsolutePath());
        generator.endMenu();
        assertEquals("empty menu is well-formed",
                slurpMenu("testEmptyMenu.json"), slurpMenu());
    }

    @Test
    public void testMultipleEntries() throws IOException {
        generator.startMenu("three", menuFile.getAbsolutePath());
        //also test escaping of title characters.
        generator.addEntry("one", "path/to/one");
        generator.addEntry("'two'", "path/to/two");
        generator.addEntry("item \"three\"", "path/to/three");

        generator.endMenu();
        assertEquals("complex menu is well-formed",
                slurpMenu("testMultipleEntries.json"), slurpMenu());
    }

    @Test
    public void testSubMenu() throws IOException {
        generator.startMenu("parent", menuFile.getAbsolutePath());
        generator.addEntry("one", "path/to/one");

        File subDir = new File(System.getProperty("java.io.tmpdir"), "XookiMenuGeneratorTestSub");
        subDir.mkdir();
        File subfile = File.createTempFile("XookiMenuGeneratorTest-sub", ".json", subDir);

        try {
            XookiMenuGenerator subMenu = new XookiMenuGenerator();
            subMenu.startMenu("child", subfile.getAbsolutePath());

            generator.addSubMenu("Child Menu", subMenu);
            generator.addEntry("item \"three\"", "path/to/three");

            generator.endMenu();
            assertEquals("menu with submenu reference is well-formed",
                    slurpMenu("testSubMenu.json"), slurpMenu());

            subMenu.endMenu();
        } finally {
            subfile.delete();
            subDir.delete();
        }
    }

    /** read the content of the current test menu */
    private String slurpMenu() throws IOException {
        FileReader reader = new FileReader(menuFile);
        try {
            return FileUtils.readFully(reader);
        } finally {
            reader.close();
        }
    }

    /** read the given menu resource to verify test results */
    private String slurpMenu(String resource) throws IOException {
        URL url = getClass().getResource(resource);
        assertNotNull("found classpath resource " + resource, url);
        InputStreamReader reader = new InputStreamReader(url.openStream());
        try {
            return FileUtils.readFully(reader);
        } finally {
            reader.close();
        }
    }
}
