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

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.aesh.command.impl.converter.FileConverter;
import org.aesh.readline.AeshContext;
import org.aesh.readline.util.Parser;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;


/**
 *
 * @author Alexey Loubyansky
 */
public class Util {

    static InputStream getResourceStream(String resource) throws CommandExecutionException {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final InputStream pomIs = cl.getResourceAsStream(resource);
        if(pomIs == null) {
            throw new CommandExecutionException(resource + " not found");
        }
        return pomIs;
    }

    public static RepositorySystemSession newRepositorySession(final RepositorySystem repoSystem,
            Path path, RepositoryListener listener, ProxySelector proxySelector, boolean offline) {
        final DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setRepositoryListener(listener);
        session.setOffline(offline);
        final LocalRepository localRepo = new LocalRepository(path.toString());
        session.setLocalRepositoryManager(repoSystem.newLocalRepositoryManager(session, localRepo));
        if (proxySelector != null) {
            session.setProxySelector(proxySelector);
        }
        return session;
    }

    static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    public static String formatColumns(List<String> lst, int width, int height) {
        String[] array = new String[lst.size()];
        lst.toArray(array);
        return Parser.formatDisplayList(array, height, width);
    }

    public static Path resolvePath(AeshContext ctx, String path) {
        Path workDir = PmSession.getWorkDir(ctx);
        return Paths.get(FileConverter.translatePath(workDir.toString(), path));
    }
}
