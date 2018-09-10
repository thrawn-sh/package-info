/**
 * Maven package-info.java Plugin - Autogenerates package-info.java files with arbitrary headers
 * Copyright © 2012-2018 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.maven.plugins.packageinfo;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackageConfiguration {

    public static final String DEFAULT_REGEX = ".*";

    /**
     * Annotations that are placed into each generated package-info.java file.
     */
    private List<String> annotations = new ArrayList<>();

    private Pattern pattern;

    /**
     * Pattern compared against
     */
    private String regex = DEFAULT_REGEX;

    public List<String> getAnnotations() {
        return annotations;
    }

    public String getRegex() {
        return regex;
    }

    public boolean matches(final String packageName) {
        if (pattern == null) {
            pattern = Pattern.compile(regex);
        }
        final Matcher matcher = pattern.matcher(packageName);
        return matcher.matches();
    }

    public void printAnnotions(final PrintWriter pw) {
        for (final String annotation : getAnnotations()) {
            pw.println(annotation);
        }
    }

    public void setAnnotations(final List<String> annotations) {
        this.annotations = annotations;
    }

    public void setRegex(final String regex) {
        this.regex = regex;
    }
}
