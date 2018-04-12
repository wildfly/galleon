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
package org.jboss.galleon.config;

/**
 * @author Alexey Loubyansky
 *
 */
public class ConfigId {

    final String model;
    final String name;

    public ConfigId(String model, String name) {
        this.model = model;
        this.name = name;
    }

    public boolean isAnonymous() {
        return model == null && name == null;
    }

    public boolean isModelOnly() {
        return model != null && name == null;
    }

    public String getModel() {
        return model;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        if(model != null) {
            buf.append("model=").append(model);
        }
        if(name != null) {
            if(model != null) {
                buf.append(' ');
            }
            buf.append("name=").append(name);
        }
        return buf.append(']').toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        ConfigId other = (ConfigId) obj;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
