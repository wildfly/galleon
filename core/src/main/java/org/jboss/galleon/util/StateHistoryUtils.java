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

package org.jboss.galleon.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.jboss.galleon.Constants;
import org.jboss.galleon.Errors;
import org.jboss.galleon.MessageWriter;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.xml.ProvisioningXmlParser;

/**
 *
 * @author Alexey Loubyansky
 */
public class StateHistoryUtils {

    public static final int STATE_HISTORY_LIMIT = 5;

    public static void addNewUndoConfig(Path installDir, Path stagedDir, MessageWriter log) throws ProvisioningException {
        final Path installedConfig = PathsUtils.getProvisioningXml(installDir);
        if (!Files.exists(installedConfig)) {
            return;
        }
        final Path stagedHistoryDir = PathsUtils.getStateHistoryDir(stagedDir);
        mkdirs(stagedHistoryDir);
        final Path installedHistoryDir = PathsUtils.getStateHistoryDir(installDir);
        List<String> installedHistory = Collections.emptyList();
        if(Files.exists(installedHistoryDir)) {
            final Path installHistoryList = installedHistoryDir.resolve(Constants.HISTORY_LIST);
            if (Files.exists(installHistoryList)) {
                try {
                    installedHistory = Files.readAllLines(installHistoryList);
                } catch (IOException e) {
                    throw new ProvisioningException(Errors.readFile(installHistoryList), e);
                }
            }
        }
        final int historyLimit = installedHistory.isEmpty() ? STATE_HISTORY_LIMIT : Integer.parseInt(installedHistory.get(0));
        final String newStateId = UUID.randomUUID().toString();
        try(BufferedWriter writer = Files.newBufferedWriter(stagedHistoryDir.resolve(Constants.HISTORY_LIST))) {
            writer.write(String.valueOf(historyLimit));
            writer.newLine();
            if(!installedHistory.isEmpty()) {
                int offset = installedHistory.size() - historyLimit + 1;
                if (offset < 1) {
                    offset = 1;
                }
                int missingStates = 0;
                while (offset < installedHistory.size()) {
                    final String stateId = installedHistory.get(offset++);
                    final Path stateFile = installedHistoryDir.resolve(stateId);
                    if(!Files.exists(stateFile)) {
                        ++missingStates;
                        continue;
                    }
                    IoUtils.copy(stateFile, stagedHistoryDir.resolve(stateId));
                    writer.write(stateId);
                    writer.newLine();
                }
                if(missingStates > 0) {
                    log.error("The state history of the current installation is corrupted referencing " + missingStates + " missing states!");
                }
            }
            if(historyLimit > 0) {
                writer.write(newStateId);
            }
        } catch (IOException e) {
            throw new ProvisioningException(Errors.writeFile(stagedHistoryDir.resolve(Constants.HISTORY_LIST)), e);
        }
        try {
            IoUtils.copy(installedConfig, stagedHistoryDir.resolve(newStateId));
        } catch (IOException e) {
            throw new ProvisioningException(Errors.copyFile(installedConfig, stagedHistoryDir.resolve(newStateId)), e);
        }
    }

    public static void removeLastUndoConfig(Path installDir, Path stagedDir, MessageWriter log) throws ProvisioningException {
        final Path installedConfig = PathsUtils.getProvisioningXml(installDir);
        if (!Files.exists(installedConfig)) {
            return;
        }
        final Path installedHistoryDir = PathsUtils.getStateHistoryDir(installDir);
        List<String> installedHistory = Collections.emptyList();
        if(Files.exists(installedHistoryDir)) {
            final Path installHistoryList = installedHistoryDir.resolve(Constants.HISTORY_LIST);
            if (Files.exists(installHistoryList)) {
                try {
                    installedHistory = Files.readAllLines(installHistoryList);
                } catch (IOException e) {
                    throw new ProvisioningException(Errors.readFile(installHistoryList), e);
                }
            }
        }
        if(installedHistory.size() < 2) {
            return;
        }
        final Path stagedHistoryDir = PathsUtils.getStateHistoryDir(stagedDir);
        mkdirs(stagedHistoryDir);
        final int historyLimit = installedHistory.isEmpty() ? STATE_HISTORY_LIMIT : Integer.parseInt(installedHistory.get(0));
        try(BufferedWriter writer = Files.newBufferedWriter(stagedHistoryDir.resolve(Constants.HISTORY_LIST))) {
            writer.write(String.valueOf(historyLimit));
            writer.newLine();
            if(!installedHistory.isEmpty()) {
                int offset = installedHistory.size() - historyLimit - 1;
                if (offset < 1) {
                    offset = 1;
                }
                int missingStates = 0;
                while (offset < installedHistory.size() - 1) {
                    final String stateId = installedHistory.get(offset++);
                    final Path stateFile = installedHistoryDir.resolve(stateId);
                    if(!Files.exists(stateFile)) {
                        ++missingStates;
                        continue;
                    }
                    IoUtils.copy(stateFile, stagedHistoryDir.resolve(stateId));
                    writer.write(stateId);
                    writer.newLine();
                }
                if(missingStates > 0) {
                    log.error("The state history of the current installation is corrupted referencing " + missingStates + " missing states!");
                }
            }
        } catch (IOException e) {
            throw new ProvisioningException(Errors.writeFile(stagedHistoryDir.resolve(Constants.HISTORY_LIST)), e);
        }
    }

    private static void mkdirs(final Path stagedHistoryDir) throws ProvisioningException {
        try {
            Files.createDirectories(stagedHistoryDir);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.mkdirs(stagedHistoryDir), e);
        }
    }

    public static boolean isUndoAvailable(Path installDir, MessageWriter log) throws ProvisioningException {
        final Path installedHistoryDir = PathsUtils.getStateHistoryDir(installDir);
        if(!Files.exists(installedHistoryDir)) {
            return false;
        }
        final Path installedHistoryList = PathsUtils.getStateHistoryFile(installDir);
        if(!Files.exists(installedHistoryList)) {
            return false;
        }
        final List<String> installedHistory;
        try {
            installedHistory = Files.readAllLines(installedHistoryList);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.readFile(installedHistoryList), e);
        }
        if(installedHistory.size() < 2) {
            return false;
        }
        int i = installedHistory.size() - 1;
        do {
            if (Files.exists(installedHistoryDir.resolve(installedHistory.get(i--)))) {
                return true;
            }
            log.error("The state history of the current installation is corrupted referencing missing states!");
        } while (i >= 1);
        return false;
    }

    public static ProvisioningConfig readUndoConfig(Path installDir, MessageWriter log) throws ProvisioningException {
        final Path installedHistoryDir = PathsUtils.getStateHistoryDir(installDir);
        if(!Files.exists(installedHistoryDir)) {
            throw new ProvisioningException(Errors.historyIsEmpty());
        }
        final Path installedHistoryList = installedHistoryDir.resolve(Constants.HISTORY_LIST);
        if(!Files.exists(installedHistoryList)) {
            throw new ProvisioningException(Errors.historyIsEmpty());
        }
        final List<String> installedHistory;
        try {
            installedHistory = Files.readAllLines(installedHistoryList);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.readFile(installedHistoryList), e);
        }
        if(installedHistory.size() < 2) {
            throw new ProvisioningException(Errors.historyIsEmpty());
        }
        int i = installedHistory.size() - 1;
        do {
            final Path statePath = installedHistoryDir.resolve(installedHistory.get(i--));
            if (Files.exists(statePath)) {
                return ProvisioningXmlParser.parse(statePath);
            }
            log.error("The state history of the current installation is corrupted referencing missing states!");
        } while (i >= 1);
        throw new ProvisioningException(Errors.historyIsEmpty());
    }

    public static int readStateHistoryLimit(Path installDir, MessageWriter log) throws ProvisioningException {
        final Path installedHistoryList = PathsUtils.getStateHistoryFile(installDir);
        if(!Files.exists(installedHistoryList)) {
            return STATE_HISTORY_LIMIT;
        }
        try(BufferedReader reader = Files.newBufferedReader(installedHistoryList)) {
            final String line = reader.readLine();
            if(line != null) {
                return Integer.parseInt(line);
            }
        } catch (IOException e) {
            throw new ProvisioningException(Errors.readFile(installedHistoryList), e);
        }
        return STATE_HISTORY_LIMIT;
    }

    public static int writeStateHistoryLimit(Path installDir, int limit, MessageWriter log) throws ProvisioningException {
        if(limit < 0) {
            throw new ProvisioningException("State history limit can not be a negative value: " + limit);
        }
        final Path installedHistoryDir = PathsUtils.getStateHistoryDir(installDir);
        if (!Files.exists(installedHistoryDir)) {
            mkdirs(installedHistoryDir);
        }
        final Path installedHistoryList = installedHistoryDir.resolve(Constants.HISTORY_LIST);
        List<String> installedHistory = Collections.emptyList();
        if (Files.exists(installedHistoryList)) {
            try {
                installedHistory = Files.readAllLines(installedHistoryList);
            } catch (IOException e) {
                throw new ProvisioningException(Errors.readFile(installedHistoryList), e);
            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(installedHistoryList)) {
            writer.write(String.valueOf(limit));
            writer.newLine();
            int offset = installedHistory.size() - limit;
            if (offset < 1) {
                offset = 1;
            }
            int missingStates = 0;
            while (offset < installedHistory.size()) {
                final String stateId = installedHistory.get(offset++);
                final Path stateFile = installedHistoryDir.resolve(stateId);
                if (!Files.exists(stateFile)) {
                    ++missingStates;
                    continue;
                }
                writer.write(stateId);
                writer.newLine();
            }
            if (missingStates > 0) {
                log.error("The state history of the current installation is corrupted referencing " + missingStates
                        + " missing states!");
            }
        } catch (IOException e) {
            throw new ProvisioningException(Errors.readFile(installedHistoryList), e);
        }
        return STATE_HISTORY_LIMIT;
    }

    public static void clearStateHistory(Path installDir, MessageWriter log) throws ProvisioningException {
        final Path installedHistoryDir = PathsUtils.getStateHistoryDir(installDir);
        if (!Files.exists(installedHistoryDir)) {
            return;
        }
        int limit = readStateHistoryLimit(installDir, log);
        final Path installedHistoryList = installedHistoryDir.resolve(Constants.HISTORY_LIST);
        try (BufferedWriter writer = Files.newBufferedWriter(installedHistoryList)) {
            writer.write(String.valueOf(limit));
            writer.newLine();
        } catch (IOException e) {
            throw new ProvisioningException(Errors.writeFile(installedHistoryList), e);
        }
        deleteHistoryFiles(installedHistoryDir);
    }

    private static void deleteHistoryFiles(Path installedHistoryDir) throws ProvisioningException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(installedHistoryDir)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry) && !entry.getFileName().toString().
                        equals(Constants.HISTORY_LIST)) {
                    try {
                        Files.delete(entry);
                    } catch (IOException ex) {
                        throw new ProvisioningException(Errors.deleteFile(entry), ex);
                    }
                }
            }
        } catch (IOException ex) {
            throw new ProvisioningException(Errors.readDirectory(installedHistoryDir), ex);
        }
    }
}
