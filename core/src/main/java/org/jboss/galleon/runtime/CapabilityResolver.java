/*
 * Copyright 2016-2019 Red Hat, Inc. and/or its affiliates
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
package org.jboss.galleon.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.galleon.Errors;
import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.spec.CapabilitySpec;

/**
 * @author Alexey Loubyansky
 *
 */
public class CapabilityResolver {

    private ArrayList<StringBuilder> capList = null;
    private StringBuilder capBuf;

    private ResolvedFeature feature;
    private CapabilitySpec capSpec;
    private String currentElem;

    List<String> resolve(CapabilitySpec capSpec, ResolvedFeature feature) throws ProvisioningException {
        if(capSpec.isStatic()) {
            return Collections.singletonList(capSpec.toString());
        }
        if(capBuf == null) {
            capBuf = new StringBuilder();
        }
        this.feature = feature;
        this.capSpec = capSpec;
        try {
            if(!capSpec.resolve(this)) {
                return Collections.emptyList();
            }
            if(capList == null) {
                return capBuf.length() == 0 ? Collections.emptyList() : Collections.singletonList(capBuf.toString());
            }
            List<String> resolved = new ArrayList<>(capList.size());
            for (int i = 0; i < capList.size(); ++i) {
                resolved.add(capList.get(i).toString());
            }
            return resolved;
        } catch(ProvisioningException e) {
            throw new ProvisioningException(Errors.failedToResolveCapability(feature, capSpec), e);
        } finally {
            reset();
        }
    }

    void reset() {
        feature = null;
        capList = null;
        capBuf.setLength(0);
        currentElem = null;
    }

    public CapabilitySpec getSpec() {
        return capSpec;
    }

    public String getElem() {
        return currentElem;
    }

    public boolean resolveElement(String elem, boolean isStatic) throws ProvisioningException {
        if(isStatic) {
            add(elem);
            return true;
        }
        this.currentElem = elem;
        return feature.spec.resolveCapabilityElement(feature, elem, this);
    }

    public CapabilityResolver add(Object elem) throws ProvisioningException {
        if(capList == null) {
            if(capBuf.length() > 0) {
                capBuf.append('.');
            }
            capBuf.append(toStringElem(elem));
            return this;
        }
        final String str = toStringElem(elem);
        for(int i = 0; i < capList.size(); ++i) {
            capList.get(i).append('.').append(str);
        }
        return this;
    }

    public CapabilityResolver multiply(Collection<?> elems) throws ProvisioningException {
        if(elems.isEmpty()) {
            throw new ProvisioningException(Errors.illegalCapabilityElement(capSpec, elems.toString(), capBuf.toString()));
        }
        if(elems.size() == 1) {
            add(elems.iterator().next());
            return this;
        }
        if(capList == null) {
            if(capBuf.length() > 0) {
                capBuf.append('.');
            }
            capList = new ArrayList<>(elems.size());
            for(Object o : elems) {
                capList.add(capBuf.length() == 0 ? new StringBuilder(toStringElem(o)) : new StringBuilder(capBuf).append(toStringElem(o)));
            }
            return this;
        }

        capList.ensureCapacity(capList.size() * elems.size());
        final int capsTotal = capList.size();
        for (int i = 0; i < capsTotal; ++i) {
            final StringBuilder capBuf = capList.get(i).append('.');
            final Iterator<?> elemI = elems.iterator();
            final Object firstElem = elemI.next();
            while(elemI.hasNext()) {
                capList.add(new StringBuilder(capBuf).append(toStringElem(elemI.next())));
            }
            capBuf.append(toStringElem(firstElem));
        }
        return this;
    }

    private String toStringElem(Object elem) throws ProvisioningException {
        if(elem == null) {
            throw new ProvisioningException(Errors.illegalCapabilityElement(capSpec, null, capBuf.toString()));
        }
        final String str = elem.toString().trim();
        if(str.isEmpty()) {
            throw new ProvisioningException(Errors.illegalCapabilityElement(capSpec, str, capBuf.toString()));
        }
        return str;
    }
}
