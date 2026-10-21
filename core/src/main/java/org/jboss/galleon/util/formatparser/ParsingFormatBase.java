/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.galleon.util.formatparser;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class ParsingFormatBase implements ParsingFormat {

    protected final String name;
    protected final String contentType;
    protected final boolean wrapper;

    protected ParsingFormatBase(String name) {
        this(name, false);
    }

    protected ParsingFormatBase(String name, String contentType) {
        this(name, contentType, false);
    }

    protected ParsingFormatBase(String name, boolean wrapper) {
        this(name, name, wrapper);
    }

    protected ParsingFormatBase(String name, String contentType, boolean wrapper) {
        this.name = name;
        this.contentType = contentType;
        this.wrapper = wrapper;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.spec.type.ParsingFormat#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isMap() {
        return false;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.spec.type.ParsingFormat#isWrapper()
     */
    @Override
    public boolean isWrapper() {
        return wrapper;
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.spec.type.ParsingFormat#pushed(org.jboss.galleon.spec.type.ParsingContext)
     */
    @Override
    public void pushed(ParsingContext ctx) throws FormatParsingException {
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.spec.type.ParsingFormat#react(org.jboss.galleon.spec.type.ParsingContext)
     */
    @Override
    public void react(ParsingContext ctx) throws FormatParsingException {
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.spec.type.ParsingFormat#deal(org.jboss.galleon.spec.type.ParsingContext)
     */
    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
    }

    /* (non-Javadoc)
     * @see org.jboss.galleon.spec.type.ParsingFormat#eol(org.jboss.galleon.spec.type.ParsingContext)
     */
    @Override
    public void eol(ParsingContext ctx) throws FormatParsingException {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (wrapper ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParsingFormatBase other = (ParsingFormatBase) obj;
        if (contentType == null) {
            if (other.contentType != null)
                return false;
        } else if (!contentType.equals(other.contentType))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (wrapper != other.wrapper)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
