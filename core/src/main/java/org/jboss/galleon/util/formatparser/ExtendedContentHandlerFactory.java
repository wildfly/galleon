/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;

import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ExtendedContentHandlerFactory implements FormatContentHandlerFactory {

    public static ExtendedContentHandlerFactory getInstance() {
        return new ExtendedContentHandlerFactory(DefaultContentHandlerFactory.getInstance());
    }

    public static ExtendedContentHandlerFactory getInstance(FormatContentHandlerFactory delegate) {
        return new ExtendedContentHandlerFactory(delegate);
    }

    private static final Class<?>[] CH_CTOR_ARGS = new Class[] {ParsingFormat.class, int.class};

    private final FormatContentHandlerFactory delegate;
    private Map<String, Class<? extends FormatContentHandler>> chTypes = Collections.emptyMap();

    private ExtendedContentHandlerFactory(FormatContentHandlerFactory delegate) {
        this.delegate = delegate;
    }

    public ExtendedContentHandlerFactory addContentHandler(String formatName, Class<? extends FormatContentHandler> cls) {
        chTypes = CollectionUtils.put(chTypes, formatName, cls);
        return this;
    }

    @Override
    public FormatContentHandler forFormat(ParsingFormat format, int strIndex) throws FormatParsingException {
        final Class<? extends FormatContentHandler> chClass = chTypes.get(format.getContentType());
        if(chClass == null) {
            return delegate.forFormat(format, strIndex);
        }
        try {
            final Constructor<? extends FormatContentHandler> ctor = chClass.getConstructor(CH_CTOR_ARGS);
            return ctor.newInstance(format, strIndex);
        } catch (Exception e) {
            throw new FormatParsingException("Failed to instantiate content handler " + chClass.getName() + " for format " + format, e);
        }
    }
}
