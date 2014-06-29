/**
 * This file is part of Maven package-info.java Plugin.
 *
 * Maven package-info.java Plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Maven package-info.java Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Maven package-info.java Plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.maven.plugins.packageinfo;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Generate package-info.java for each package that doesn't already contain one.<br/>
 * Call <code>mvn package-info:package-info</code> to generate missing package-info.java.
 */
@Mojo(name = "package-info", threadSafe = true)
@Execute(phase = LifecyclePhase.GENERATE_SOURCES)
public class PackageInfoPlugin extends AbstractMojo {

    static final FileFilter JAVA_FILTER = new FileFilter() {

        @Override
        public boolean accept(final File file) {
            if (file.isFile()) {
                final String name = file.getName().toLowerCase(Locale.ENGLISH);
                return name.endsWith(".java");
            }
            return false;
        }
    };

    static boolean containsFiles(final File[] files, final FileFilter filter) {
        if (isEmpty(files)) {
            return false;
        }

        for (final File file : files) {
            if (filter.accept(file)) {
                return true;
            }
        }
        return false;
    }

    static void createNecesarryDirectories(final File file) throws IOException {
        final File parent = file.getParentFile();
        if (parent.isDirectory()) {
            return;
        }

        final boolean createdDirs = parent.mkdirs();
        if (!createdDirs) {
            throw new IOException("could not create all necessary but nonexistent parent directories for " + file);
        }
    }

    static boolean isEmpty(final File[] files) {
        return (files == null) || (files.length == 0);
    }

    static boolean isEmpty(final List<?> list) {
        return (list == null) || list.isEmpty();
    }

    static String path2PackageName(final String path) {
        final String strip = StringUtils.strip(path, File.separator);
        return strip.replace(File.separatorChar, '.');
    }

    static String toRelativePath(final File root, final File file) {
        final String rootPath = root.getAbsolutePath();
        final String filePath = file.getAbsolutePath();
        final String withoutPrefix = StringUtils.removeStart(filePath, rootPath);
        return StringUtils.removeStart(withoutPrefix, File.separator);
    }

    /**
     * Annotations that are placed into each generated package-info.java file.
     */
    @Parameter
    private List<String> annotationLines;

    /**
     * The source directories containing the sources to be checked for missing package-info.java.
     */
    @Parameter(defaultValue = "${project.compileSourceRoots}", required = true, readonly = true)
    private List<String> compileSourceRoots;

    /**
     * Specify where to place generated package-info.java files.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/package-info", required = true)
    private File outputDirectory;

    /**
     * The project currently being built.
     */
    @Component
    protected MavenProject project;

    boolean doesFileAlreadyExistInSourceRoots(final String filename) {
        for (final String compileSourceRoot : compileSourceRoots) {
            final File absoluteCompileSourceRootFolder = makeFileAbsolute(new File(compileSourceRoot));
            final File file = new File(absoluteCompileSourceRootFolder, filename);
            if (file.isFile()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Log log = getLog();

        if (isEmpty(annotationLines)) {
            log.warn("no annotationLines give: not generating any package-info.java files");
            return;
        }

        try {
            for (final String compileSourceRoot : compileSourceRoots) {
                final File root = makeFileAbsolute(new File(compileSourceRoot));
                log.debug("checking " + root + " for missing package-info.java files");
                processFolder(root, root);
            }
        } catch (final IOException e) {
            throw new MojoExecutionException("could not generate package-info.java", e);
        }

        final File absoluteOutputDirectory = makeFileAbsolute(outputDirectory);
        final String outputPath = absoluteOutputDirectory.getAbsolutePath();
        if (!project.getCompileSourceRoots().contains(outputPath)) {
            project.addCompileSourceRoot(outputPath);
        }
    }

    void generateDefaultPackageInfo(final String relativePath) throws IOException {
        if (StringUtils.isEmpty(relativePath)) {
            // default package can't have package-info.java
            return;
        }

        final String filename = relativePath + File.separator + "package-info.java";
        if (doesFileAlreadyExistInSourceRoots(filename)) {
            // don't generate file in outputDirectory if it already exists in one of the compileSourceRoots
            return;
        }

        final File absoluteOutputDirectory = makeFileAbsolute(outputDirectory);
        final File packageInfo = new File(absoluteOutputDirectory, filename);
        createNecesarryDirectories(packageInfo);

        final PrintWriter pw = new PrintWriter(packageInfo);
        for (final String annotationLine : annotationLines) {
            pw.println(annotationLine);
        }

        final String packageName = path2PackageName(relativePath);
        pw.print("package ");
        pw.print(packageName);
        pw.println(";");
        pw.println();
        pw.close();
    }

    public List<String> getAnnotationLines() {
        return annotationLines;
    }

    public List<String> getCompileSourceRoots() {
        return compileSourceRoots;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public MavenProject getProject() {
        return project;
    }

    final File makeFileAbsolute(final File file) {
        if (file.isAbsolute()) {
            return file;
        }

        return new File(project.getBasedir(), file.getPath());
    }

    void processFolder(final File file, final File root) throws IOException {
        if (!file.isDirectory()) {
            return;
        }

        final File[] children = file.listFiles();
        if (children != null) {
            if (containsFiles(children, JAVA_FILTER)) {
                final String relativePath = toRelativePath(root, file);
                generateDefaultPackageInfo(relativePath);
            }

            for (final File child : children) {
                processFolder(child, root);
            }
        }
    }

    public void setAnnotationLines(final List<String> annotationLines) {
        this.annotationLines = annotationLines;
    }

    public void setCompileSourceRoots(final List<String> compileSourceRoots) {
        this.compileSourceRoots = compileSourceRoots;
    }

    public void setOutputDirectory(final File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setProject(final MavenProject project) {
        this.project = project;
    }
}
