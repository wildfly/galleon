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

import java.nio.file.Path;

/**
 *
 * @author jdenise@redhat.com
 */
public interface CliErrors {

    static String addFeatureFailed() {
        return failed("Add feature");
    }

    static String addRepositoryFailed() {
        return failed("Add repository");
    }

    static String addUniverseFailed() {
        return failed("Add universe");
    }

    static String checkForUpdatesFailed() {
        return failed("Check for updates");
    }

    static String clearHistoryFailed() {
        return failed("Clear history");
    }

    static String diffFailed() {
        return failed("Diff");
    }

    static String displayContentFailed() {
        return failed("Display content");
    }

    static String emptyOption(String opt) {
        return "Empty option " + opt;
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

    static String getHistoryLimitFailed() {
        return failed("Get history limit");
    }

    static String invalidBoolean(String value) {
        return "Invalid boolean value " + value;
    }

    static String invalidConfigDirectory(Path galleonDir) {
        return "Configuration directory " + galleonDir + " is not a directory.";
    }

    static String invalidHistoryLimit(String limit) {
        return "Invalid history limit " + limit;
    }

    static String invalidInfoType() {
        return "Invalid info type";
    }

    static String invalidMavenUpdatePolicy(String policy) {
        return "Invalid update policy " + policy;
    }

    static String invalidUniverse() {
        return "Invalid universe";
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

    static String notFile(String absolutePath) {
        return "Not a file: " + absolutePath;
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
        return failed("Remove repository");
    }

    static String removeUniverseFailed() {
        return failed("Remove universe");
    }

    static String resetConfigFailed() {
        return failed("Reset configuration");
    }

    static String resolveLocationFailed() {
        return failed("Resolve location");
    }

    static String resolvedUniverseFailed() {
        return failed("Resolve universe");
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

    static String setHistoryLimitFailed() {
        return failed("Set history limit");
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

    static String subCommandMissing() {
        return "Sub command is missing";
    }

    static String undoFailed() {
        return failed("Undo");
    }

    static String uninstallFailed() {
        return failed("Uninstall");
    }

    static String unknownFile(String absolutePath) {
        return "File " + absolutePath + " doesn't exist";
    }

    static String updateFailed() {
        return failed("Update");
    }

}
