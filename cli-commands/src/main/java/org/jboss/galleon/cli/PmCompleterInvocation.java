/*
 * Copyright 2016-2023 Red Hat, Inc. and/or its affiliates
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

import java.util.Collection;
import java.util.List;
import org.aesh.command.Command;
import org.aesh.command.completer.CompleterInvocation;
import org.aesh.readline.AeshContext;
import org.aesh.readline.terminal.formatting.TerminalString;

/**
 *
 * @author jdenise@redhat.com
 */
public class PmCompleterInvocation implements CompleterInvocation {

    private final CompleterInvocation delegate;
    private final PmSession session;

    public PmCompleterInvocation(CompleterInvocation delegate, PmSession session) {
        this.delegate = delegate;
        this.session = session;
    }

    @Override
    public String getGivenCompleteValue() {
        return delegate.getGivenCompleteValue();
    }

    @Override
    public Command getCommand() {
        return delegate.getCommand();
    }

    @Override
    public List<TerminalString> getCompleterValues() {
        return delegate.getCompleterValues();
    }

    @Override
    public void setCompleterValuesTerminalString(List<TerminalString> terminalStrings) {
        delegate.setCompleterValuesTerminalString(terminalStrings);
    }

    @Override
    public void clearCompleterValues() {
        delegate.clearCompleterValues();
    }

    @Override
    public void addCompleterValue(String s) {
        delegate.addCompleterValue(s);
    }

    @Override
    public void addCompleterValueTerminalString(TerminalString terminalString) {
        delegate.addCompleterValueTerminalString(terminalString);
    }

    @Override
    public boolean isAppendSpace() {
        return delegate.isAppendSpace();
    }

    @Override
    public void setAppendSpace(boolean b) {
        delegate.setAppendSpace(b);
    }

    @Override
    public void setIgnoreOffset(boolean ignoreOffset) {
        delegate.setIgnoreOffset(ignoreOffset);
    }

    @Override
    public boolean doIgnoreOffset() {
        return delegate.doIgnoreOffset();
    }

    @Override
    public void setOffset(int offset) {
        delegate.setOffset(offset);
    }

    @Override
    public int getOffset() {
        return delegate.getOffset();
    }

    @Override
    public void setIgnoreStartsWith(boolean ignoreStartsWith) {
        delegate.setIgnoreStartsWith(ignoreStartsWith);
    }

    @Override
    public boolean isIgnoreStartsWith() {
        return delegate.isIgnoreStartsWith();
    }

    @Override
    public AeshContext getAeshContext() {
        return delegate.getAeshContext();
    }

    public PmSession getPmSession() {
        return session;
    }

    @Override
    public void setCompleterValues(Collection<String> completerValues) {
        delegate.setCompleterValues(completerValues);
    }

    @Override
    public void addAllCompleterValues(Collection<String> completerValues) {
        delegate.addAllCompleterValues(completerValues);
    }
}
