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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class PackageInfoPluginTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void containsFilesTest() throws Exception {
        Assert.assertFalse("null must not contain files", PackageInfoPlugin.containsFiles(null, PackageInfoPlugin.JAVA_FILTER));
        Assert.assertFalse("empty must not contain files", PackageInfoPlugin.containsFiles(new File[0], PackageInfoPlugin.JAVA_FILTER));

        final File classFile = temporaryFolder.newFile("a.class");
        Assert.assertFalse("class must not contain files", PackageInfoPlugin.containsFiles(new File[] { classFile }, PackageInfoPlugin.JAVA_FILTER));

        final File javaFile = temporaryFolder.newFile("b.java");
        Assert.assertTrue("java must contain files", PackageInfoPlugin.containsFiles(new File[] { javaFile }, PackageInfoPlugin.JAVA_FILTER));
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
        final PackageConfiguration configuration = new PackageConfiguration();
        final List<String> annotations = Arrays.asList("test");
        configuration.setAnnotations(annotations);
        final List<PackageConfiguration> configurations = Arrays.asList(configuration);

        final PackageInfoPlugin plugin = new PackageInfoPlugin();
        plugin.setPackages(configurations);
        plugin.execute();
    }

    @Test
    public void executeTest() throws Exception {
        final File root = temporaryFolder.getRoot();

        final MavenProject projectMock = Mockito.mock(MavenProject.class);
        Mockito.when(projectMock.getBasedir()).thenReturn(root);

        final PackageConfiguration configuration = new PackageConfiguration();
        final List<String> annotations = Arrays.asList("test");
        configuration.setAnnotations(annotations);
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
        // FIXME TODO
    }

    @Test
    public void isEmptyArrayTest() throws Exception {
        Assert.assertTrue("null array is empty", PackageInfoPlugin.isEmpty((File[]) null));
        Assert.assertTrue("empty array is empty", PackageInfoPlugin.isEmpty(new File[0]));
        Assert.assertFalse("array is not empty", PackageInfoPlugin.isEmpty(new File[1]));
    }

    @Test
    public void isEmptyListTest() throws Exception {
        Assert.assertTrue("null List is empty", PackageInfoPlugin.isEmpty((List<?>) null));
        Assert.assertTrue("empty List is empty", PackageInfoPlugin.isEmpty(new ArrayList<>()));
        final List<Object> list = Arrays.asList(new Object());
        Assert.assertFalse("List is not empty", PackageInfoPlugin.isEmpty(list));
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
    public void toEmptyArrayTest() throws Exception {
        final File[] empty = new File[0];
        Assert.assertArrayEquals("array must match", empty, PackageInfoPlugin.toEmpty(null));
        Assert.assertSame("array must be same", empty, PackageInfoPlugin.toEmpty(empty));
        final File[] files = new File[1];
        Assert.assertSame("array must be same", files, PackageInfoPlugin.toEmpty(files));
    }

    @Test
    public void toRelativePathTest() throws Exception {
        final File root = new File("/root/");
        Assert.assertEquals("de/shadowhunt/maven", PackageInfoPlugin.toRelativePath(root, new File("/root/de/shadowhunt/maven/")));
        Assert.assertEquals("", PackageInfoPlugin.toRelativePath(root, root));
    }
}
