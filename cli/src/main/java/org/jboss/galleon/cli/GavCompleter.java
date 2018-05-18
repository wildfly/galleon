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
package org.jboss.galleon.cli;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.aesh.command.completer.CompleterInvocation;
import org.aesh.command.completer.OptionCompleter;

/**
 *
 * @author Alexey Loubyansky
 */
public class GavCompleter implements OptionCompleter<PmCompleterInvocation> {

    @Override
    public void complete(PmCompleterInvocation ci) {
        Path path = ci.getPmSession().getPmConfiguration().
                getMavenConfig().getLocalRepository();
        if(!Files.isDirectory(path)) {
            return;
        }
        try {
            doComplete(ci, path);
        } catch (IOException e) {
            return;
        }
    }

    private void doComplete(CompleterInvocation ci, Path repoHome) throws IOException {
        final List<String> candidates = new ArrayList<>();
        final String currentValue = ci.getGivenCompleteValue();
        final int groupSeparator = currentValue.indexOf(':');
        if(groupSeparator > 0) {
            Path currentDir = repoHome;
            final String[] parts = currentValue.substring(0, groupSeparator).split("\\.");
            for(String part : parts) {
                currentDir = currentDir.resolve(part);
            }
            if(groupSeparator == currentValue.length() - 1) {
                try(DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)) {
                    for(Path p : stream) {
                        if(isArtifactDir(p)) {
                            candidates.add(currentValue + p.getFileName());
                        }
                    }
                }
                ci.setAppendSpace(false);
            } else {
                final int artifactSeparator = currentValue.indexOf(':', groupSeparator + 1);
                if(artifactSeparator > 0) {
                    currentDir = currentDir.resolve(currentValue.substring(groupSeparator + 1, artifactSeparator));
                    if(artifactSeparator == currentValue.length() - 1) {
                        try(DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)) {
                            for(Path p : stream) {
                                if(isVersionDir(p)) {
                                    candidates.add(currentValue + p.getFileName());
                                }
                            }
                        }
                    } else {
                        final String chunk = currentValue.substring(artifactSeparator + 1);
                        final String prefix = currentValue.substring(0, artifactSeparator + 1);
                        try(DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)) {
                            for(Path p : stream) {
                                if (isVersionDir(p)) {
                                    final String dirName = p.getFileName().toString();
                                    if (dirName.startsWith(chunk) && dirName.length() != chunk.length()) {
                                        candidates.add(prefix + p.getFileName());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    final String chunk = currentValue.substring(groupSeparator + 1);
                    if(Files.exists(currentDir.resolve(chunk))) {
                        if(isArtifactDir(currentDir.resolve(chunk))) {
                            candidates.add(currentValue + ":");
                        }
                    }
                    final String prefix = currentValue.substring(0, groupSeparator + 1);
                    try(DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)) {
                        for(Path p : stream) {
                            if (isArtifactDir(p)) {
                                final String dirName = p.getFileName().toString();
                                if (dirName.startsWith(chunk) && dirName.length() != chunk.length()) {
                                    candidates.add(prefix + p.getFileName());
                                }
                            }
                        }
                    }
                    ci.setAppendSpace(false);
                }
            }
        } else {
            completeGroup(currentValue, candidates, repoHome);
            ci.setAppendSpace(false);
        }

        ci.addAllCompleterValues(candidates);
    }

    private void completeGroup(String currentValue, List<String> candidates,
            Path repoHome) throws IOException {

        Path groupDir = repoHome;
        final String chunk;
        final String prefix;
        if(currentValue.isEmpty()) {
            chunk = "";
            prefix = "";
        } else {
            final String[] parts = currentValue.split("\\.");
            if(currentValue.charAt(currentValue.length() - 1) == '.') {
                chunk = "";
                prefix = currentValue;
                for(String part : parts) {
                    groupDir = groupDir.resolve(part);
                }
            } else {
                if (parts.length == 1) {
                    chunk = parts[0];
                    prefix = "";
                } else {
                    int i = 0;
                    while (i < parts.length - 1) {
                        groupDir = groupDir.resolve(parts[i++]);
                    }
                    chunk = parts[parts.length - 1];
                    prefix = currentValue.substring(0, currentValue.length() - chunk.length());
                }
                trySeparators(groupDir.resolve(chunk), currentValue, candidates);
            }
        }

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(groupDir)) {
            for(Path p : stream) {
                if (isGroupDir(p)) {
                    final String dirName = p.getFileName().toString();
                    if (chunk.isEmpty() || dirName.startsWith(chunk) && dirName.length() != chunk.length()) {
                        candidates.add(prefix + dirName);
                    }
                }
            }
        }
    }

    private void trySeparators(Path dir, String prefix, List<String> candidates) throws IOException {
        if(!Files.isDirectory(dir)) {
            return;
        }
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            boolean group = false;
            boolean artifact = false;
            for(Path p : stream) {
                if(!artifact && isArtifactDir(p)) {
                    candidates.add(prefix + ":");
                    if(group) {
                        return;
                    } else {
                        artifact = true;
                    }
                } else if(!group && isGroupDir(p)) {
                    candidates.add(prefix + ".");
                    if(artifact) {
                        return;
                    } else {
                        group = true;
                    }
                }
            }
        }
    }

    private boolean isGroupDir(Path dir) throws IOException {
        if(!Files.isDirectory(dir)) {
            return false;
        }
        if(isVersionDir(dir) || isArtifactDir(dir)) {
            return false;
        }
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for(Path p : stream) {
                return !isVersionDir(p);
            }
        }
        return false;
    }

    private boolean isArtifactDir(Path dir) throws IOException {
        if(!Files.isDirectory(dir)) {
            return false;
        }
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for(Path p : stream) {
                if(isVersionDir(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isVersionDir(Path dir) throws IOException {
        if(!Files.isDirectory(dir)) {
            return false;
        }
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for(Path p : stream) {
                if(p.getFileName().toString().endsWith(".zip")) {
                    return true;
                }
            }
        }
        return false;
    }
}
