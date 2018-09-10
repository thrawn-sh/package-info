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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class PackageInfoPluginTest {

    private static String getContent(final File file) throws IOException {
        final Path path = file.toPath();
        try (InputStream input = Files.newInputStream(path)) {
            return IOUtils.toString(input, "UTF-8");
        }
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void containsFilesTest() throws Exception {
        Assert.assertFalse("empty must not contain files", PackageInfoPlugin.containsFiles(PackageInfoPlugin.JAVA_FILTER, new File[0]));

        final File classFile = temporaryFolder.newFile("a.class");
        Assert.assertFalse("class must not contain files", PackageInfoPlugin.containsFiles(PackageInfoPlugin.JAVA_FILTER, new File[] { classFile }));

        final File javaFile = temporaryFolder.newFile("b.java");
        Assert.assertTrue("java must contain files", PackageInfoPlugin.containsFiles(PackageInfoPlugin.JAVA_FILTER, new File[] { javaFile }));
    }

    @Test(expected = IOException.class)
    public void createNecessaryDirectoriesExceptionTest() throws Exception {
        final File parent = temporaryFolder.newFile("a");
        final File file = new File(parent, "b");
        PackageInfoPlugin.createNecessaryDirectories(file);
    }

    @Test
    public void createNecessaryDirectoriesTest() throws Exception {
        // existing folder
        final File exisitingFolder = temporaryFolder.getRoot();
        Assert.assertTrue("folder exists", exisitingFolder.isDirectory());
        PackageInfoPlugin.createNecessaryDirectories(exisitingFolder);
        Assert.assertTrue("folder exists", exisitingFolder.isDirectory());

        // newly created folder
        final File folder = new File(temporaryFolder.getRoot(), "a/b/c/d");
        Assert.assertFalse("folder doesn't exists", folder.isDirectory());
        PackageInfoPlugin.createNecessaryDirectories(folder);
        Assert.assertTrue("folder exists", folder.getParentFile().isDirectory());
    }

    @Test
    public void doesFileAlreadyExistInSourceRootsTest() throws Exception {
        final File root = temporaryFolder.getRoot();
        final List<String> compileSourceRoots = Arrays.asList(root.getPath());

        temporaryFolder.newFolder("net", "example");
        temporaryFolder.newFile("net/example/package-info.java");
        Assert.assertTrue("package-info.java must exists", PackageInfoPlugin.doesFileAlreadyExistInSourceRoots("net/example/package-info.java", root, compileSourceRoots));

        Assert.assertFalse("package-info.java must not exists", PackageInfoPlugin.doesFileAlreadyExistInSourceRoots("net/example/foo/package-info.java", root, compileSourceRoots));
    }

    @Test
    public void executeEmptyTest() throws Exception {
        final PackageInfoPlugin plugin = new PackageInfoPlugin();
        plugin.execute();
    }

    @Test(expected = MojoExecutionException.class)
    public void executeExceptionTest() throws Exception {
        final MavenProject projectMock = Mockito.mock(MavenProject.class);
        Mockito.when(projectMock.getBasedir()).thenThrow(IOException.class);

        final PackageConfiguration configuration = new PackageConfiguration();
        final List<String> annotations = Arrays.asList("test");
        configuration.setAnnotations(annotations);
        final List<PackageConfiguration> configurations = Arrays.asList(configuration);

        final PackageInfoPlugin plugin = new PackageInfoPlugin();
        plugin.setProject(projectMock);
        plugin.setPackages(configurations);
        plugin.execute();
    }

    @Test
    public void executeNonMatchingTest() throws Exception {
        final File root = temporaryFolder.getRoot();

        final MavenProject projectMock = Mockito.mock(MavenProject.class);
        Mockito.when(projectMock.getBasedir()).thenReturn(root);

        final PackageConfiguration configuration = new PackageConfiguration();
        final List<String> configurationAnnotations = Arrays.asList("// other");
        configuration.setAnnotations(configurationAnnotations);
        configuration.setRegex("net.other.*");
        final List<PackageConfiguration> configurations = Arrays.asList(configuration);

        final File source = temporaryFolder.newFolder("source");
        final String sourcePath = source.getPath();
        final List<String> sources = Arrays.asList(sourcePath);

        final File output = temporaryFolder.newFolder("output");

        final PackageInfoPlugin plugin = new PackageInfoPlugin();
        plugin.setProject(projectMock);
        plugin.setEncoding("UTF-8");
        plugin.setOutputDirectory(output);
        plugin.setCompileSourceRoots(sources);
        plugin.setPackages(configurations);

        temporaryFolder.newFolder("source", "net", "example", "missing");
        temporaryFolder.newFile("source/net/example/missing/Test.java");

        plugin.execute();

        Assert.assertFalse("pattern does not match => no file", new File(output, "net/example/missing/package-info.java").exists());
    }

    @Test
    public void executeTest() throws Exception {
        final File root = temporaryFolder.getRoot();

        final MavenProject projectMock = Mockito.mock(MavenProject.class);
        Mockito.when(projectMock.getBasedir()).thenReturn(root);

        final PackageConfiguration otherConfiguration = new PackageConfiguration();
        final List<String> otherConfigurationAnnotations = Arrays.asList("// other");
        otherConfiguration.setAnnotations(otherConfigurationAnnotations);
        otherConfiguration.setRegex("net.other.*");
        final PackageConfiguration exampleConfiguration = new PackageConfiguration();
        final List<String> exampleConfigurationAnnotations = Arrays.asList("// example");
        exampleConfiguration.setAnnotations(exampleConfigurationAnnotations);
        exampleConfiguration.setRegex("net.example.*");
        final PackageConfiguration defaultConfiguration = new PackageConfiguration();
        final List<String> defaultConfigurationAnnotations = Arrays.asList("// default");
        defaultConfiguration.setAnnotations(defaultConfigurationAnnotations);
        final List<PackageConfiguration> configurations = Arrays.asList(otherConfiguration, exampleConfiguration, defaultConfiguration);

        final File source = temporaryFolder.newFolder("source");
        final String sourcePath = source.getPath();
        final List<String> sources = Arrays.asList(sourcePath);

        final File output = temporaryFolder.newFolder("output");

        final PackageInfoPlugin plugin = new PackageInfoPlugin();
        plugin.setProject(projectMock);
        plugin.setEncoding("UTF-8");
        plugin.setOutputDirectory(output);
        plugin.setCompileSourceRoots(sources);
        plugin.setPackages(configurations);

        // default package
        temporaryFolder.newFile("source/Test.java");
        // package with existing package-info.java
        temporaryFolder.newFolder("source", "net", "example", "exisiting");
        temporaryFolder.newFile("source/net/example/exisiting/Test.java");
        temporaryFolder.newFile("source/net/example/exisiting/package-info.java");
        // package with missing package-info.java
        temporaryFolder.newFolder("source", "net", "example", "missing");
        temporaryFolder.newFile("source/net/example/missing/Test.java");

        plugin.execute();

        Assert.assertFalse("default package can not have package-info.java", new File(output, "package-info.java").exists());
        Assert.assertFalse("do not generate package-info.java when package-info.java already exists", new File(output, "net/example/exisiting/package-info.java").exists());
        final StringBuilder expected = new StringBuilder();
        expected.append("// example");
        expected.append(IOUtils.LINE_SEPARATOR);
        expected.append("package net.example.missing;");
        expected.append(IOUtils.LINE_SEPARATOR);
        expected.append(IOUtils.LINE_SEPARATOR);

        Assert.assertEquals("content must match", expected.toString(), getContent(new File(output, "net/example/missing/package-info.java")));
    }

    @Test
    public void javaFilterTest() throws Exception {
        final File javaFile = temporaryFolder.newFile("a.java");
        Assert.assertTrue("JAVA_FILTER must accept a.java", PackageInfoPlugin.JAVA_FILTER.accept(javaFile));

        final File JAVAFile = temporaryFolder.newFile("b.JAVA");
        Assert.assertTrue("JAVA_FILTER must accept b.JAVA", PackageInfoPlugin.JAVA_FILTER.accept(JAVAFile));

        final File JaVaFile = temporaryFolder.newFile("c.JaVa");
        Assert.assertTrue("JAVA_FILTER must accept c.JaVa", PackageInfoPlugin.JAVA_FILTER.accept(JaVaFile));

        final File classFile = temporaryFolder.newFile("d.class");
        Assert.assertFalse("JAVA_FILTER must not accept d.class", PackageInfoPlugin.JAVA_FILTER.accept(classFile));

        final File folder = temporaryFolder.newFolder("folder");
        Assert.assertFalse("JAVA_FILTER must not accept a folder", PackageInfoPlugin.JAVA_FILTER.accept(folder));
    }

    @Test
    public void makeFileAbsoluteTest() throws Exception {
        final File root = temporaryFolder.getRoot();
        final File expected = new File(root, "src/main/java/net/example/a.java");
        final File file = new File("src/main/java/net/example/a.java");
        Assert.assertEquals("files must match", expected, PackageInfoPlugin.makeFileAbsolute(root, file));
    }

    @Test
    public void path2PackageNameTest() throws Exception {
        Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("/de/shadowhunt/maven/"));
        Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("de/shadowhunt/maven/"));
        Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("/de/shadowhunt/maven"));
        Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("de/shadowhunt/maven"));
        Assert.assertEquals("", PackageInfoPlugin.path2PackageName(""));
    }

    @Test
    public void toRelativePathTest() throws Exception {
        final File root = new File("/root/");
        Assert.assertEquals("de/shadowhunt/maven", PackageInfoPlugin.toRelativePath(root, new File("/root/de/shadowhunt/maven/")));
        Assert.assertEquals("", PackageInfoPlugin.toRelativePath(root, root));
    }
}
