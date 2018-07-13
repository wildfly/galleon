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
package org.jboss.galleon.cli.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import org.jboss.galleon.cli.PmSession;

/**
 *
 * @author jdenise@redhat.com
 */
public class ResourceResolver {

    public interface Resolver<T> {

        T resolve() throws ResolutionException;
    }

    private final Map<String, CompletableFuture<?>> content = new HashMap<>();

    private static final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thr = new Thread(r, "Galleon CLI resource resolver");
            thr.setDaemon(true);
            return thr;
        }
    });

    private final PmSession pmSession;

    public ResourceResolver(PmSession pmSession) {
        this.pmSession = pmSession;
    }

    public <T> void resolve(String key, Resolver<T> res) {
        doResolve(key, res, true);
    }

    public <T> void resolveSync(String key, Resolver<T> res) {
        doResolve(key, res, false);
    }

    @SuppressWarnings("unchecked")
    private <T> CompletableFuture<T> doResolve(String key, Resolver<T> res, boolean async) {
        CompletableFuture<T> comp;
        synchronized (this) {
            comp = (CompletableFuture<T>) content.get(key);
            if (comp == null) {
                comp = new CompletableFuture<>();
                content.put(key, comp);
            }
        }
        CompletableFuture<T> finalComp = comp;
        if (async) {
            executorService.submit(() -> {
                resolve(key, finalComp, res);
            });
        } else {
            resolve(key, finalComp, res);
        }
        return comp;
    }

    private <T> void resolve(String key, CompletableFuture<T> finalComp, Resolver<T> res) {
        T t;
        try {
            t = res.resolve();
        } catch (Throwable thr) {
            java.util.logging.Logger.getLogger(ResourceResolver.class.getName()).log(Level.FINEST,
                    "Exception while completing: {0}", thr.getLocalizedMessage());
            synchronized (this) {
                content.remove(key);
                finalComp.completeExceptionally(thr);
                return;
            }
        }
        finalComp.complete(t);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Resolver<T> res, String msg) throws InterruptedException, ExecutionException {
        CompletableFuture<T> comp;
        synchronized (this) {
            comp = (CompletableFuture<T>) content.get(key);
            if (comp == null) {
                if (res != null) {
                    comp = doResolve(key, res, false);
                }
            }
        }
        if (comp != null) {
            T ret = comp.get();
            return ret;
        }
        return null;
    }
}
