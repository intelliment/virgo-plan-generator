package com.intelliment.vpg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.jar.Manifest;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * A few tests to check the functionallity of the Mojo
 * 
 * @author Eduardo Fernández León <efernandez@intellimentsec.com>
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class TestVirgoPlanMojo {
	
	@Spy
	private File directory = new File(System.getProperty("java.io.tmpdir"));
	
	@Spy
	@InjectMocks
	private VirgoPlanMojo mojo = new VirgoPlanMojo();
	
	@Test(expected = MojoExecutionException.class)
	public void invalidDirectory() throws MojoExecutionException {
		when(directory.isDirectory()).thenReturn(false);
		when(directory.exists()).thenReturn(true);
		
		mojo.execute();
	}
	
	@Test(expected = MojoExecutionException.class)
	public void unexistsDirectory() throws MojoExecutionException {
		when(directory.isDirectory()).thenReturn(true);
		when(directory.exists()).thenReturn(false);
		
		mojo.execute();
	}
	
	@Test
	public void executeWithoutOrder() throws MojoExecutionException, IOException {
		stubbingManifest();
		
		mojo.setName("test-plan");
		mojo.setVersion("1.0.1");
		mojo.setScoped(true);
		mojo.setAtomic(true);
		
		mojo.execute();
		
		File planFile = new File(directory, "test-plan-1.0.1.plan");
		assertTrue(planFile.exists());
		
		String content = FileUtils.fileRead(planFile);
		assertNotNull(content);
		assertTrue(content.contains("name=\"test-plan\" version=\"1.0.1\" scoped=\"true\" atomic=\"true\""));
		assertTrue(content.contains("<artifact type=\"bundle\" name=\"test-symbolic-name\" version=\"[1.4.2, 2)\" />"));
		assertTrue(content.contains("<artifact type=\"bundle\" name=\"another-bundle-name\" version=\"[2.2, 3)\" />"));
	}
	
	@Test
	public void executeWithOrder() throws MojoExecutionException, IOException {
		stubbingManifest();
		
		mojo.setName("test-plan");
		mojo.setVersion("1.0.1");
		mojo.setScoped(true);
		mojo.setAtomic(true);
		mojo.setOrder("another-bundle-name, test-symbolic-name");
		
		mojo.execute();
		
		File planFile = new File(directory, "test-plan-1.0.1.plan");
		assertTrue(planFile.exists());
		
		String content = FileUtils.fileRead(planFile);
		assertNotNull(content);
		assertTrue(content.contains("name=\"test-plan\" version=\"1.0.1\" scoped=\"true\" atomic=\"true\""));
		assertTrue(content.contains("<artifact type=\"bundle\" name=\"test-symbolic-name\" version=\"[1.4.2, 2)\" />"));
		assertTrue(content.contains("<artifact type=\"bundle\" name=\"another-bundle-name\" version=\"[2.2, 3)\" />"));
		
		assertTrue(content.indexOf("another-bundle-name") < content.indexOf("test-symbolic-name"));
	}
	
	@Test
	public void executeWithExclusions() throws MojoExecutionException, IOException {
		stubbingManifest();
		
		mojo.setName("test-plan");
		mojo.setVersion("1.0.1");
		mojo.setScoped(true);
		mojo.setAtomic(true);
		mojo.setExclude("another-bundle-name");
		
		mojo.execute();
		
		File planFile = new File(directory, "test-plan-1.0.1.plan");
		assertTrue(planFile.exists());
		
		String content = FileUtils.fileRead(planFile);
		assertNotNull(content);
		assertTrue(content.contains("name=\"test-plan\" version=\"1.0.1\" scoped=\"true\" atomic=\"true\""));
		assertTrue(content.contains("<artifact type=\"bundle\" name=\"test-symbolic-name\" version=\"[1.4.2, 2)\" />"));
		assertFalse(content.contains("<artifact type=\"bundle\" name=\"another-bundle-name\" version=\"[2.2, 3)\" />"));
	}
	
	/**
	 * This method emulates that the directory contains files with jars and
	 * valid names and versions
	 * 
	 * @throws MojoExecutionException
	 * 
	 */
	private void stubbingManifest() throws MojoExecutionException {
		String[] files = new String[] { "test1.jar", "test2.jar", "file.txt" };
		doReturn(files).when(directory).list();
		
		Manifest m1 = mock(Manifest.class);
		doReturn("1.4.2").when(mojo).getVersion(eq(m1));
		doReturn("test-symbolic-name").when(mojo).getSymbolicName(eq(m1));
		doReturn(m1).when(mojo).getManifest(eq(new File(directory, "test1.jar")));
		
		Manifest m2 = mock(Manifest.class);
		doReturn("2.2").when(mojo).getVersion(eq(m2));
		doReturn("another-bundle-name").when(mojo).getSymbolicName(eq(m2));
		doReturn(m2).when(mojo).getManifest(eq(new File(directory, "test2.jar")));
		
	}
}
