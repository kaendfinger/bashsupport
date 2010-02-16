/*
 * Copyright 2009 Joachim Ansorg, mail@ansorg-it.com
 * File: BashModuleSettings.java, Class: BashModuleSettings
 * Last modified: 2010-02-16
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ansorgit.plugins.bash.settings;

import com.ansorgit.plugins.bash.BashComponents;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.impl.storage.ClasspathStorage;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@State(
        name = BashComponents.MODULE_SETTINGS_STORAGE_ID,
        storages = {
                @Storage(
                        id = ClasspathStorage.DEFAULT_STORAGE,
                        file = "$MODULE_FILE$"
                )
        }
)
public class BashModuleSettings implements PersistentStateComponent<Element> {
    private Element settings;

    public static BashModuleSettings getInstance(@NotNull final Module module) {
        //return ServiceManager.getService(module.getProject(), BashModuleSettings.class);
        return module.getComponent(BashModuleSettings.class);
    }

    public String getID() {
        return BashComponents.MODULE_SETTINGS_STORAGE_ID;
    }

    public Element getState() {
        return settings;
    }

    public void loadState(@NotNull final Element settings) {
        this.settings = settings;
    }
}