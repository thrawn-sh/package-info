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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "package-info", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
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
		final String rootPath = root.getAbsolutePath() + "/";
		final String filePath = file.getAbsolutePath();
		return StringUtils.remove(filePath, rootPath);
	}

	/**
	 * Annotations that are placed into each generated package-info.java file
	 */
	@Parameter(readonly = true)
	private List<String> annotationLines;

	/**
	 * The source directories containing the sources to be compiled.
	 */
	@Parameter(defaultValue = "${project.compileSourceRoots}", required = true, readonly = true)
	private List<String> compileSourceRoots;

	/**
	 * Specify where to place generated package-info.java files.
	 */
	@Parameter(defaultValue = "${project.build.directory}/generated-sources/package-info", required = true, readonly = true)
	private File outputDirectory;

	/**
	 * The project currently being built.
	 */
	@Component
	protected MavenProject project;

	boolean doesFileAlreadyExistinSourceRoots(final String filename) {
		for (final String compileSourceRoot : compileSourceRoots) {
			final File file = new File(compileSourceRoot + filename);
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
				log.debug("checking " + compileSourceRoot + " for missing package-info.java files");
				final File root = new File(compileSourceRoot);
				processFolder(root, root);
			}
		} catch (final IOException e) {
			throw new MojoExecutionException("could not generate package-info.java", e);
		}

		final String outputPath = outputDirectory.getAbsolutePath();
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
		if (doesFileAlreadyExistinSourceRoots(filename)) {
			// don't generate file in outputDirectory if it already exists in one of the compileSourceRoots
			return;
		}

		final File packageInfo = new File(outputDirectory, filename);
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
}
