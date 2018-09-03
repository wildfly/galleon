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

/**
 *
 * @author jdenise@redhat.com
 */
public interface HelpDescriptions {
    // CLI tool descriptions
    String TOOL_FILE_OPTION = "Path to file containing CLI commands to execute";
    String TOOL_HELP_OPTION = "Display help";

    // Commands and options descriptions
    String ADD_FEATURE = "Add a new feature";
    String ADD_FEATURE_PACK = "Add a feature-pack";
    String ADD_UNIVERSE = "Add a universe to the current installation. Called without a universe name, set the default universe of this installation.";
    String BOOLEAN_OPT = "true or false";
    String CHECK_UPDATES = "Get available updates for a full installation or an identified feature pack";
    String CHECK_UPDATES_DEPENDENCIES = "Include dependencies when checking for updates";
    String CHECK_UPDATES_FP = "The feature pack to search update for";
    String CD = "Changes the current work dir (or feature pack node in edit mode) to the specified location";
    String CLEAR_CACHE = "Clear the cache of feature-packs";
    String CLEAR_HISTORY = "Clear the provisioning commands history";
    String CONFIGURATION_NAME = "Configuration name";
    String CONFIGURATION_ORIGIN = "Configuration origin";
    String DIFF = "Saves current provisioned configuration changes into a feature pack.";
    String DIFF_SRC_DIR = "Customized source installation directory";
    String DIFF_TARGET_DIR = "Directory to save the feature pack to";
    String DIR_OR_FP_PATH = "Directory (or feature pack node) path";
    String DISPLAY_FP_INFO = "Display information on a feature-pack";
    String EDIT_STATE = "Load an installation or a provisioning xml file in order to create a provisioning state and edit it";
    String EDIT_STATE_ARG = "Installation directory or provisionng file";
    String EXCLUDE_CONFIGURATION = "Exclude a configuration";
    String EXCLUDE_PACKAGE = "Exclude a package";
    String EXIT = "Exit CLI";
    String EXPLORE_FP = "Explore a feature-pack";
    String EXPLORE_INSTALLATION = "Explore an installation";
    String EXPORT = "Generate a provisioning configuration file from an installation or a in memory state";
    String EXPORT_FILE = "Path to file used to generate the provisioning configuration";
    String FEATURE_PATH = "Configuration / Feature id";
    String FETCH_FP = "Download a feature-pack to the local repository";
    String FP_FILE = "Feature Pack file";
    String FP_FILE_IMPORT = FP_FILE + " to import";
    String FP_FILE_PATH = "Path to feature pack file";
    String FP_INFO_TYPE = "Type of information to display (all, configs, dependencies, options)";
    String FP_LOCATION = "Feature Pack location";
    String FP_TO_REMOVE = "Feature Pack to remove";
    String GET_HISTORY_LIMIT = "Get the history limit";
    String HELP = "Display help for a given command. " + "Without any argument list the set of available commands";
    String HELP_COMMAND_NAME = "The command name optionally followed by sub command name";
    String HISTORY_LIMIT = "History maximum number of stored states";
    String IMPORT_FP = "Import a feature-pack";
    String INCLUDE_CONFIG = "Include a configuration";
    String INCLUDE_PACKAGE = "Include a package";
    String INCLUDE_DEFAULT_CONFIGS = "Include the default configurations defined in this feature-pack. By default they are not included";
    String INCLUDE_DEFAULT_PACKAGES = "Include the default packages defined in this feature-pack. By default they are not included";
    String INFO = "Display information for an installation directory or editing state";
    String INFO_TYPE = "Type of information to display (all, configs, dependencies, options, patches)";
    String INSTALL = "Installs specified feature-pack";
    String INSTALLATION_DIRECTORY = "Installation directory";
    String INSTALL_IN_UNIVERSE = "Install feature-pack to universe. Optional, it is installed by default";
    String LEAVE_STATE = "Leave provisioning state";
    String LOCATION_FP = "Location of feature pack to fetch";
    String LOCATION_FP_FETCH = LOCATION_FP + " to fetch";
    String LS = "Show the current directory content (or feature pack node in edit mode)";
    String MVN_ADD_REPO = "Add a maven repository";
    String MVN_SET_DEFAULT_RELEASE_POLICY = "Set the default release update policy";
    String MVN_SET_DEFAULT_SNAPSHOT_POLICY = "Set the default snapshot update policy";
    String MVN_DISPLAY_CONFIG = "Display maven configuration content";
    String MVN_ENABLE_RELEASE = "Enable release";
    String MVN_ENABLE_SNAPSHOT = "Enable snapshot";
    String MVN_LOCAL_REPO_PATH = "Path to local maven repository. Without any path provided, reset to default value";
    String MVN_RELEASE_UPDATE_POLICY = "Maven release update policy. NB: Interval is expressed in minutes";
    String MVN_REMOVE_REPO = "Remove a maven repository from configuration";
    String MVN_REPO_NAME = "Maven remote repository name";
    String MVN_REPO_TYPE = "Maven remote repository type, \"" + CliMavenArtifactRepositoryManager.DEFAULT_REPOSITORY_TYPE + "\" by default";
    String MVN_REPO_URL = "Maven remote repository URL";
    String MVN_SETTINGS_PATH = "Path to maven xml settings file. Without any path provided, unset the path";
    String MVN_SET_LOCAL_PATH = "Set or reset the path to the local maven repository path";
    String MVN_SET_SETTINGS_PATH = "Set or reset the path to the maven xml settings file";
    String MVN_SNAPSHOT_UPDATE_POLICY = "Maven snapshot update policy. NB: Interval is expressed in minutes";
    String MVN_UPDATE_POLICY = "Update policy. Can be always, daily, never, interval:<minutes>";
    String MKDIR = "Create directory(ies), if they do not already exist";
    String NEW_STATE = "New provisioning state";
    String PACKAGE_NAME = "Package name";
    String PACKAGE_ORIGIN = "Package origin";
    String PACKAGE_PATH = "Path to a package";
    String PROVISION = "Install from a provisioning file or the current state";
    String PROVISION_FILE = "File describing the desired provisioned state";
    String PWD = "Display the current path";
    String REMOVE_EXCLUDED_CONFIG = "Remove an already excluded configuration";
    String REMOVE_EXCLUDED_PACKAGE = "Remove an already excluded package";
    String REMOVE_FEATURE = "Remove a feature";
    String REMOVE_FEATURE_PACK = "Remove a feature pack";
    String REMOVE_INCLUDED_CONFIG = "Remove an already included configuration";
    String REMOVE_INCLUDED_PACKAGE = "Remove an already included package";
    String REMOVE_UNIVERSE = "Remove a universe. Without any name provided, remove the default universe";
    String RESET_CONFIG = "Reset a configuration to its default state";
    String RM = "Remove files or directories";
    String SEARCH_IN_DEPENDENCIES = "Include dependencies in the search";
    String SEARCH_QUERY = "Text to search for";
    String SEARCH_STATE = "Search the state for the provided content";
    String SET_HISTORY_LIMIT = "Set the history limit";
    String UNDO = "Undo the last provisioning command";
    String UNINSTALL = "Un-install a feature pack from an installation. " + "Plugin options to re-apply to remaining feature packs can be provided";
    String UNIVERSE_FACTORY = "Universe factory name";
    String UNIVERSE_LIST = "List universes and products";
    String UNIVERSE_LIST_PRODUCT = "List products that match the provided pattern";
    String UNIVERSE_LIST_UNIVERSE = "In order to list product from not installed universe, you can provide a universe id";
    String UNIVERSE_LOCATION = "Universe location";
    String UNIVERSE_NAME = "Universe name";
    String UPDATE = "Update the installation to latest available updates and patches";
    String UPDATE_DEPENDENCIES = "Include dependencies when updating";
    String UPDATE_FP = "The feature pack to update";
    String UPDATE_NO_CONFIRMATION = "No confirmation required";
    String VERBOSE = "Whether or not the output should be verbose";

}
