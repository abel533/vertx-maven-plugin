/*
 *
 *   Copyright (c) 2016-2018 Red Hat, Inc.
 *
 *   Red Hat licenses this file to you under the Apache License, version
 *   2.0 (the "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *   implied.  See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package io.reactiverse.vertx.maven.plugin.components.impl;

import com.google.common.base.Joiner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
class GroovyExtensionCombiner {

    private GroovyExtensionCombiner() {
        // avoid direct instantiation.
    }

    static List<String> merge(String projectName, String projectVersion,
                              List<String> local, List<List<String>> deps) {
        List<String> extensionClassesList = new ArrayList<>();
        List<String> staticExtensionClassesList = new ArrayList<>();

        List<Properties> all = new ArrayList<>();
        all.add(asProperties(local));
        if (deps != null) {
            deps.forEach(s -> all.add(asProperties(s)));
        }

        for (Properties properties : all) {
            String staticExtensionClasses = properties.getProperty("staticExtensionClasses", "").trim();
            String extensionClasses = properties.getProperty("extensionClasses", "").trim();
            if (extensionClasses.length() > 0) {
                append(extensionClasses, extensionClassesList);
            }
            if (staticExtensionClasses.length() > 0) {
                append(staticExtensionClasses, staticExtensionClassesList);
            }
        }

        List<String> desc = new ArrayList<>();
        desc.add("moduleName=" + projectName);
        desc.add("moduleVersion=" + projectVersion);
        if (! extensionClassesList.isEmpty()) {
            desc.add("extensionClasses=" + join(extensionClassesList));
        }
        if (! staticExtensionClassesList.isEmpty()) {
            desc.add("staticExtensionClasses=" + join(staticExtensionClassesList));
        }

        return desc;
    }

    private static void append(String entry, List<String> list) {
        if (entry != null) {
            Collections.addAll(list, entry.split("\\s*,\\s*"));
        }
    }

    private static String join(List<String> strings) {
        return Joiner.on(",").join(strings);
    }

    private static Properties asProperties(List<String> lines) {
        byte[] content = read(lines);
        Properties properties = new Properties();

        if (content.length != 0) {
            try (ByteArrayInputStream stream = new ByteArrayInputStream(content)) {
                properties.load(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }

    private static byte[] read(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new byte[0];
        }
        StringBuilder buffer = new StringBuilder();
        for (String l : lines) {
            buffer.append(l).append("\n");
        }
        return buffer.toString().getBytes();
    }
}
