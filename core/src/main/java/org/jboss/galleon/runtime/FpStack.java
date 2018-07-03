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
package org.jboss.galleon.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jboss.galleon.ProvisioningException;
import org.jboss.galleon.config.ConfigCustomizations;
import org.jboss.galleon.config.ConfigId;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.PackageConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation.ProducerSpec;
import org.jboss.galleon.util.CollectionUtils;

/**
 *
 * @author Alexey Loubyansky
 */
class FpStack {

    private static final int INHERIT_PKGS_FALSE = -1;
    private static final int INHERIT_PKGS_NOT_FOUND = 0;
    private static final int INHERIT_PKGS_TRANSITIVE = 1;
    private static final int INHERIT_PKGS_TRUE = 2;

    private class Level {

        private List<FeaturePackConfig> fpConfigs = Collections.emptyList();
        private Map<ProducerSpec, FeaturePackConfig> transitive = Collections.emptyMap();
        private int currentFp = -1;

        Level copy() {
            final Level copy = new Level();
            copy.fpConfigs = CollectionUtils.clone(fpConfigs);
            copy.currentFp = currentFp;
            return copy;
        }

        void addFpConfig(FeaturePackConfig fpConfig) {
            fpConfigs = CollectionUtils.add(fpConfigs, fpConfig);
            if(fpConfig.isTransitive()) {
                transitive = CollectionUtils.put(transitive, fpConfig.getLocation().getProducer(), fpConfig);
            }
        }

        boolean hasNext() {
            return currentFp + 1 < fpConfigs.size();
        }

        FeaturePackConfig next() {
            if(!hasNext()) {
                throw new IndexOutOfBoundsException((currentFp + 1) + " exceeded " + fpConfigs.size());
            }
            return fpConfigs.get(++currentFp);
        }

        boolean isFilteredOut(ProducerSpec producer, ConfigId configId) {
            final FeaturePackConfig fpConfig = transitive.get(producer);
            if(fpConfig != null && FpStack.isFilteredOut(fpConfig, configId)) {
                return true;
            }
            return FpStack.isFilteredOut(fpConfigs.get(currentFp), configId);
        }

        boolean isInheritConfigs() {
            return fpConfigs.get(currentFp).isInheritConfigs();
        }

        boolean isInheritModelOnlyConfigs() {
            return fpConfigs.get(currentFp).isInheritModelOnlyConfigs();
        }

        boolean isInheritPackages() {
            return fpConfigs.get(currentFp).isInheritPackages();
        }

        int isInheritPackages(ProducerSpec producer) {
            final FeaturePackConfig fpConfig = getFpConfig(producer);
            if(fpConfig == null) {
                return INHERIT_PKGS_NOT_FOUND;
            }
            if(!fpConfig.isInheritPackages()) {
                return INHERIT_PKGS_FALSE;
            }
            return fpConfig.isTransitive() ? INHERIT_PKGS_TRANSITIVE : INHERIT_PKGS_TRUE;
        }

        boolean isPackageExcluded(ProducerSpec producer, String packageName) {
            final FeaturePackConfig fpConfig = getFpConfig(producer);
            return fpConfig == null ? false : fpConfig.isPackageExcluded(packageName);
        }

        boolean isPackageIncluded(ProducerSpec producer, String packageName) {
            final FeaturePackConfig fpConfig = getFpConfig(producer);
            return fpConfig == null ? false : fpConfig.isPackageIncluded(packageName);
        }

        Boolean isPackageFilteredOut(ProducerSpec producer, String packageName) {
            final FeaturePackConfig fpConfig = getFpConfig(producer);
            if(fpConfig == null) {
                return null;
            }
            if(fpConfig.isInheritPackages()) {
                return fpConfig.isPackageExcluded(packageName);
            }
            return !fpConfig.isPackageIncluded(packageName);
        }

        private FeaturePackConfig getFpConfig(ProducerSpec producer) {
            for(int i = fpConfigs.size() - 1; i >= 0; --i) {
                final FeaturePackConfig fpConfig = fpConfigs.get(i);
                if(fpConfig.getLocation().getProducer().equals(producer)) {
                    return fpConfig;
                }
            }
            return null;
        }
    }

    private final ProvisioningConfig config;
    private List<Level> levels = new ArrayList<>();
    private Level lastPushed;

    private List<List<Level>> recordedStacks = Collections.emptyList();

    FpStack(ProvisioningConfig config) {
        this.config = config;
    }

    boolean push(FeaturePackConfig fpConfig, boolean extendCurrentLevel) {
        if(!isRelevant(fpConfig)) {
            return false;
        }
        if(extendCurrentLevel) {
            lastPushed.addFpConfig(fpConfig);
            return true;
        }
        final Level newLevel = new Level();
        newLevel.addFpConfig(fpConfig);
        levels.add(newLevel);
        lastPushed = newLevel;
        return true;
    }

    void popLevel() {
        if(isEmpty()) {
            return;
        }
        if(levels.size() == 1) {
            levels.clear();
            lastPushed = null;
        } else {
            levels.remove(levels.size() - 1);
            lastPushed = levels.get(levels.size() - 1);
        }
    }

    boolean isEmpty() {
        return lastPushed == null;
    }

    boolean hasNext() {
        if(lastPushed == null) {
            return false;
        }
        return lastPushed.hasNext();
    }

    FeaturePackConfig next() {
        if(lastPushed == null) {
            throw new NoSuchElementException();
        }
        return lastPushed.next();
    }

    boolean isFilteredOut(ProducerSpec producer, ConfigId configId, boolean fromPrevLevel) {
        int i = levels.size() - (fromPrevLevel ? 2 : 1);
        while(i >= 0) {
            if(levels.get(i--).isFilteredOut(producer, configId)) {
                return true;
            }
        }
        return isFilteredOut(config, configId);
    }

    private static boolean isFilteredOut(ConfigCustomizations configCustoms, ConfigId configId) {
        if(configId.isAnonymous()) {
            return !configCustoms.isInheritConfigs();
        }
        if(configId.isModelOnly()) {
            return configCustoms.isConfigModelExcluded(configId) || !configCustoms.isInheritModelOnlyConfigs();
        }
        if(configCustoms.isInheritConfigs()) {
            if(configCustoms.isConfigExcluded(configId)) {
                return true;
            }
            if(configCustoms.isConfigModelExcluded(configId)) {
                if(configCustoms.isConfigIncluded(configId)) {
                    return false;
                }
                return true;
            }
            return false;
        }
        if(configCustoms.isConfigIncluded(configId)) {
            return false;
        }
        if(configCustoms.isConfigModelIncluded(configId)) {
            if(configCustoms.isConfigExcluded(configId)) {
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean isRelevant(FeaturePackConfig fpConfig) {
        if(isEmpty()) {
            return true;
        }
        final ProducerSpec producer = fpConfig.getLocation().getProducer();
        final int inheritPkgs = isInheritPackages(producer);
        if(inheritPkgs == INHERIT_PKGS_NOT_FOUND || inheritPkgs == INHERIT_PKGS_TRANSITIVE && !fpConfig.isTransitive()) {
            return true;
        }
        if(inheritPkgs > 0) {
            if(fpConfig.hasExcludedPackages()) {
                for(String excluded : fpConfig.getExcludedPackages()) {
                    if(!isPackageExcluded(producer, excluded) && !isPackageIncluded(producer, excluded)) {
                        return true;
                    }
                }
            }
            if(fpConfig.hasIncludedPackages()) {
                for(PackageConfig included : fpConfig.getIncludedPackages()) {
                    if(!isPackageIncluded(producer, included.getName()) && !isPackageExcluded(producer, included.getName())) {
                        return true;
                    }
                }
            }
        }

        if (fpConfig.hasDefinedConfigs()) {
            boolean configsInherited = true;
            for(int i = levels.size() - 1; i >= 0; --i) {
                if(!levels.get(i).isInheritConfigs()) {
                    configsInherited = false;
                    break;
                }
            }
            if (configsInherited) {
                return true;
            }
        }

        if (fpConfig.hasModelOnlyConfigs()) {
            boolean configsInherited = true;
            for(int i = levels.size() - 1; i >= 0; --i) {
                if(!levels.get(i).isInheritModelOnlyConfigs()) {
                    configsInherited = false;
                    break;
                }
            }
            if (configsInherited) {
                return true;
            }
        }
        return false;
    }

    private int isInheritPackages(ProducerSpec producer) {
        int result = INHERIT_PKGS_NOT_FOUND;
        for(int i = levels.size() - 1; i >= 0; --i) {
            final int levelResult = levels.get(i).isInheritPackages(producer);
            if(levelResult < 0) {
                return levelResult;
            }
            if(levelResult > result) {
                result = levelResult;
            }
        }
        return result;
    }

    boolean isPackageExcluded(ProducerSpec producer, String packageName) {
        for(int i = levels.size() - 1; i >= 0; --i) {
            if(levels.get(i).isPackageExcluded(producer, packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPackageIncluded(ProducerSpec producer, String packageName) {
        for(int i = levels.size() - 1; i >= 0; --i) {
            if(levels.get(i).isPackageIncluded(producer, packageName)) {
                return true;
            }
        }
        return false;
    }

    boolean isPackageFilteredOut(ProducerSpec producer, String packageName, boolean fromPrevLevel) {
        int i = levels.size() - (fromPrevLevel ? 2 : 1);
        if(i < 0) {
            return false;
        }
        Level level = levels.get(i--);
        Boolean filteredOut = level.isPackageFilteredOut(producer, packageName);
        if(filteredOut != null && filteredOut) {
            return true;
        }
        while (i >= 0) {
            level = levels.get(i--);
            if(filteredOut == null && !level.isInheritPackages()) {
                return true;
            }
            filteredOut = level.isPackageFilteredOut(producer, packageName);
            if(filteredOut != null && filteredOut) {
                return true;
            }
        }
        if(filteredOut == null && !level.isInheritPackages()) {
            return true;
        }
        return false;
    }

    void recordStack() {
        final List<Level> copy = new ArrayList<>(levels.size());
        for(int i = 0; i < levels.size(); ++i) {
            copy.add(levels.get(i).copy());
        }
        recordedStacks = CollectionUtils.add(recordedStacks, copy);
    }

    void activateConfigStack(int i) throws ProvisioningException {
        if(recordedStacks.size() <= i) {
            throw new ProvisioningException("Stack index " + i + " is exceeding the current stack size " + recordedStacks.size());
        }
        levels = recordedStacks.get(i);
        lastPushed = levels.isEmpty() ? null : levels.get(levels.size() - 1);
    }
}
