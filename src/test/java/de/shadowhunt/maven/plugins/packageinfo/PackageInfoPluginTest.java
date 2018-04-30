/**
 * Maven package-info.java Plugin - Autogenerates package-info.java files with arbitrary headers
 * Copyright © ${project.inceptionYear} shadowhunt (dev@shadowhunt.de)
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
    public void containsFilesTest() throws IOException {
        Assert.assertFalse("null must not contain files", PackageInfoPlugin.containsFiles(null, PackageInfoPlugin.JAVA_FILTER));
        Assert.assertFalse("empty must not contain files", PackageInfoPlugin.containsFiles(new File[0], PackageInfoPlugin.JAVA_FILTER));

        final File classFile = temporaryFolder.newFile("a.class");
        Assert.assertFalse("class must not contain files", PackageInfoPlugin.containsFiles(new File[] { classFile }, PackageInfoPlugin.JAVA_FILTER));

        final File javaFile = temporaryFolder.newFile("b.java");
        Assert.assertTrue("java must contain files", PackageInfoPlugin.containsFiles(new File[] { javaFile }, PackageInfoPlugin.JAVA_FILTER));
    }

    @Test(expected = IOException.class)
    public void createNecessaryDirectoriesExceptionTest() throws IOException {
        final File parent = temporaryFolder.newFile("a");
        final File file = new File(parent, "b");
        PackageInfoPlugin.createNecessaryDirectories(file);
    }

    @Test
    public void createNecessaryDirectoriesTest() throws IOException {
        { // existing folder
            final File folder = temporaryFolder.getRoot();
            Assert.assertTrue("folder exists", folder.isDirectory());
            PackageInfoPlugin.createNecessaryDirectories(folder);
            Assert.assertTrue("folder exists", folder.isDirectory());
        }

        { // newly created folder
            final File folder = new File(temporaryFolder.getRoot(), "a/b/c/d");
            Assert.assertFalse("folder doesn't exists", folder.isDirectory());
            PackageInfoPlugin.createNecessaryDirectories(folder);
            Assert.assertTrue("folder exists", folder.getParentFile().isDirectory());
        }
    }

    @Test
    public void doesFileAlreadyExistInSourceRootsTest() throws IOException {
        final PackageInfoPlugin plugin = new PackageInfoPlugin();
        final List<String> compileSourceRoots = Arrays.asList(temporaryFolder.getRoot().getPath());
        plugin.setCompileSourceRoots(compileSourceRoots);

        temporaryFolder.newFolder("net", "example");
        temporaryFolder.newFile("net/example/package-info.java");
        Assert.assertTrue("package-info.java must exists", plugin.doesFileAlreadyExistInSourceRoots("net/example/package-info.java"));

        Assert.assertFalse("\"package-info.java must not exists\"", plugin.doesFileAlreadyExistInSourceRoots("net/example/foo/package-info.java"));
    }

    @Test
    public void generateDefaultPackageInfoTest() throws IOException {
        final PackageInfoPlugin plugin = new PackageInfoPlugin();

        final String annotation = "test";
        final PackageConfiguration configuration = new PackageConfiguration();
        configuration.setAnnotations(Arrays.asList(annotation));

        final File source = temporaryFolder.newFolder("source");
        final File output = temporaryFolder.newFolder("output");
        plugin.setOutputDirectory(output);
        plugin.setCompileSourceRoots(Arrays.asList(source.getPath()));
        plugin.setPackages(Arrays.asList(new PackageConfiguration()));

        { // default package
            final File defaultPackageInfo = new File(output, "package-info.java");
            plugin.generateDefaultPackageInfo(""); // default package
            Assert.assertFalse("no package-info.java for default package", defaultPackageInfo.isFile());
        }

        { // existing
            final File targetPackageInfo = new File(output, "package-info.java");
            temporaryFolder.newFolder("source", "net", "example");
            temporaryFolder.newFile("source/net/example/package-info.java");

            plugin.generateDefaultPackageInfo("net/example");
            Assert.assertFalse("no package-info.java for net/example package", targetPackageInfo.isFile());
        }

        {
            final File targetPackageInfo = new File(output, "net/example/foo/package-info.java");

            Assert.assertFalse("no package-info.java for default package", targetPackageInfo.isFile());
            plugin.generateDefaultPackageInfo("net/example/foo");
            Assert.assertTrue("package-info.java for default package", targetPackageInfo.isFile());
        }
    }

    @Test
    public void isEmptyArrayTest() {
        Assert.assertTrue("null array is empty", PackageInfoPlugin.isEmpty((File[]) null));
        Assert.assertTrue("empty array is empty", PackageInfoPlugin.isEmpty(new File[0]));
        Assert.assertFalse("array is not empty", PackageInfoPlugin.isEmpty(new File[1]));
    }

    @Test
    public void isEmptyListTest() {
        Assert.assertTrue("null List is empty", PackageInfoPlugin.isEmpty((List<?>) null));
        Assert.assertTrue("empty List is empty", PackageInfoPlugin.isEmpty(new ArrayList<Object>()));
        final List<Object> list = new ArrayList<Object>();
        list.add(new Object());
        Assert.assertFalse("List is not empty", PackageInfoPlugin.isEmpty(list));
    }

    @Test
    public void javaFilterTest() throws IOException {
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
    public void makeFileAbsoluteTest() throws IOException {
        final PackageInfoPlugin plugin = new PackageInfoPlugin();
        final MavenProject project = Mockito.mock(MavenProject.class);
        Mockito.when(project.getBasedir()).thenReturn(temporaryFolder.getRoot());
        plugin.setProject(project);

        final File expected = new File(temporaryFolder.getRoot(), "src/main/java/net/example/a.java");
        Assert.assertEquals("files must match", expected, plugin.makeFileAbsolute(new File("src/main/java/net/example/a.java")));
    }

    @Test
    public void path2PackageNameTest() {
        Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("/de/shadowhunt/maven/"));
        Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("de/shadowhunt/maven/"));
        Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("/de/shadowhunt/maven"));
        Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("de/shadowhunt/maven"));
        Assert.assertEquals("", PackageInfoPlugin.path2PackageName(""));
    }

    @Test
    public void toRelativePathTest() {
        final File root = new File("/root/");
        Assert.assertEquals("de/shadowhunt/maven", PackageInfoPlugin.toRelativePath(root, new File("/root/de/shadowhunt/maven/")));
        Assert.assertEquals("", PackageInfoPlugin.toRelativePath(root, root));
    }
}
