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
package org.jboss.galleon.cli.cmd.state.pkg;

import java.util.List;
import org.aesh.command.option.Argument;
import org.aesh.command.option.Option;
import org.jboss.galleon.cli.AbstractCompleter;
import org.jboss.galleon.cli.GalleonCLICommandCompleter;
import org.jboss.galleon.cli.HelpDescriptions;
import org.jboss.galleon.cli.PmCompleterInvocation;
import org.jboss.galleon.cli.PmSession;
import org.jboss.galleon.cli.cmd.state.AbstractFPProvisionedCommand;

/**
 *
 * @author jdenise@redhat.com
 */
public abstract class AbstractProvisionedPackageCommand extends AbstractFPProvisionedCommand {

    public static class ProvisionedPackageCompleter extends AbstractCompleter implements GalleonCLICommandCompleter {

        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            return completerInvocation.getPmSession().getState().completionContent(this, completerInvocation);
        }

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.pkg.core.CoreStateProvisionedPackageContentCompleter";
        }


    }
    public static class TargetedFPCompleter extends AbstractCompleter implements GalleonCLICommandCompleter {
        @Override
        protected List<String> getItems(PmCompleterInvocation completerInvocation) {
            return completerInvocation.getPmSession().getState().completionContent(this, completerInvocation);
        }

        @Override
        public String getCoreCompleterClassName(PmSession session) {
            return "org.jboss.galleon.cli.cmd.state.pkg.core.CoreStateTargetedFPContentCompleter";
        }
    }
    @Argument(required = true, description = HelpDescriptions.PACKAGE_NAME,
            completer = ProvisionedPackageCompleter.class)
    private String pkg;

    @Option(completer = TargetedFPCompleter.class, description = HelpDescriptions.PACKAGE_ORIGIN)
    private String origin;

    public String getPackage() {
        return pkg;
    }

    /**
     * @return the origin
     */
    public String getOrigin() {
        return origin;
    }

    public abstract boolean isIncludedPackages();
}
