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
package org.jboss.galleon.cli;

import org.aesh.utils.Config;

/**
 *
 * @author jdenise@redhat.com
 */
public interface HelpDescriptions {
    // CLI tool descriptions
    String TOOL_FILE_OPTION = "Path to file containing CLI commands to execute";
    String TOOL_HELP_OPTION = "Display help";

    // Domains descriiptions
    String DOMAIN_CONFIGURATION = "Commands to configure CLI tool";
    String DOMAIN_EDITING = "Commands to edit the current provisioning state. "
            + "Only available in edit mode";
    String DOMAIN_EDIT_MODE = "Commands to switch the tool into edit mode." + Config.getLineSeparator()
            + "This mode allows to create advanced provisioning state "
            + "that you can then use to provision installations.";
    String DOMAIN_FEATURE_PACK = "Commands to manage feature packs";
    String DOMAIN_INSTALLATION = "Commands to manage existing installations";
    String DOMAIN_PROVISIONING = "Commands to achieve main provisioning use cases";
    String DOMAIN_OTHER = "Other commands";

    // Commands and options descriptions
    String ADD_DEPENDENCY = "Add a feature pack dependency to the provisioning state";
    String ADD_FEATURE = "Add a new feature to a configuration";
    String ADD_UNIVERSE = "Add a universe to the current installation. Called without a universe name, set the default universe of this installation";
    String ADD_UNIVERSE_STATE = "Add a universe to the provisioning state. Called without a universe name, set the default universe of this installation";
    String BOOLEAN_OPT = "true or false";
    String CHECK_UPDATES = "Get available updates for a full installation or an identified feature pack";
    String CHECK_UPDATES_DEPENDENCIES = "Include dependencies when checking for updates. Doesn't apply when specifying feature-packs";
    String CHECK_UPDATES_FP = "The feature pack producers to check update for";
    String CD = "Changes the current work dir";
    String CD_PATH = "Target directory";
    String CD_STATE = "Changes the current node";
    String CLEAR_CACHE = "Clear the cache of feature packs";
    String CLEAR_HISTORY = "Clear the provisioning commands history";
    String CONFIGURATION_MODEL = "Configuration model";
    String CONFIGURATION_NAME = "Configuration name";
    String CONFIGURATION_FULL_NAME = "Configuration full name (<model>/<name>)";
    String CONFIGURATION_ORIGIN = "Configuration origin";
    String DEFINE_CONFIG = "Define a new empty configuration";
    String DIFF = "Save the current provisioned configuration changes into a feature pack";
    String DIFF_SRC_DIR = "Customized source installation directory";
    String DIFF_TARGET_DIR = "Directory to save the feature pack to";
    String EDIT_STATE = "Load an installation or a provisioning xml file in order to create a provisioning state";
    String EDIT_STATE_ARG = "Installation directory or provisionng file";
    String EXCLUDE_CONFIGURATION = "Exclude a configuration";
    String EXCLUDE_LAYERS = "Exclude layers from a custom configuration. Excluded layers are kept in the configuration but are disabled";
    String EXCLUDE_PACKAGE = "Exclude a package";
    String EXIT = "Exit CLI tool";
    String EXPORT = "Export the installation's provisioning configuration file";
    String EXPORT_FILE = "Path to file in which the provisioning configuration is generated";
    String EXPORT_STATE = "Generate a provisioning configuration file from a provisioning state";
    String FEATURE_PACK = "Contains commands to manage feature packs";
    String FEATURE_PATH = "Configuration / Feature id";
    String FILESYSTEM = "Contains commands to navigate the filesystem";
    String FIND = "Find feature pack locations that match the pattern";
    String FIND_LAYERS_PATTERN = "Comma separated list of layer name patterns. eg: ejb* to search for all feature-pack that offer an ejb layer. "
            + "If no feature pack location pattern is set, search into the final releases";
    String FIND_PATTERN = "Feature pack location and/or layer pattern. eg: wildfly:*.Final to search for all Final builds";
    String FIND_RESOLVED_ONLY = "Look-up in resolved feature-packs only";
    String FIND_UNIVERSE = "Provide a universe id in order to search for feature packs "
            + "located in not installed universe";
    String FP_FILE = "Feature pack zip file";
    String FP_FILE_IMPORT = FP_FILE + " to import";
    String FP_FILE_PATH = "Path to feature pack zip file";
    String FP_INFO_TYPE = "Type of information to display (all, configs, dependencies, layers, options)";
    String FP_LOCATION = "Feature pack location";
    String FP_PATH = "Feature pack node path";
    String FP_TO_REMOVE = "Feature pack to remove";
    String GET_HISTORY_LIMIT = "Get the history limit";
    String GET_CHANGES = "Display the files modified, added or removed from an installation";
    String GET_INFO = "Display information on an installation directory";
    String GET_INFO_FP = "Display information on a feature pack";
    String GET_INFO_STATE = "Display information on provisioning state";
    String HELP = "Display help for a given command. Without any argument list the set of available commands";
    String HELP_COMMAND_NAME = "The command name optionally followed by sub command name";
    String HISTORY_LIMIT = "History maximum number of stored states";
    String IMPORT_FP = "Import a feature pack zip file to cache and install it in universe";
    String INCLUDE_CONFIG = "Include a default configuration";
    String INCLUDE_LAYERS = "Include layers into a custom configuration";
    String INCLUDE_PACKAGE = "Include a package";
    String INCLUDE_DEFAULT_CONFIGS = "Include the default configurations defined in this feature pack. By default they are not included";
    String INCLUDE_DEFAULT_PACKAGES = "Include the default packages defined in this feature pack. By default they are not included";
    String INFO_TYPE = "Type of information to display (all, configs, dependencies, layers, options, patches, universes)";
    String INSTALL = "Installs specified feature pack";
    String INSTALLATION = "Contains commands to manage existing installations";
    String INSTALLATION_DIRECTORY = "Installation directory";
    String INSTALL_CONFIG = "A <configuration model>/<configuration name> to configure configuration generated with layers. "
            + "The configuration model is optional, it is retrieved from the feature-pack content";
    String INSTALL_DEFAULT_CONFIGS = "A comma separated list of <configuration model>/<configuration name>";
    String INSTALL_IN_UNIVERSE = "Install feature pack to universe. Optional, it is installed by default";
    String INSTALL_LAYERS = "Comma seperated list of layers";
    String INSTALL_MODEL = "The layers model";
    String LEAVE_EXPLORATION = "Leave exploration";
    String LEAVE_STATE = "Leave provisioning state";
    String LIST = "List latest available feature packs for the default frequency";
    String LIST_ALL_FREQUENCIES = "Display the latest builds for all frequencies";
    String LIST_UNIVERSE = "Provide a universe id in order to list feature packs "
            + "located in not installed universe";
    String LOCATION_FP_RESOLVE = "Location of feature pack to resolve";
    String LS = "List the current node content";
    String MAVEN = "Contains commands to configure maven support";
    String MVN_ADD_REPO = "Add a maven repository";
    String MVN_GET_INFO = "Display maven configuration content";
    String MVN_POLICIES = "Update policies are 'always', 'daily', 'never', 'interval:<minutes>'. NB: Interval is expressed in minutes";
    String MVN_SET_DEFAULT_RELEASE_POLICY = "Set the default release update policy";
    String MVN_SET_DEFAULT_SNAPSHOT_POLICY = "Set the default snapshot update policy";
    String MVN_ENABLE_OFFLINE = "Enable or disable the offline mode";
    String MVN_ENABLE_RELEASE = "Enable or disable 'release' artifacts resolution";
    String MVN_ENABLE_SNAPSHOT = "Enable or disable 'snapshot' artifacts resolution";
    String MVN_LOCAL_REPO_PATH = "Path to local maven repository";
    String MVN_RELEASE_UPDATE_POLICY = "Maven release update policy. " + MVN_POLICIES;
    String MVN_REMOVE_REPO = "Remove a maven repository from configuration";
    String MVN_REPO_ENABLE_RELEASE = "Enable or disable 'release' artifacts resolution. If not set relies on default resolution";
    String MVN_REPO_ENABLE_SNAPSHOT = "Enable or disable 'snapshot' artifacts resolution. If not set relies on default resolution";
    String MVN_REPO_NAME = "Maven remote repository name";
    String MVN_REPO_TYPE = "Maven remote repository type, \"" + CliMavenArtifactRepositoryManager.DEFAULT_REPOSITORY_TYPE + "\" by default";
    String MVN_REPO_URL = "Maven remote repository URL";
    String MVN_RESET_DEFAULT_RELEASE_POLICY = "Reset to the default release update policy";
    String MVN_RESET_DEFAULT_SNAPSHOT_POLICY = "Reset to the default snapshot update policy";
    String MVN_RESET_LOCAL_PATH = "Reset to the local repository default path";
    String MVN_RESET_OFFLINE = "Reset the offline mode to its default value";
    String MVN_RESET_RELEASE = "Reset release artifact resolution to default value";
    String MVN_RESET_SETTINGS_PATH = "Reset the path to the maven xml settings file";
    String MVN_RESET_SNAPSHOT = "Reset snapshot artifact resolution to default value";
    String MVN_SETTINGS_PATH = "Path to maven xml settings file";
    String MVN_SET_LOCAL_PATH = "Set the path to the local maven repository path";
    String MVN_SET_SETTINGS_PATH = "Set the path to the maven xml settings file";
    String MVN_SNAPSHOT_UPDATE_POLICY = "Maven snapshot update policy. " + MVN_POLICIES;
    String MVN_UPDATE_POLICY = "Update policy. " + MVN_POLICIES;
    String NEW_STATE = "New empty provisioning state";
    String PACKAGE_NAME = "Package name";
    String PACKAGE_ORIGIN = "Package origin";
    String PACKAGE_PATH = "Path to a package";
    String PERSIST_CHANGES = "Persist the configuration changes into the provisioning configuration";
    String PROVISION = "Provision an installation from a provisioning file";
    String PROVISION_STATE = "Provision an installation from a provisioning state";
    String PROVISION_FILE = "File describing the desired provisioned state";
    String PWD = "Display the current path";
    String REMOVE_DEPENDENCY = "Remove a feature pack dependency from the provisioning state";
    String REMOVE_EXCLUDED_CONFIG = "Remove an already excluded configuration";
    String REMOVE_EXCLUDED_LAYERS = "Remove excluded layers from a custom configuration";
    String REMOVE_EXCLUDED_PACKAGE = "Remove an already excluded package";
    String REMOVE_FEATURE = "Remove a feature";
    String REMOVE_INCLUDED_CONFIG = "Remove an already included configuration";
    String REMOVE_INCLUDED_PACKAGE = "Remove an already included package";
    String REMOVE_INCLUDED_LAYERS = "Remove included layers from a custom configuration";
    String REMOVE_UNIVERSE = "Remove a universe. Without any name provided, remove the default universe";
    String RESET_CONFIG = "Reset a configuration to its default state";
    String RESOLVE_FP = "Download a feature pack to the local maven repository";
    String SEARCH_IN_DEPENDENCIES = "Include dependencies in the search";
    String SEARCH_QUERY = "Text to search for";
    String SEARCH_STATE = "Search the provisioning state for content";
    String SET_HISTORY_LIMIT = "Set the history limit";
    String STATE = "Contains commands to switch to provisioning state editing mode";
    String UNDO = "Undo the last provisioning command";
    String UNDO_STATE = "Undo the last editing command";
    String UNINSTALL = "Un-install a feature pack from an installation.";
    String UNIVERSE_FACTORY = "Universe factory name";
    String UNIVERSE_LOCATION = "Universe location";
    String UNIVERSE_NAME = "Universe name";
    String UPDATE = "Update the installation to the latest available updates and patches. "
            + "If feature pack locations are provided thanks to the --feature-packs option, the version they contain are used to update";
    String UPDATE_DEPENDENCIES = "Include dependencies when updating. Doesn't apply when specifying feature-packs";
    String UPDATE_FP = "The feature pack producers or locations to update. If a feature pack location is provided, the version it contains is used to update";
    String UPDATE_NO_CONFIRMATION = "No confirmation required";
    String VERBOSE = "Whether or not the output should be verbose";

}
