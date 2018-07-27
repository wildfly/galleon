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
package org.jboss.galleon.model;

import java.nio.file.Path;

/**
 * A {@link Gaecv} with the absolute {@link #path} to the given artifact file in the local filesystem.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Gaecvp {

    private final Gaecv gaecv;
    private final Path path;

    public Gaecvp(Gaecv gaecv, Path path) {
        super();
        this.gaecv = gaecv;
        this.path = path;
    }

    public Gaecv getGaecv() {
        return gaecv;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public String toString() {
        return gaecv.toString() + ":"+ path;
    }

}
