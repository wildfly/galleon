/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.cli.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aesh.utils.Config;

/**
 *
 * @author jdenise@redhat.com
 */
public class Table {

    public static class Cell {

        private final List<String> items = new ArrayList<>();
        int length = 0;

        public Cell(String... content) {
            for (String c : content) {
                addLine(c);
            }
        }

        public void addLine(String line) {
            items.add(line);
            if (line.length() > length) {
                length = line.length();
            }
        }

        int length() {
            return length;
        }

        boolean isMultiLine() {
            return items.size() > 1;
        }

        String getFirstLine() {
            return items.get(0);
        }

        List<String> getMultiLines() {
            if (items.size() < 2) {
                return Collections.emptyList();
            }
            return items.subList(1, items.size());
        }
    }

    public enum SortType {
        ASCENDANT,
        DESCENDANT
    }
    private final List<String> headers = new ArrayList<>();
    private final List<List<Cell>> content = new ArrayList<>();
    private final Map<Integer, Integer> widths = new HashMap<>();

    public Table(List<String> headersList) {
        for (int i = 0; i < headersList.size(); i++) {
            int length = headersList.get(i).length();
            Integer w = widths.get(i);
            if (w == null) {
                widths.put(i, length);
            } else if (w.compareTo(length) < 0) {
                widths.put(i, length);
            }
            headers.add(headersList.get(i));
        }
    }

    public Table(String... headers) {
        this(Arrays.asList(headers));
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public void addLine(String... line) {
        List<Cell> cells = new ArrayList<>();
        for (String c : line) {
            cells.add(new Cell(c));
        }
        addCellsLine(cells);
    }

    public void addLine(List<String> line) {
        List<Cell> cells = new ArrayList<>();
        for (String c : line) {
            cells.add(new Cell(c));
        }
        addCellsLine(cells);
    }

    // Multi line cells
    public void addCellsLine(Cell... cell) {
        addCellsLine(Arrays.asList(cell));
    }

    public void addCellsLine(List<Cell> line) {
        for (int i = 0; i < line.size(); i++) {
            int length = line.get(i).length();
            Integer w = widths.get(i);
            if (w == null) {
                widths.put(i, length);
            } else if (w.compareTo(length) < 0) {
                widths.put(i, length);
            }
        }
        content.add(line);
    }

    public void sort(SortType type) {
        if (SortType.ASCENDANT.equals(type)) {
            Collections.sort(content, new Comparator<List<Cell>>() {
                @Override
                public int compare(List<Cell> o1, List<Cell> o2) {
                    return o1.get(0).getFirstLine().compareTo(o2.get(0).getFirstLine());
                }
            });
        } else {
            Collections.sort(content, new Comparator<List<Cell>>() {
                @Override
                public int compare(List<Cell> o1, List<Cell> o2) {
                    return o2.get(0).getFirstLine().compareTo(o1.get(0).getFirstLine());
                }
            });
        }
    }

    public String build() {
        return build(true);
    }

    public String build(boolean linesVisible) {
        StringBuilder builder = new StringBuilder();
        String line = null;
        String separator = " ";
        if (linesVisible) {
            StringBuilder lineBuilder = new StringBuilder();
            for (int i = 0; i < headers.size(); i++) {
                Integer w = widths.get(i);
                lineBuilder.append(line(w)).append(separator);
            }
            line = lineBuilder.toString();
            builder.append(line).append(Config.getLineSeparator());
        }
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            Integer w = widths.get(i);
            if (w > header.length()) {
                header = pad(header, w);
            }
            builder.append(header).append(separator);
        }
        if (line != null) {
            builder.append(Config.getLineSeparator()).append(line);
        }
        builder.append(Config.getLineSeparator());
        String tab = "";
        for (List<Cell> c : content) {
            for (int i = 0; i < c.size(); i++) {
                Cell val = c.get(i);
                Integer w = widths.get(i);
                // Print first line.
                String fl = val.getFirstLine();
                if (w > val.length()) {
                    fl = pad(fl, w);
                }
                builder.append(fl).append(separator);
                for (String l : val.getMultiLines()) {
                    builder.append(Config.getLineSeparator());
                    if (w > val.length()) {
                        l = pad(l, w);
                    }
                    builder.append(tab).append(l).append(separator);
                }
                tab = tab(tab.length() + w + 1);
            }
            tab = "";
            builder.append(Config.getLineSeparator());
        }
        return builder.toString();
    }

    private static String line(int width) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < width; i++) {
            builder.append("=");
        }
        return builder.toString();
    }

    private static String tab(int width) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < width; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }

    private static String pad(String s, int length) {
        StringBuilder builder = new StringBuilder();
        builder.append(s);
        for (int i = 0; i < length - s.length(); i++) {
            builder.append(" ");
        }
        return builder.toString();
    }

    public static class Node implements Comparable<Node> {

        private final List<Node> next = new ArrayList<>();
        private final String value;

        public Node(String value) {
            this.value = value;
        }

        public void addNext(Node next) {
            this.next.add(next);
        }

        String getValue() {
            return value;
        }

        @Override
        public int compareTo(Node o) {
            return value.compareTo(o.value);
        }

    }

    public static class Tree {

        StringBuilder builder;
        Map<Integer, Integer> columnsWidth = new HashMap<>();
        List<String> headers;
        private final List<Node> trees = new ArrayList<>();

        public Tree(String... headers) {
            this(Arrays.asList(headers));
        }

        public Tree(List<String> headers) {
            this.headers = headers;
        }

        public void add(Node line) {
            trees.add(line);
        }

        public void addAll(Collection<Node> nodes) {
            trees.addAll(nodes);
        }

        public String build() {
            builder = new StringBuilder();
            columnsWidth.clear();
            computeWidths(trees, 0);

            StringBuilder lineBuilder = new StringBuilder();
            for (int i = 0; i < headers.size(); i++) {
                int max = columnsWidth.get(i);
                lineBuilder.append(line(max)).append(" ");
            }
            String line = lineBuilder.toString();
            builder.append(line).append(Config.getLineSeparator());
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                Integer w = columnsWidth.get(i);
                header = pad(header, w);
                builder.append(header).append(" ");
            }
            if (line != null) {
                builder.append(Config.getLineSeparator()).append(line).
                        append(Config.getLineSeparator());
            }
            render(trees, 0, 0);
            return builder.append(Config.getLineSeparator()).toString();
        }

        void computeWidths(List<Node> trees, int column) {
            if (column >= headers.size()) {
                return;
            }
            int max = 0;
            List<Node> next = new ArrayList<>();
            for (Node n : trees) {
                if (n.getValue().length() > max) {
                    max = n.getValue().length();
                }
                next.addAll(n.next);
            }
            int hsize = headers.get(column).length();
            if (hsize > max) {
                max = hsize;
            }
            columnsWidth.put(column, max);
            computeWidths(next, column + 1);
        }

        void render(List<Node> trees, int offset, int column) {
            if (trees.isEmpty()) {
                return;
            }
            Collections.sort(trees);
            int max = columnsWidth.get(column);

            String tab = "";
            for (int i = 0; i < trees.size(); i++) {
                Node n = trees.get(i);
                String content = pad(n.getValue(), max);
                builder.append(tab).append(content).append(" ");
                render(n.next, offset + max + 1, column + 1);
                if (i < trees.size() - 1) {
                    builder.append(Config.getLineSeparator());
                }
                tab = tab(offset);
            }
        }
    }
}
