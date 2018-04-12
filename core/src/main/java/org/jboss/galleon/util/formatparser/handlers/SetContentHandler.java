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
package org.jboss.galleon.util.formatparser.handlers;

import java.util.Collections;
import java.util.Set;

import org.jboss.galleon.util.CollectionUtils;
import org.jboss.galleon.util.formatparser.FormatContentHandler;
import org.jboss.galleon.util.formatparser.FormatParsingException;
import org.jboss.galleon.util.formatparser.ParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class SetContentHandler extends FormatContentHandler {

    private Set<Object> set = Collections.emptySet();

    public SetContentHandler(ParsingFormat format, int strIndex) {
        super(format, strIndex);
    }

    @Override
    public void addChild(FormatContentHandler childHandler) throws FormatParsingException {
        set = CollectionUtils.add(set, childHandler.getContent());
    }

    @Override
    public Object getContent() throws FormatParsingException {
        return set;
    }
}
