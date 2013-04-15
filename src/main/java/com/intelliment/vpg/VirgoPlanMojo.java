package com.intelliment.vpg;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;

/**
 * 
 * @author Eduardo Fernández León <efernandez@intellimentsec.com>
 * 
 */
@Mojo(name = "generate-plan", defaultPhase = LifecyclePhase.PACKAGE)
public class VirgoPlanMojo extends AbstractMojo {
	
	/**
	 * Location of the libs
	 */
	@Parameter(defaultValue = "libs", property = "libsDirectory", required = false)
	private File libsDirectory;
	
	/**
	 * Name of the PLAN
	 */
	@Parameter(defaultValue = "${project.name}", property = "name", required = false)
	private String name;
	
	/**
	 * Version of the PLAN
	 */
	@Parameter(defaultValue = "${project.version}", property = "version", required = false)
	private String version;
	
	/**
	 * Indicates if the PLAN will be scoped
	 */
	@Parameter(defaultValue = "false", property = "scoped", required = true)
	private Boolean scoped;
	
	/**
	 * Indicates if the PLAN will be atomic
	 */
	@Parameter(defaultValue = "true", property = "atomic", required = true)
	private Boolean atomic;
	
	/**
	 * Generates the plan file according the input parameters
	 */
	@Override
	public void execute() throws MojoExecutionException {
		
		if (!libsDirectory.exists() || !libsDirectory.isDirectory()) {
			throw new MojoExecutionException("The directory " + libsDirectory.getPath() + " doesn't exist");
		}
		
		FileWriter fw = null;
		try {
			fw = createFileWriter();
			writeHeader(fw);
			for (File file : libsDirectory.listFiles(new JarFilenameFilter())) {
				Manifest manifest = getManifest(file);
				if (manifest != null) {
					String bundleName = getSymbolicName(manifest);
					String bundleVersion = getVersion(manifest);
					if (bundleName == null || bundleVersion == null) {
						getLog().warn("Name or version is null in file " + file.getName());
					} else {
						writeBundle(fw, bundleName, bundleVersion);
					}
				} else {
					getLog().warn("The file " + file.getName() + " does not contain MANIFEST.MF");
				}
			}
			
			writeFooter(fw);
		} catch (IOException e) {
			throw new MojoExecutionException("IO error writing the plan. Exception message : " + e.getMessage());
		} finally {
			IOUtil.close(fw);
		}
		
	}
	
	/**
	 * Creates the plan file
	 * 
	 * @return
	 * @throws IOException
	 */
	private FileWriter createFileWriter() throws IOException {
		String filename = name + "-" + version + ".plan";
		File planFile = new File(libsDirectory, filename);
		FileWriter fw = new FileWriter(planFile);
		return fw;
	}
	
	private void writeFooter(FileWriter plan) throws IOException {
		plan.append("</plan>");
	}
	
	private void writeBundle(FileWriter plan, String name, String version) throws IOException {
		int maxversion = getMaxVersion(version);
		String result =
		        String.format("\t<artifact type=\"bundle\" name=\"%s\" version=\"[%s, %d)\" />\n", name, version,
		                maxversion);
		plan.append(result);
	}
	
	private int getMaxVersion(String version) {
		int max;
		String maxversion = version.split("\\.")[0];
		if (maxversion != null) {
			max = Integer.parseInt(maxversion) + 1;
		} else {
			max = Integer.parseInt(version);
		}
		
		return max;
	}
	
	private void writeHeader(FileWriter plan) throws IOException {
		String header =
		        String.format("<plan name=\"%s\" version=\"%s\" scoped=\"%s\" atomic=\"%s\"\n", name, version, scoped,
		                atomic);
		plan.append(header);
		plan.append("\txmlns=\"http://www.eclipse.org/virgo/schema/plan\"\n");
		plan.append("\t\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		plan.append("\t\txsi:schemaLocation=\"\n");
		plan.append("\t\t\thttp://www.eclipse.org/virgo/schema/plan\"\n");
		plan.append("\t\t\thttp://www.eclipse.org/virgo/schema/plan/eclipse-virgo-plan.xsd\">\"\n\n");
	}
	
	/**
	 * Returns the bundle symbolic name. This method takes into account the
	 * possible extra info in the same line of the bundle symbolic name, eg
	 * ";singleton=true"
	 * 
	 * @param manifest
	 * @return
	 */
	private String getSymbolicName(Manifest manifest) {
		String name = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
		return name != null ? name.split(";")[0] : null;
	}
	
	/**
	 * Returns the bundle version
	 * 
	 * @param manifest
	 * @return
	 */
	private String getVersion(Manifest manifest) {
		return manifest.getMainAttributes().getValue("Bundle-Version");
	}
	
	/**
	 * Returns the manifest file associated with the jar file
	 * 
	 * @param file
	 * @return
	 * @throws MojoExecutionException
	 */
	private Manifest getManifest(File file) throws MojoExecutionException {
		JarFile jar = null;
		try {
			jar = new JarFile(file);
			return jar.getManifest();
		} catch (IOException e) {
			throw new MojoExecutionException("Error reading the jar " + file.getName());
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}
	
	/**
	 * Inner class to filter files accepting only JAR files.
	 * 
	 * @author Eduardo Fernández León <efernandez@intellimentsec.com>
	 * 
	 */
	class JarFilenameFilter implements FilenameFilter {
		
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
		
	}
}
