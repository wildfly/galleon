/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.path;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jdenise@redhat.com
 */
public class PathParser {

    public static class Node {

        private final String orig;
        private final String name;

        Node(String orig, String name) {
            this.orig = orig;
            this.name = name;
        }

        public String getOrig() {
            return orig;
        }

        /**
         * @return the id
         */
        public String getName() {
            return name;
        }
    }

    public interface PathConsumer {

        void enterNode(Node node) throws PathConsumerException;

        void enterRoot() throws PathConsumerException;

        boolean expectEndOfNode();
    }

    enum State {
        NOT_STARTED,
        ROOT,
        CONTENT,
    }

    enum MultiState {
        FIRST,
        NEXT
    }

    public static final char PATH_SEPARATOR = '/';
    public static final char ORIGIN_SEPARATOR = '#';

    public static void parse(String path, PathConsumer consumer) throws PathParserException, PathConsumerException {
        State state = State.NOT_STARTED;
        char[] arr = path.toCharArray();
        int offset = 0;
        while (offset < arr.length) {
            char c = arr[offset];
            switch (state) {
                case NOT_STARTED: {
                    if (c == PATH_SEPARATOR) {
                        state = State.CONTENT;
                        consumer.enterRoot();
                    } else {
                        throw new PathParserException("Invalid syntax, path must start by " + PATH_SEPARATOR);
                    }
                    offset += 1;
                    break;
                }
                case CONTENT: {
                    List<String> content = new ArrayList<>();
                    int i = parseMultiple(arr, offset, content, ORIGIN_SEPARATOR, PATH_SEPARATOR, consumer.expectEndOfNode());
                    offset += i;
                    state = State.CONTENT;
                    Node n = null;
                    if (content.size() == 1) {
                        n = new Node(null, content.get(0));
                    } else if (content.size() == 2) {
                        n = new Node(content.get(0), content.get(1));
                    } else {
                        if (!consumer.expectEndOfNode()) {
                            throw new PathParserException("Invalid node " + content);
                        }
                    }
                    consumer.enterNode(n);
                    break;
                }
            }
        }
    }

    // Parse a String ending with separator
    // The separator can be escaped with \.
    private static int parsePathNode(char[] arr, int offset, List<String> content, char separator) {
        if (offset == arr.length - 1) {
            return -1;
        }
        StringBuilder builder = new StringBuilder();
        boolean escaped = false;
        int orig = offset;
        while (offset < arr.length) {
            char c = arr[offset];
            offset += 1;
            if (c == '\\') {
                if (escaped) {
                    escaped = false;
                    builder.append("\\");
                    builder.append(c);
                } else {
                    escaped = true;
                }
                continue;
            }
            if (c == separator) {
                if (escaped) {
                    builder.append(c);
                } else {
                    // we are done.
                    content.add(builder.toString());
                    break;
                }
            } else {
                builder.append(c);
            }
        }
        if (content.isEmpty()) {
            content.add(builder.toString());
        }
        return offset - orig;
    }

    private static int parseMultiple(char[] arr, int offset, List<String> content, final char separator1, char terminalSeparator, boolean expectEndOfPath) {
        MultiState state = MultiState.FIRST;
        int orig = offset;
        StringBuilder builder = new StringBuilder();
        boolean escaped = false;
        boolean done = false;
        while (offset < arr.length && !done) {
            char c = arr[offset];
            offset += 1;
            if (c == '\\') {
                if (escaped) {
                    escaped = false;
                    builder.append("\\");
                    builder.append(c);
                } else {
                    escaped = true;
                }
                continue;
            }
            switch (state) {
                case FIRST: {
                    if (c == separator1) {
                        if (escaped) {
                            builder.append(c);
                        } else {
                            // End of first element
                            content.add(builder.toString());
                            builder = new StringBuilder();
                            state = MultiState.NEXT;
                        }
                    } else if (c == terminalSeparator) {
                        if (escaped) {
                            builder.append(c);
                        } else {
                            // End of feature group
                            content.add(builder.toString());
                            done = true;
                        }
                    } else {
                        builder.append(c);
                    }
                    break;
                }
                case NEXT: {
                    if (c == terminalSeparator) {
                        if (escaped) {
                            builder.append(c);
                        } else {
                            // End of feature spec
                            content.add(builder.toString());
                            done = true;
                            break;
                        }
                        break;
                    } else {
                        builder.append(c);
                    }
                    break;
                }
            }
        }
        if (!expectEndOfPath) {
        if (!done) {
            content.add(builder.toString());
            }
        }
        return offset - orig;
    }
}
