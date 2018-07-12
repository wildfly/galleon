/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
    public enum SortType {
        ASCENDANT,
        DESCENDANT
    }
    private final List<String> headers = new ArrayList<>();
    private final List<List<String>> content = new ArrayList<>();
    private final Map<Integer, Integer> widths = new HashMap<>();
    public Table(String... header) {
        for (int i = 0; i < header.length; i++) {
            int length = header[i].length();
            Integer w = widths.get(i);
            if (w == null) {
                widths.put(i, length);
            } else if (w.compareTo(length) < 0) {
                widths.put(i, length);
            }
            headers.add(header[i]);
        }
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public void addLine(String... line) {
        addLine(Arrays.asList(line));
    }

    public void addLine(List<String> line) {
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
            Collections.sort(content, new Comparator<List<String>>() {
                @Override
                public int compare(List<String> o1, List<String> o2) {
                    return o1.get(0).compareTo(o2.get(0));
                }
            });
        } else {
            Collections.sort(content, new Comparator<List<String>>() {
                @Override
                public int compare(List<String> o1, List<String> o2) {
                    return o2.get(0).compareTo(o1.get(0));
                }
            });
        }
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            Integer w = widths.get(i);
            if (w != null) {
                if (w > header.length()) {
                    header = pad(header, w);
                }
            }
            builder.append(" ").append(header);
        }
        builder.append(Config.getLineSeparator());
        for (List<String> c : content) {
            for (int i = 0; i < c.size(); i++) {
                String val = c.get(i);
                Integer w = widths.get(i);
                if (w != null) {
                    if (w > val.length()) {
                        val = pad(val, w);
                    }
                }
                builder.append(" ").append(val);
            }
            builder.append(Config.getLineSeparator());
        }
        return builder.toString();
    }

    private String pad(String s, int length) {
        StringBuilder builder = new StringBuilder();
        builder.append(s);
        for (int i = 0; i < length - s.length(); i++) {
            builder.append(" ");
        }
        return builder.toString();
    }
}
