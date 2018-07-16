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
package org.jboss.galleon.cli.cmd;

/**
 *
 * @author jdenise@redhat.com
 */
public interface CliErrors {

    static String addFeatureFailed() {
        return failed("Add Faiture");
    }

    static String addRepositoryFailed() {
        return failed("Add Repository");
    }

    static String diffFailed() {
        return failed("Diff");
    }

    static String displayContentFailed() {
        return failed("Display content");
    }

    static String enterFPFailed() {
        return failed("Enter feature-pack");
    }

    static String excludeFailed() {
        return failed("Exclude");
    }

    static String exploreFailed() {
        return failed("Explore");
    }

    static String exportProvisionedFailed() {
        return failed("Export provisioned state");
    }

    static String failed(String action) {
        return action + " failed.";
    }

    static String fetchFeaturePackFailed() {
        return failed("Fetch feature-pack");
    }

    static String importFeaturePackFailed() {
        return failed("Import feature-pack");
    }

    static String includeFailed() {
        return failed("Include");
    }

    static String infoFailed() {
        return failed("Retrieve info");
    }

    static String installFailed() {
        return failed("Install");
    }

    static String newStateFailed() {
        return failed("Create new state");
    }

    static String provisioningFailed() {
        return failed("Provisioning");
    }

    static String readContentFailed() {
        return failed("Read content state");
    }

    static String readProvisionedStateFailed() {
        return failed("Read provisioned state");
    }

    static String removeFailed() {
        return failed("Remove");
    }

    static String removeFeatureFailed() {
        return failed("Remove feature");
    }

    static String removeRepositoryFailed() {
        return failed("Remove Repository");
    }

    static String resetConfigFailed() {
        return failed("Reset configuration");
    }

    static String resolveLocationFailed() {
        return failed("Resolve location");
    }

    static String retrievePath() {
        return failed("Retrieve path");
    }

    static String retrieveProducerFailed() {
        return failed("Retrieve producer");
    }

    static String retrieveFeaturePackID() {
        return failed("Retrieve feature-pack id");
    }

    static String searchFailed() {
        return failed("Search");
    }

    static String setLocalRepositoryFailed() {
        return failed("Set local repository");
    }

    static String setSettingsFailed() {
        return failed("Set settings");
    }

    static String stateCommandFailed() {
        return failed("State Command");
    }

    static String undoFailed() {
        return failed("Undo");
    }

}
