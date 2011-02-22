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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

/**
 * <p>
 * This task is used to modify existing jnlp file.
 * It modifies only the resources part of the file and nothing else. 
 * (useful to make the dependencies inside).
 * If a resources part already exists the program add just new jar entries, don't remove them.  
 * </p>
 * <p>
 * There are three ways for resolving path with this task:
 * </p>
 * <ul>
 *  <li> Automatic path resolution (default)</li>
 *  <li> Flat path resolution</li>
 *  <li> Relative path resolution</li>
 * </ul>
 * <p>
 *  <b>The automatic path resolution</b> generates uri's corresponding to the fileset paths.<br/>
 *  <b>The flat path resolution</b> permits to specify in hard the prefix for the path, it is useful if you want deploy the jnlp on a distant server.
 *  <p>
 *      If the flat path value is http://myserver.com
 *      The Task will modify the jnlp file like that:
 *      {@literal  
 *          <resources>
 *              <jar href="http://myserver.com/myDependecy1.jar"/>
 *              <jar href="http://myserver.com/myDependecy2.jar"/>
 *          </resources>
 *      } 
 *  </p>
 *  <br/>
 *  <b>The relative path resolution</b> generates path in relative corresponding to the fileset paths.<br/>
 * </p>
 * 
 * @version 1.0
 * 
 */
public class JNLPTask extends Task {

    private Vector<FileSet> filesets = new Vector<FileSet>();

    private static final String JAR_ENTITY = "jar";
    private static final String RESOURCES_ENTITY = "resources";
    private static final String DOM_IMPLEMENTATION = "LS";
    private static final String PRETTY_PRINT_FORMAT = "format-pretty-print";
    private static final String HREF_ATTRIBUTE = "href";
    private static final String DOWNLOAD_ATTRIBUTE = "download";
    private static final String MAIN_ATTRIBUTE = "main";

    private String mainjar = null;
    private String jnlpFile = null;
    private String flatPath = null;
    private final String tmpFile = "tmp.xml";
    private PathType pathType = PathType.AUTOMATIC;



    @Override
    public synchronized void execute() throws BuildException {
        validate();
        log("JNLP modification...");
        try {
            DOMImplementationRegistry domRegistry = DOMImplementationRegistry
                    .newInstance();
            DOMImplementationLS domImpl = (DOMImplementationLS) domRegistry
                    .getDOMImplementation(DOM_IMPLEMENTATION);

            LSParser jnlpBuilder = domImpl.createLSParser(
                    DOMImplementationLS.MODE_SYNCHRONOUS, null);
            Document jnlpDoc = jnlpBuilder.parseURI(jnlpFile);

            Node root = null;
            for (int i = 0; i < jnlpDoc.getChildNodes().getLength(); i++) {
                if (jnlpDoc.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                    root = jnlpDoc.getChildNodes().item(i);
                    break;
                }
            }
            NodeList nodeList = root.getChildNodes();

            if (nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    if (RESOURCES_ENTITY.equals(nodeList.item(i).getNodeName())) {
                        root.removeChild(nodeList.item(i));
                    }
                }
            }
            Element resourcesElement = jnlpDoc.createElement(RESOURCES_ENTITY);
            root.appendChild(resourcesElement);

            appendAllResources(jnlpDoc, resourcesElement);
            LSOutput lsOutput = domImpl.createLSOutput();
            lsOutput.setByteStream(new FileOutputStream(new File(tmpFile)));
            LSSerializer serializer = domImpl.createLSSerializer();
            serializer.getDomConfig().setParameter(PRETTY_PRINT_FORMAT, true);
            serializer.write(jnlpDoc, lsOutput);
            replaceFile(new File(tmpFile), new File(jnlpFile));
            log("JNLP modification done !");
        } catch (IOException e) {
            throw new BuildException(e.getMessage());
        } catch (ClassCastException e) {
            throw new BuildException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new BuildException(e.getMessage());
        } catch (InstantiationException e) {
            throw new BuildException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new BuildException(e.getMessage());
        } catch (DOMException e) {
            throw new BuildException(e.getMessage());
        } catch (Exception e) {
            throw new BuildException(e.getMessage());
        }

    }

    private void validate() throws BuildException {

        // less one resources has to be setted.
        if (null == this.filesets || this.filesets.isEmpty()) {
            throw new BuildException("the parameter filesets can not be empty");
        }

        // mainjar can't be empty or null
        if (null == mainjar || "".equals(mainjar)) {
            throw new BuildException("the parameter mainjar have to be setted");
        }

        // jnlp can't null or not exist
        if (null == jnlpFile) {
            throw new BuildException("the parameter jnlpFile have to be setted");
        }

        if (!(new File(jnlpFile).exists())) {
            throw new BuildException("the jnlpFile " + jnlpFile
                    + " doesn't exist");
        }

        if (flatPath != null) {
            pathType = PathType.FLAT;
        } else {
            pathType = PathType.AUTOMATIC;
        }
        
        // check if the main jar parameter is included in the resources and if
        // the resources exists.
        for (FileSet currentResource : filesets) {

            for (File currentFile : currentResource.getDir().listFiles()) {

                if (checkDirectories(currentResource, currentFile)) {
                    if (!currentFile.exists()) {
                        throw new BuildException("the resource: "
                                + currentFile.getAbsolutePath()
                                + " doesnot exists !");
                    }

                }
            }
        }
        if (!new File(mainjar).exists()) {
            throw new BuildException("the mainJar does not exists");
        }

    }

    private void replaceFile(File src, File dest) throws IOException {
        FileUtils.delete(dest);
        FileUtils.getFileUtils().copyFile(src, dest);
    }

    
    private void appendElement(File currentFile, Document document,
            Element rootElement, boolean main) throws DOMException, Exception {
        Element currentElement = document.createElement(JAR_ENTITY);

        currentElement.setAttribute(HREF_ATTRIBUTE, computePath(currentFile,
                pathType));
         // TODO: the download attribute should be parameterizable => lazy or eager, from the moment it's only "eager".
        currentElement.setAttribute(DOWNLOAD_ATTRIBUTE, "eager");

        if (main) {
            currentElement.setAttribute(MAIN_ATTRIBUTE, "true");
        } else {
            currentElement.setAttribute(MAIN_ATTRIBUTE, "false");
        }

        rootElement.appendChild(currentElement);
    }

    private void appendAllResources(Document jnlpDoc, Element element)
            throws DOMException, Exception {

        appendElement(new File(mainjar), jnlpDoc, element, true);

        for (FileSet currentResource : filesets) {

            for (File currentFile : currentResource.getDir().listFiles()) {
                if (checkDirectories(currentResource, currentFile)) {
                    appendElement(currentFile, jnlpDoc, element, false);
                }
            }

        }
    }

    private String computePath(File currentFile, PathType type)
            throws Exception {
        switch (type) {
        case FLAT:
            return computeFlat(currentFile);
        default:
            return computeAutomatic(currentFile);
        }
    }



    private String computeFlat(File currentFile) {

        String finalPath = null;
        
        int separatorPos = flatPath.lastIndexOf(File.separator);
        
        if (separatorPos == flatPath.length() - 1) {
            finalPath = flatPath+currentFile.getName();
        } else {
            finalPath = flatPath+File.separator + currentFile.getName();
        }
        
        
        
        return finalPath;
    }

    private String computeAutomatic(File currentFile) {
        return currentFile.toURI().toString();
    }

    private boolean checkDirectories(FileSet fileset, File file) {
        for (String fileName : fileset.getDirectoryScanner().getIncludedFiles()) {
            if (file.getName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the jar resources to include inside the jnlp.
     * 
     * @param fileset
     *            the resources to include inside the jnlp.
     */
    public void addConfiguredFileset(FileSet fileset) {
        filesets.add(fileset);

    }

    /**
     * Set the main jar file (that contain the main class to load).
     * 
     * @param mainjar
     *            the main jar file (that contain the main class to load).
     */
    public void setMainJar(String mainjar) {
        this.mainjar = mainjar;
    }

    /**
     * Set the jnlp file to modify.
     * 
     * @param jnlpFile
     *            the jnlp file to modify.
     */
    public void setJnlpFile(String jnlpFile) {
        this.jnlpFile = jnlpFile;
    }

    /**
     * Force the resources path in the jnlp file.
     * 
     * @param flatPath
     *            the prefix path to use.
     */
    public void setFlatPathResources(String flatPath) {
        this.flatPath = flatPath;
    }
    
    private enum PathType {
        FLAT, AUTOMATIC;
    }
}
