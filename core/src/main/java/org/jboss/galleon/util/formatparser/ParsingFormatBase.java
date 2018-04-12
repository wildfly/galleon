/*
 * Copyright ${license.git.copyrightYears} Red Hat, Inc. and/or its affiliates
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
