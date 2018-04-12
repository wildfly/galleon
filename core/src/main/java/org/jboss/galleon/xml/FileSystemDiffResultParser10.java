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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.galleon.diff.FileSystemDiffResult;
import org.jboss.galleon.util.ParsingUtils;
import org.jboss.galleon.xml.FileSystemDiffResultParser.Element;
import org.jboss.staxmapper.XMLExtendedStreamReader;

/**
 *
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class FileSystemDiffResultParser10 implements PlugableXmlParser<FileSystemDiffResult> {

    @Override
    public QName getRoot() {
        return FileSystemDiffResultParser.ROOT_1_0;
    }

    @Override
    public void readElement(XMLExtendedStreamReader reader, FileSystemDiffResult fileSystemDiffResult) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT: {
                    return;
                }
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case ADDED:
                            readAddedFiles(reader, fileSystemDiffResult);
                            break;
                        case DELETED:
                            readDeletedFiles(reader, fileSystemDiffResult);
                            break;
                        case MODIFIED:
                            readModifiedBinaryedFiles(reader, fileSystemDiffResult);
                            break;
                        case CHANGES:
                            readChanges(reader, fileSystemDiffResult);
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private void readAddedFiles(XMLExtendedStreamReader reader, FileSystemDiffResult fileSystemDiffResult) throws XMLStreamException {
         ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT:
                    return;
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PATH:
                            fileSystemDiffResult.getAddedFiles().add(new File(reader.getElementText()).toPath());
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private void readDeletedFiles(XMLExtendedStreamReader reader, FileSystemDiffResult fileSystemDiffResult) throws XMLStreamException {
         ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT:
                    return;
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PATH:
                            fileSystemDiffResult.getDeletedFiles().add(new File(reader.getElementText()).toPath());
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private void readModifiedBinaryedFiles(XMLExtendedStreamReader reader, FileSystemDiffResult fileSystemDiffResult) throws XMLStreamException {
         ParsingUtils.parseNoAttributes(reader);
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT:
                    return;
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case PATH:
                            fileSystemDiffResult.getModifiedBinaryFiles().add(new File(reader.getElementText()).toPath());
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

    private void readChanges(XMLExtendedStreamReader reader, FileSystemDiffResult fileSystemDiffResult) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.nextTag()) {
                case XMLStreamConstants.END_ELEMENT:
                    return;
                case XMLStreamConstants.START_ELEMENT: {
                    final Element element = Element.of(reader.getName());
                    switch (element) {
                        case CHANGE:
                            Path path = new File(reader.getAttributeValue(0)).toPath();
                            if(fileSystemDiffResult.getUnifiedDiffs().containsKey(path)) {
                                fileSystemDiffResult.getUnifiedDiffs().get(path).add(reader.getElementText());
                            } else {
                                List<String> diff = new ArrayList<>();
                                diff.add(reader.getElementText());
                                fileSystemDiffResult.getUnifiedDiffs().put(path, diff);
                            }
                            break;
                        default:
                            throw ParsingUtils.unexpectedContent(reader);
                    }
                    break;
                }
                default: {
                    throw ParsingUtils.unexpectedContent(reader);
                }
            }
        }
        throw ParsingUtils.endOfDocument(reader.getLocation());
    }

}
