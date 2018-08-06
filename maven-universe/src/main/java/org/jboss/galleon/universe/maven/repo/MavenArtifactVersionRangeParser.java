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

package org.jboss.galleon.universe.maven.repo;

import java.util.Collections;
import java.util.List;

import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.Version;
import org.jboss.galleon.universe.maven.MavenUniverseException;
import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class MavenArtifactVersionRangeParser {

    private static class Bound {

        private final MavenArtifactVersion v;
        private final boolean included;

        Bound(MavenArtifactVersion v, boolean included) {
            this.v = v;
            this.included = included;
        }

        boolean fallsLower(MavenArtifactVersion version) {
            if(v == null) {
                return false;
            }
            final int i = v.compareTo(version);
            if(i == 0) {
                return !included;
            }
            return i > 0;
        }

        boolean fallsHigher(MavenArtifactVersion version) {
            if(v == null) {
                return false;
            }
            final int i = v.compareTo(version);
            if(i == 0) {
                return !included;
            }
            return i < 0;
        }
    }

    private static class SingleVersionRange implements MavenArtifactVersionRange {

        private final MavenArtifactVersion version;
        private final boolean included;

        SingleVersionRange(MavenArtifactVersion version, boolean included) {
            this.version = version;
            this.included = included;
        }

        @Override
        public boolean includesVersion(MavenArtifactVersion version) {
            return !(this.version.compareTo(version) == 0 ^ included);
        }

        @Override
        public String toString() {
            if(included) {
                return '[' + version.toString() + ']';
            }
            return '(' + version.toString() + ')';
        }
    }

    private static class SimpleVersionRange implements MavenArtifactVersionRange {

        private final Bound left;
        private final Bound right;

        SimpleVersionRange(Bound left, Bound right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean includesVersion(MavenArtifactVersion version) {
            return !(left.fallsLower(version) || right.fallsHigher(version));
        }

        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder();
            if(left.included) {
                buf.append('[');
            } else {
                buf.append('(');
            }
            if(left.v != null) {
                buf.append(left.v);
            }
            buf.append(',');
            if(right.v != null) {
                buf.append(right.v);
            }
            if(right.included) {
                buf.append(']');
            } else {
                buf.append(')');
            }
            return buf.toString();
        }
    }

    private static class RangeCollection implements MavenArtifactVersionRange {

        private final List<MavenArtifactVersionRange> ranges;

        private RangeCollection(List<MavenArtifactVersionRange> ranges) {
            this.ranges = ranges;
        }

        @Override
        public boolean includesVersion(MavenArtifactVersion version) {
            for(MavenArtifactVersionRange range : ranges) {
                if(range.includesVersion(version)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder();
            StringUtils.append(buf, ranges);
            return buf.toString();
        }
    }

    private int i;

    public MavenArtifactVersionRange parseRange(String rangeStr) throws MavenUniverseException {
        i = 0;
        List<MavenArtifactVersionRange> ranges = null;
        while(i < rangeStr.length()) {
            final char ch = rangeStr.charAt(i++);
            switch(ch) {
                case '(':
                case '[': {
                    final MavenArtifactVersionRange range = parsedRangeStart(rangeStr, ch);
                    if(ranges != null) {
                        ranges = CollectionUtils.add(ranges, range);
                    } else if(i < rangeStr.length()) {
                        ranges = CollectionUtils.add(Collections.emptyList(), range);
                    } else {
                        return range;
                    }
                    break;
                }
                case ',':
                    continue;
                case ')':
                case ']':
                default:
                    return new SingleVersionRange(new MavenArtifactVersion(rangeStr), true);
            }
        }
        if(ranges == null) {
            throw new MavenUniverseException("Could not determine parse any version range in '" + rangeStr + "'");
        }
        return new RangeCollection(ranges);
    }

    private MavenArtifactVersionRange parsedRangeStart(String rangeStr, char trigger) throws MavenUniverseException {
        final int startIndex = i;
        while(i < rangeStr.length()) {
            char ch = rangeStr.charAt(i++);
            switch(ch) {
                case ',': {
                    final MavenArtifactVersion boundVersion = i == startIndex + 1 ? null : new MavenArtifactVersion(rangeStr.substring(startIndex, i - 1));
                    return new SimpleVersionRange(new Bound(boundVersion, trigger != '('), parseBound(rangeStr));
                }
                case ')': {
                    if(trigger != '(') {
                        throw boundNotClosed(rangeStr, trigger, startIndex - 1, ')');
                    }
                    final MavenArtifactVersion boundVersion = i == startIndex + 1 ? null : new MavenArtifactVersion(rangeStr.substring(startIndex, i - 1));
                    return new SingleVersionRange(boundVersion, false);
                }
                case ']':
                    if(trigger != '[') {
                        throw boundNotClosed(rangeStr, trigger, startIndex - 1, ']');
                    }
                    final MavenArtifactVersion boundVersion = i == startIndex + 1 ? null : new MavenArtifactVersion(rangeStr.substring(startIndex, i - 1));
                    return new SingleVersionRange(boundVersion, true);
                case '(':
                case '[':
                    throw unexpectedChar(rangeStr, ch, i - 1);
                default:
            }
        }
        throw rangeNotComplete(rangeStr, startIndex);
    }

    private Bound parseBound(String rangeStr) throws MavenUniverseException {
        final int startIndex = i;
        while(i < rangeStr.length()) {
            final char ch = rangeStr.charAt(i++);
            switch(ch) {
                case ')': {
                    final MavenArtifactVersion boundVersion = i == startIndex + 1 ? null : new MavenArtifactVersion(rangeStr.substring(startIndex, i - 1));
                    return new Bound(boundVersion, false);
                }
                case ']': {
                    final MavenArtifactVersion boundVersion = i == startIndex + 1 ? null : new MavenArtifactVersion(rangeStr.substring(startIndex, i - 1));
                    return new Bound(boundVersion, true);
                }
                case ',':
                case '(':
                case '[':
                    throw unexpectedChar(rangeStr, ch, i - 1);
                default:
            }
        }
        throw boundNotClosed(rangeStr, startIndex);
    }

    private MavenUniverseException rangeNotComplete(String rangeStr, final int startIndex) {
        return new MavenUniverseException("Version range started at index " + startIndex + " in " + rangeStr + " is not complete");
    }

    private static MavenUniverseException unexpectedChar(String rangeStr, final char ch, int index) {
        return new MavenUniverseException("Unexpected character " + ch + " at index " + index + " in " + rangeStr);
    }

    private MavenUniverseException boundNotClosed(String rangeStr, char trigger, final int startIndex, char ending) {
        return new MavenUniverseException("The bound started by '" + trigger + "' at index " + startIndex + " in " + rangeStr + " is closed with '" + ending + "'");
    }

    private static MavenUniverseException boundNotClosed(String rangeStr, final int startIndex) {
        return new MavenUniverseException("Bound start at index " + startIndex + " in " + rangeStr + " is missing closing ')' or ']'");
    }

    public static void main(String[] args) throws Exception {

        GenericVersionScheme scheme = new GenericVersionScheme();
        //ComparableVersion v = new ComparableVersion("1.0.0.Alpha-SNAPSHOT");
        //ComparableVersion v2 = new ComparableVersion("1.0-alpha");

        Version v = scheme.parseVersion("1.0.0.Deta3");
        Version v2 = scheme.parseVersion("1.0.0.Beta2");
        System.out.println(v2.compareTo(v));

        MavenArtifactVersion v1 = new MavenArtifactVersion("1.0.0.Alpha1-SNAPSHOT");

        final MavenArtifactVersionRangeParser parser = new MavenArtifactVersionRangeParser();
        MavenArtifactVersionRange range = parser.parseRange("[1.0-alpha,2.0-alpha)");
        System.out.println(range);
        System.out.println(range.includesVersion(v1));
    }
}
