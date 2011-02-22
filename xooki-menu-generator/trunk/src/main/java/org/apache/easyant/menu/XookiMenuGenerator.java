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

import org.apache.easyant.core.menu.MenuGenerator;
import org.apache.tools.ant.util.FileUtils;

import java.io.*;
import java.util.NoSuchElementException;

/**
 * Generates a <a href="http://xooki.sourceforge.net/">xooki</a> JSON menu file.
 */
public class XookiMenuGenerator implements MenuGenerator<XookiMenuGenerator> {

    private boolean closed;

    private String id;
    private File path;
    private Writer fileWriter;

    private Block currentBlock;

    public void startMenu(String title, String location) throws IOException {
        if (fileWriter != null) {
            throw new IllegalStateException("Menu has already been opened for writing at " + path.getAbsolutePath());
        }
        closed = false;
        
        path = new File(location);
        fileWriter = new BufferedWriter(new FileWriter(path));
        currentBlock = new Block();

        //start the toplevel menu object.
        currentBlock = currentBlock.startObject();

        if (title == null) {
            id = toId(location);
            currentBlock.appendAttribute("id", id);
        } else {
            id = toId(title);
            currentBlock
                 .appendAttribute("id", id)
                 .appendAttribute("title", title);
        }

        //add all child entries to an array called "children"
        currentBlock = currentBlock.startArray("children");
    }

    public void addSubMenu(String title, XookiMenuGenerator subMenu) throws IOException {
        assertOpen();
        subMenu.assertOpen();

        currentBlock
            .startObject()
                .appendAttribute("title", title)
                .appendAttribute("importNode", subMenu.id)
                .appendAttribute("importRoot", computeSubMenuPath(subMenu))
            .end();
    }

    public void addEntry(String title, String targetLink) throws IOException {
        assertOpen();

        currentBlock
            .startObject()
                .appendAttribute("id", targetLink)
                .appendAttribute("title", title)
            .end();
    }

    public void endMenu() throws IOException {
        assertOpen();
        try {
            currentBlock
                 .end()  //end "children" array
                 .end(); //end toplevel {} block
        } finally {
            try {
                fileWriter.close();
            } finally {
                fileWriter = null;
                closed = true;
                currentBlock = null;
            }
        }
    }

    /**
     * Convert the filename for the given submenu into a path relative to this menu.
     * @throws IOException if the conversion fails for any reason
     */
    private String computeSubMenuPath(XookiMenuGenerator subMenu) throws IOException {

        File basePath = this.path.getParentFile();
        File subPath = subMenu.path.getParentFile();

        String path;
        try {
            path = FileUtils.getRelativePath(basePath, subPath);
        } catch (Exception e) {
            //getRelativePath throws java.lang.Exception, for no clear reason, but we have to handle it.
            IOException ioe = new IOException("Error computing relative path for submenu " + subMenu.id);
            ioe.initCause(e);
            throw ioe;
        }

        if (path == null)
            throw new FileNotFoundException("Unable to compute relative path for submenu " + subMenu.id);

        return path;
    }

    private void assertOpen() throws IOException {
        if (closed) {
            throw new IllegalStateException("The menu at " + path.getAbsolutePath() + " has already been closed");
        }
        if (fileWriter == null) {
            throw new IllegalStateException("This menu has never been opened");
        }
    }

    private String toId(String title) {
        return title.replaceAll("\W+", "_");
    }

    /**
     * Represents a single block-level element in JSON, e.g. an Array or an Object.  A simple API is provided
     * to add nested blocks and attributes.
     */
    private class Block {

        private static final String FIRST_ENTRY_SEP = "
";
        private static final String NEXT_ENTRY_SEP = ",
";

        private Block parent; //points up the context stack
        private char close;   //character to write when this block is ended
        private String indent; //current indent level
        private String entrySeparator = FIRST_ENTRY_SEP; //separator string between entries of this block

        /** constructor for the root block */
        public Block() {
            this.parent = null;
            this.indent = "";
            this.entrySeparator = "";
        }

        /** constructor for a nested block */
        private Block(Block parent, char close) {
            this.parent = parent;
            this.indent = parent.indent + "\t";
            this.close = close;
        }

        /** add a JavaScript attribute to this block */
        public Block appendAttribute(String name, String value) throws IOException {
            nextEntry().append('\"').append(name).append("\":");
            return appendLiteral(value);
        }

        /** begin a nested array block with the given name  */
        public Block startArray(String name) throws IOException {
            nextEntry().append('\"').append(name).append("\": ");
            fileWriter.append('[');
            return new Block(this, ']');
        }

        /** begin a nested object block  */
        public Block startObject() throws IOException {
            nextEntry().append('{');
            return new Block(this, '}');
        }

        /** close the current block, returning a reference to the parent. */
        public Block end() throws IOException {
            if (parent == null) {
                throw new NoSuchElementException("Cannot pop the root element");
            }
            fileWriter.append('
');
            return parent.endBlock(close); //return reference to parent, popping the stack
        }

        /** end a child block using the given terminator */
        private Block endBlock(char close) throws IOException {
            fileWriter.append(indent).append(close);
            return this;
        }

        /**
         * start a new entry in the current block, including a separator from
         * any previous entries and indenting whitespace
         */
        private Writer nextEntry() throws IOException {
            fileWriter.append(entrySeparator).append(indent);
            entrySeparator = NEXT_ENTRY_SEP;
            return fileWriter;
        }

        /** append a quoted, escaped string literal to this block */
        private Block appendLiteral(String value) throws IOException {
            //escape any ' or " so that they don't screw up our syntax
            value = value.replaceAll("(['\"])", "\\$1");
            //enclose the value in quotes to include any whitespace
            fileWriter.append("\"").append(value).append("\"");
            return this;
        }

    }

}
