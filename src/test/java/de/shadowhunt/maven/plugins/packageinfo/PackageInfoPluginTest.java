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
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PackageInfoPluginTest {

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

    //	@Test
    //	public void javaFilterTest() {
    //		Assert.assertTrue(PackageInfoPlugin.JAVA_FILTER.accept(new File("a.java")));
    //		Assert.assertTrue(PackageInfoPlugin.JAVA_FILTER.accept(new File("a.JAVA")));
    //		Assert.assertTrue(PackageInfoPlugin.JAVA_FILTER.accept(new File("a.JaAa")));
    //		Assert.assertFalse(PackageInfoPlugin.JAVA_FILTER.accept(new File("a.class")));
    //	}
}
