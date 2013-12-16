/*
 * #%L
 * Maven package-info.java Plugin
 * %%
 * Copyright (C) 2012 - 2013 shadowhunt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
	public void toRelativePathTest() {
		final File root = new File("/root/");
		Assert.assertEquals("de/shadowhunt/maven", PackageInfoPlugin.toRelativePath(root, new File("/root/de/shadowhunt/maven/")));
		Assert.assertEquals("", PackageInfoPlugin.toRelativePath(root, root));
	}

	@Test
	public void path2PackageNameTest() {
		Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("/de/shadowhunt/maven/"));
		Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("de/shadowhunt/maven/"));
		Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("/de/shadowhunt/maven"));
		Assert.assertEquals("de.shadowhunt.maven", PackageInfoPlugin.path2PackageName("de/shadowhunt/maven"));
		Assert.assertEquals("", PackageInfoPlugin.path2PackageName(""));
	}

	//	@Test
	//	public void javaFilterTest() {
	//		Assert.assertTrue(PackageInfoPlugin.JAVA_FILTER.accept(new File("a.java")));
	//		Assert.assertTrue(PackageInfoPlugin.JAVA_FILTER.accept(new File("a.JAVA")));
	//		Assert.assertTrue(PackageInfoPlugin.JAVA_FILTER.accept(new File("a.JaAa")));
	//		Assert.assertFalse(PackageInfoPlugin.JAVA_FILTER.accept(new File("a.class")));
	//	}
}
