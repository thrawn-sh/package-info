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
    private List<String> annotations = new ArrayList<String>();

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
        for (final String annotation : annotations) {
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
