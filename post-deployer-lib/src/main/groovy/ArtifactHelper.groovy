package com.hagzag.hudson.build.tools


import org.apache.maven.project.DependencyResolutionException
import org.apache.maven.project.MavenProject
import org.apache.maven.repository.internal.MavenRepositorySystemSession
import org.codehaus.plexus.DefaultPlexusContainer
import org.sonatype.aether.RepositorySystem
import org.sonatype.aether.artifact.Artifact
import org.apache.maven.repository.internal.DefaultServiceLocator
import org.sonatype.aether.repository.LocalRepository
import org.sonatype.aether.resolution.ArtifactRequest
import org.sonatype.aether.resolution.ArtifactResult
import org.sonatype.aether.util.artifact.DefaultArtifact
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;

public class ArtifactHelper{
	org.apache.maven.execution.MavenSession session;
	org.apache.maven.project.MavenProject project;
	public ArtifactHelper(org.apache.maven.execution.MavenSession mavenSession,org.apache.maven.project.MavenProject mavenProject){
		session=mavenSession;
		project=mavenProject;
	}
	public String resolvedArtifact(art){
		def afm =session.lookup('org.apache.maven.artifact.handler.manager.ArtifactHandlerManager');
		def resolver = session.lookup('org.apache.maven.artifact.resolver.ArtifactResolver');
		def factory = new org.apache.maven.artifact.factory.DefaultArtifactFactory();
		factory.class.getDeclaredField("artifactHandlerManager").accessible = true;
		factory.artifactHandlerManager=afm;
		def artifact ;
		project.dependencies.each {
			if (art == it.artifactId){
				artifact = factory.createDependencyArtifact(it.groupId,it.artifactId,org.apache.maven.artifact.versioning.VersionRange.createFromVersion(it.version),it.type, it.classifier , it.scope)
				resolver.resolve( artifact, project.remoteArtifactRepositories,	session.localRepository );
			}
		}
		artifact.getFile().getAbsolutePath()
	}
	public Collection<Artifact> getConfigurationPlugins(groupId,version,classifier){
		def repo = session.getLocalRepository().getBasedir();
		DefaultServiceLocator locator = new DefaultServiceLocator();
		locator.setServices( WagonProvider.class, new ManualWagonProvider() );
		locator.addService( RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class );

		RepositorySystem repoSystem=locator.getService( RepositorySystem.class );
		MavenRepositorySystemSession repoSession = new MavenRepositorySystemSession();
		MavenProject localProject =project.getDelegate()
		LocalRepository localRepo = new LocalRepository(repo );
		repoSession.setLocalRepositoryManager(repoSystem.newLocalRepositoryManager( localRepo))
		def remoteRepo = new ArrayList<RemoteRepository>()
		localProject.getRemoteArtifactRepositories().each { remoteRepo.add(new RemoteRepository(it.getId(),"",it.getUrl())) }
		def retVal = new java.util.ArrayList<File>()
		localProject.getParent().getModules().each(){
			try{
				ArtifactRequest request = new ArtifactRequest()
				request.setArtifact( new DefaultArtifact(  groupId,it.toString().subSequence(3,it.toString().length()), classifier, "jar", version) )
				request.setRepositories(remoteRepo);
				ArtifactResult result = repoSystem.resolveArtifact( repoSession, request );
				retVal.add(result.getArtifact().file)
			}catch(org.sonatype.aether.resolution.ArtifactResolutionException de){
			}
		}
		return retVal;
	}
}