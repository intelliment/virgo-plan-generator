Virgo Plan Generator
====================

Maven plugin for Virgo Plan files generator. This plugin uses a directory container with JARs files to generate the Plan.

The parameters of this plugin are:

* libsDirectory - Directory that contains the JARs. The default value is "libs". 
* name - Name of the Plan file. The default value is the project name.
* version - Version of the Plan file. The default value is the project version.
* scoped - Indicates if the plan will be scoped (true or false). The default value is false.
* atomic - Indicates if the plan will be atomic (true or false). The default value is true.
* order - Comma separated bundle symbolic name in the desired order in the plan. This parameter is optional.
* exclude - Comma separated bundle symbolic name that we want to avoid to put in the plan. This parameter is optional.

The generated plan will be contain the list of entries for each _OSGi-fied_ JAR with _type=bundle_

Example of configuration:

		<groupId>com.intelliment.example</groupId>
		<artifactId>libs</artifactId>
		<version>0.1.0-SNAPSHOT</version>
		<packaging>pom</packaging>
		
		<build>
			<plugins>
				<plugin>
					<groupId>org.apache.maven.plugins</groupId>
					<artifactId>maven-dependency-plugin</artifactId>
					<version>2.3</version>
					<executions>
						<execution>
							<id>copy-dependencies</id>
							<phase>process-resources</phase>
							<goals>
								<goal>copy-dependencies</goal>
							</goals>
							<configuration>
								<outputDirectory>${project.basedir}/jars</outputDirectory>
								<overWriteReleases>true</overWriteReleases>
								<overWriteSnapshots>true</overWriteSnapshots>
								<overWriteIfNewer>true</overWriteIfNewer>
							</configuration>
						</execution>
					</executions>
				</plugin>
				<plugin>
					<groupId>com.intelliment</groupId>
					<artifactId>virgo-plan-generator</artifactId>
					<version>0.1-SNAPSHOT</version>
					<executions>
						<execution>
							<goals>
								<goal>generate-plan</goal>
							</goals>
							<configuration>
								<name>example-plan</name>
								<version>1.0.0</version>
								<libsDirectory>${project.basedir}/jars</libsDirectory>
								<order>bundle1, bundle2, bundle3</order>
								<exclude>bundle4</exclude>
							</configuration>
						</execution>
					</executions>
				</plugin>
			</plugins>
		</build>
		
		<dependencies>
			 ....
		</dependencies>
		
This pom.xml uses the maven-dependency-plugin to resolve the dependencies and copy them into _jars_ directory, then it uses 
the virgo-plan-generator plugin with this directory and generates the plan file with name example.  