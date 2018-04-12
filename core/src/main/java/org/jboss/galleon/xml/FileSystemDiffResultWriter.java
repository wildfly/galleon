/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.xml;


import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.diff.FileSystemDiffResult;
import org.jboss.galleon.xml.FileSystemDiffResultParser.Element;
import org.jboss.galleon.xml.util.AttributeValue;
import org.jboss.galleon.xml.util.CDataNode;
import org.jboss.galleon.xml.util.ElementNode;
import org.jboss.galleon.xml.util.TextNode;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class FileSystemDiffResultWriter extends BaseXmlWriter<FileSystemDiffResult> {

    private static final FileSystemDiffResultWriter INSTANCE = new FileSystemDiffResultWriter();

    public static FileSystemDiffResultWriter getInstance() {
        return INSTANCE;
    }

    @Override
    protected ElementNode toElement(FileSystemDiffResult result) throws XMLStreamException {
        ElementNode root = addElement(null, Element.DIFF_RESULT);
        if(result.getAddedFiles() != null && !result.getAddedFiles().isEmpty()) {
            ElementNode addedFilesNode = addElement(root, Element.ADDED);
            for(Path addedFile : result.getAddedFiles()) {
                addElement(addedFilesNode, Element.PATH).addChild(new TextNode(addedFile.toString()));
            }
        }
        if(result.getDeletedFiles() != null && !result.getDeletedFiles().isEmpty()) {
            ElementNode deletedFilesNode = addElement(root, Element.DELETED);
            for(Path deletedFile : result.getDeletedFiles()) {
                addElement(deletedFilesNode, Element.PATH).addChild(new TextNode(deletedFile.toString()));
            }
        }
        if(result.getUnifiedDiffs()!= null && !result.getUnifiedDiffs().isEmpty()) {
            ElementNode changesNode = addElement(root, Element.CHANGES);
            for(Entry<Path, List<String>> change : result.getUnifiedDiffs().entrySet()) {
                ElementNode changeNode =  addElement(changesNode, Element.CHANGE);
                changeNode.addChild(addChangeContent(change.getValue()));
                changeNode.addAttribute("path", new AttributeValue(change.getKey().toString()));
            }
            ElementNode modifiedFilesNode = addElement(root, Element.MODIFIED);
            for(Path modifiedFile : result.getModifiedBinaryFiles()) {
                addElement(modifiedFilesNode, Element.PATH).addChild(new TextNode(modifiedFile.toString()));
            }
        }
        return root;
    }

    private CDataNode addChangeContent(List<String> diff ) {
        return new CDataNode(diff.stream().collect(Collectors.joining(System.lineSeparator(), System.lineSeparator(), System.lineSeparator())));
    }

}
