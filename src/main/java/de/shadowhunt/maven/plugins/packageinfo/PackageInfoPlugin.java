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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Generate package-info.java for each package that doesn't already contain one.<br/>
 * Call <code>mvn package-info:package-info</code> to generate missing package-info.java.
 */
@Mojo(name = "package-info", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true, threadSafe = true)
public class PackageInfoPlugin extends AbstractMojo {

    static final FileFilter JAVA_FILTER = file -> {
        if (!file.isFile()) {
            return false;
        }
        final String name = file.getName();
        final String lowerCaseName = name.toLowerCase(Locale.ENGLISH);
        return lowerCaseName.endsWith(".java");
    };

    static boolean containsFiles(final FileFilter filter, final File... files) {
        for (final File file : files) {
            if (filter.accept(file)) {
                return true;
            }
        }
        return false;
    }

    static void createNecessaryDirectories(final File file) throws IOException {
        final File parent = file.getParentFile();
        if (parent.isDirectory()) {
            return;
        }

        final boolean createdDirs = parent.mkdirs();
        if (!createdDirs) {
            throw new IOException("could not create all necessary but nonexistent parent directories for " + file);
        }
    }

    static boolean doesFileAlreadyExistInSourceRoots(final String filename, final File base, final List<String> compileSourceFolders) {
        for (final String compileSourceFolder : compileSourceFolders) {
            final File folder = new File(compileSourceFolder);
            final File root = makeFileAbsolute(base, folder);
            final File file = new File(root, filename);
            if (file.isFile()) {
                return true;
            }
        }
        return false;
    }

    static final File makeFileAbsolute(final File base, final File file) {
        if (file.isAbsolute()) {
            return file;
        }

        final String path = file.getPath();
        return new File(base, path);
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
     * The source directories containing the sources to be checked for missing package-info.java.
     */
    @Parameter(defaultValue = "${project.compileSourceRoots}", required = true, readonly = true)
    private List<String> compileSourceRoots;

    /**
     * Encoding for the generated package-info.java files.
     */
    @Parameter(defaultValue = "${project.build.sourceEncoding}", required = true, readonly = true)
    private String encoding;

    /**
     * Specify where to place generated package-info.java files.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/package-info", required = true)
    private File outputDirectory;

    /**
     * Package configuration
     */
    @Parameter
    private List<PackageConfiguration> packages;

    /**
     * The project currently being built.
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Log log = getLog();

        try {
            if ((packages == null) || packages.isEmpty()) {
                log.warn("no packages give: not generating any package-info.java files");
                return;
            }

            final File base = project.getBasedir();
            for (final String compileSourceFolder : compileSourceRoots) {
                final File folder = new File(compileSourceFolder);
                final File root = makeFileAbsolute(base, folder);
                log.debug("checking " + root + " for missing package-info.java files");
                processFolder(root, root);
            }

            final File absoluteOutputDirectory = makeFileAbsolute(base, outputDirectory);
            final String outputPath = absoluteOutputDirectory.getAbsolutePath();
            project.addCompileSourceRoot(outputPath);
        } catch (final IOException e) {
            throw new MojoExecutionException("could not generate package-info.java", e);
        }
    }

    void generateDefaultPackageInfo(final File base, final String relativePath) throws IOException {
        if (StringUtils.isEmpty(relativePath)) {
            // default package can't have package-info.java
            return;
        }

        final String filename = relativePath + File.separator + "package-info.java";
        if (doesFileAlreadyExistInSourceRoots(filename, base, compileSourceRoots)) {
            // don't generate file in outputDirectory if it already exists in one of the compileSourceRoots
            return;
        }

        final File absoluteOutputDirectory = makeFileAbsolute(base, outputDirectory);
        final File packageInfo = new File(absoluteOutputDirectory, filename);
        createNecessaryDirectories(packageInfo);

        final String packageName = path2PackageName(relativePath);
        for (final PackageConfiguration packageConfiguration : packages) {
            if (packageConfiguration.matches(packageName)) {
                try (Writer packageInfoWriter = new FileWriterWithEncoding(packageInfo, encoding)) {
                    try (PrintWriter pw = new PrintWriter(packageInfoWriter)) {
                        packageConfiguration.printAnnotions(pw);
                        pw.print("package ");
                        pw.print(packageName);
                        pw.println(";");
                        pw.println();
                    }
                }
                return;
            }
        }
    }

    void processFolder(final File folder, final File base) throws IOException {
        if (!folder.isDirectory()) {
            return;
        }

        final File[] children = folder.listFiles();
        if (children == null) {
            return;
        }

        if (containsFiles(JAVA_FILTER, children)) {
            final String relativePath = toRelativePath(base, folder);
            generateDefaultPackageInfo(base, relativePath);
        }

        for (final File child : children) {
            processFolder(child, base);
        }
    }

    public void setCompileSourceRoots(final List<String> compileSourceRoots) {
        this.compileSourceRoots = compileSourceRoots;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public void setOutputDirectory(final File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setPackages(final List<PackageConfiguration> packages) {
        this.packages = packages;
    }

    public void setProject(final MavenProject project) {
        this.project = project;
    }
}
