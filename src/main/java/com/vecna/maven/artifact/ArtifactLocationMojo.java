/**
 * Copyright 2011 Vecna Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
*/

package com.vecna.maven.artifact;

import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Exports the locations of specified dependency artifacts as project properties.
 * 
 * @author ogolberg@vecna.com
 * @goal execute
 * @requiresDependencyResolution
 */
public class ArtifactLocationMojo extends AbstractMojo {
  /**
   * Maven project. Read-only.
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project; 
  
  /**
   * @component
   */
  private ArtifactResolver resolver;
  
  /**
   * Key = groupId:artifactId, Value = the project property that should be set to the artifact location
   * @parameter
   */
  private Properties artifacts;
  
  /**
   * Location of the local repository.
   *
   * @parameter expression="${localRepository}"
   * @readonly
   * @required
   */
  protected ArtifactRepository local;
    
  /**
   * List of Remote Repositories used by the resolver
   *
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   * @required
   */
  protected List<ArtifactRepository> remoteRepos;   
  
  /**
   * {@inheritDoc}
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    for (Object artifactObj : project.getDependencyArtifacts()) {
      Artifact artifact = (Artifact) artifactObj;
      String key = artifact.getGroupId() + ":" + artifact.getArtifactId();
      String property = artifacts.getProperty(key);
      if (property != null) {
        if (!artifact.isResolved()) {
          getLog().info("artifact " + artifact + " is not resolved yet");
          try {
            resolver.resolve(artifact, remoteRepos, local);
          } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("can't resolve artifact", e);
          } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("can't resolve artifact", e);
          }
        }
        String path = artifact.getFile().getAbsolutePath();
        getLog().info("setting project." + property + " to " + path);
        project.getProperties().setProperty(property, path);
      }
    }
  }
}
