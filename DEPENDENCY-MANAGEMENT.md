# Dependency management in Rapaio-Jupyter-Kernel

Dependency management uses MiMa (Minimal Maven). As such, it is based entirely on Maven, and it follows 
*the Maven way* to interpret and solve dependencies. The usual scenario for Maven happens at build time
through one or multiple pom files. All the dependency information is contained in configuration files
and all that information is used to solve dependencies and to bring and manipulate various artifacts.

This is in total contrast with the interactivity that a Jupyter kernel brings. Code cells can be executed 
in order, sequentially or in any other imaginable order, left to the user's choice. 
In order to adapt the Maven features to the interactive way of using a notebook, some new concepts were 
introduced which will be described in this document.

What we do is to specify dependencies, those dependencies are solved using dependency repositories (which 
can also be added), and from those dependencies some artifacts are obtained (this is called solving dependencies)
and loaded into the jvm process through class path. The only different thing is that you can de that incrementally, 
in an interactive way, according to how jupyter notebooks works.

## What is a repository

A maven repository is a server which contains various packages in some specific format. There is a _local repository_ 
and remote repositories. A local repository is created for each notebook. The local repository acts as a cache for package
metadata and artifacts. If a dependency is requested, Maven asks for that dependency in the local repository. If the 
dependency is not found in the local repository, then it is requested from remote repositories. If there is a remote
repository capable to deliver the dependency, the metadata and artifacts of that dependency is stored in the local
repository for following usages. 

A _remote repository_ is any repository, different from the local repository. Even the name includes the word remote, 
a remote repository does not necessary lies on a remote host. Any local storage which contains a Maven repository
is a remote repository from the notebook's perspective. 

Repositories contains two types of dependencies: releases and snapshots. A release dependency is a dependency designated 
for public consumption, production ready and ready only. Once a package is released, the specific version and it's 
artifacts does not change anymore. A snapshot dependency serves a different purpose. A snapshot is a dependency which 
is in a development cycle and is subject to change. In order to be able to work with such kind of dependencies, the 
artifacts are published as snapshots, and they can be consumed. 

The two types of publishing processes impacts repositories. Some repositories contain only releases,
some other only snapshots, or both releases and snapshots. Local repository contains both type of published artifacts.


When a dependency is requested,
it's metadata, data and dependencies are searched for in the registered remote repositories. By default, the
`rapaio-jupyter-kernel` has the following public IBiblio repositories defined:

        central, https://repo.maven.apache.org/maven2/
        jcenter, https://jcenter.bintray.com/
        jboss, https://repository.jboss.org/nexus/content/repositories/releases/
        atlassian, https://packages.atlassian.com/maven/public

We can always add IBiblio repositories the the default ones, in order to allow the notebook to download artifacts from
other repositories. This is an example of that:

    %dependency /add-repo google https://maven.google.com/

The syntax for adding repositories is more complex and allows one to specify what kind of artifacts it hosts.

    %dependency /add-repo google https://maven.google.com/ release|never snapshot|always

The previous example specifies that the repository contains both release and snapshot artifacts, for release the 
update policy being *never* (once a dependency being resolved, it will not search for new releases), and for snapshot 
the update policy being *always* (always search for new versions of the artifacts). This is for illustrative purposes.

The default values for those values are as follows:

* repositories hosted on local host which has an url starting with `file://` will have release enabled with never, 
snapshot enabled with never
* repositories which have urls not starting with `file://` will have release enabled with never and snapshots disabled

## What is a dependency

A *dependency* is a module which contains various artifacts encapsulating functionality, often times time packaged 
as `jar` files. Sometimes a dependency requires other dependencies in a recursive fashion. Those are called transitive
dependencies. When a dependency is needed, all its own dependencies are collected, also with their transitive dependencies.
This property is called transitivity. All the recursive dependencies of a dependencies are, thus,
called *transitive dependencies*.

The way how a dependency is identified is the common Maven way. A dependency is identified
using five tokens: `groupId`, `artifactId` and `version` which are required and `extension` and `classifier` which
are optional. 

The identifier has the following syntax `<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>`.

The syntax used in dependency magic commands is the following:

    com.google.code.gson:gson:2.10.1

All the identifier tokens in the specified order separated by colons. The previous dependency identifier has
the following for in a Maven pom file:

    <dependencies>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
        ...
    </dependencies>

## Resolving dependencies in notebook

Resolving a dependency is the process which a repository performs to search for loadable artifacts of a dependency 
and its transitive dependencies. Those artifacts, if they contain classes or class archives are loaded in the jvm.

In Maven this is done once by Maven tool using pom files which describes all the information. In a notebook this 
scenario is not feasible. Many dependency commands are one line magics. Thus, it is impossible to know all the 
configuration since that information can be spread in multiple cells and executed in an interactive way, not 
sequentially. For this purpose, the dependency management in `rapaio-jupyter-kernel` uses a different strategy, more
appropriate for an interactive usage scenario.

There are two different phases: proposal and resolved. When a dependency is added through a magic line, for example, 
that dependency is added as a candidate, it is not resolved on spot. You can add multiple dependencies as candidates.
The resolving actions happens only when resolve one line magic is executed:

    %dependency /resolve

When resolve magic is executed all the dependency candidates are resolved, which includes finding transitive 
dependencies, conflict resolution, collecting artifacts and loaded them in notebook's jvm. If any error happens during 
resolving the candidate dependencies, the error is displayed and the candidates are cleared. If resolving is 
successfully, the dependencies moves into resolved phase. 

Note that the resolve magic command applies only to the current set of candidate dependencies, not to the resolved 
dependencies. This happens also because once a dependency is resolved and loaded into notebook's jvm, it cannot 
be unloaded. Of course, one can restart the kernel and everything starts from scratch. 

The recommendation is still to have all the dependency configurations executed before the resolve magic command 
execution, to handle better cases of conflicts or dependency resolution. Following that recommendation it will 
have the potential of avoiding subtle situations, but this is not a requirement.  

## Common scenario

The implementation local repository for each notebook. The default remote repositories are also available.

If one needs to add a dependency, it can use the following magic command:

    %dependency /add com.github.javafaker:javafaker:1.0.2

This command adds as a direct dependency the `javafaker` with a specific version. If this is the only needed dependency, 
what should be done is to call the *resolve* operation.

    %dependency /resolve

The resolve operation is a complex operation which aims to identify direct and
transitive dependencies in the registered remote repositories, detect the artifacts attached to them, detects
potential conflicts on those dependencies, and finally brings the artifacts in the local cached repository and 
loads the into the virtual machine attached to the notebook instance.

From that moment, the classes defined in dependencies are available to the JVM through class loaders, and import instructions
can be executed successfully.

## Listing commands

Now that we know what we can do, we have also a set of magic commands which allows one to inspect the current state of 
the dependencies. We can list current repositories, we can list given directives for adding or overriding dependencies, 
and we can also list the artifacts which were loaded into notebook.

As always, we have also the listing from magic help available:

    %help
    
    ...
    Dependency manager
    Documentation:
    Find and resolve a dependency using coordinates: group_id, artifact_id and version id.
    The maven public repositories are searched for dependencies. Additionally, any maven transitive dependency declared with magic handlers are included in classpath.
    Syntax:
    %dependency /list-repos
    List all repositories
    %dependency /add-repo name url[ release|update-policy]?[ snapshot|update-policy]?
    Add Maven Repository using a name and an url. Optional parameters are available.
    Release enabled is obtained by specifying release|update-policy.
    Update policy can be: never/always/daily/interval.
    Snapshot enabled is obtained by specified snapshot|update-policy.
    Update policy can be: never/always/daily/interval.
    If any flag is not specified we have the following default options:
    - release enabled with never, snapshot enabled with never for repos with url starting with file://
      - release enabled with never, snapshot disabled for repos with url not starting with file://
    %dependency /list-dependencies
    List proposed and resolved dependencies
    %dependency /list-artifacts
    List artifacts loaded after the last dependency resolve
    %dependency /resolve
    Resolve dependencies
    %dependency /add groupId:artifactId[:extension[:classifier]]:version
    %dependency /add groupId:artifactId[:extension[:classifier]]:version --optional
    Declares a dependency to the current notebook. Artifact's extension (default value jar) and classifier (default value is empty string) values are optional.
    ...



[Back to README.md](README.md)