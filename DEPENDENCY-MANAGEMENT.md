# Dependency management in Rapaio-Jupyter-Kernel

Dependency management implementation in this kernel is based entirely on Ivy. This choice is motivated
by various factors, among them:

- Ivy is easy to be included as dependency as it consists only in a jar file, packaged into kernel
- In contrast with Maven and other big dependency managers, Ivy does only dependency management.
  This is actually the only feature needed in this kernel, as it's aim is not to build or publish artifacts
  of any sort.
- Dependency conflicts is flexible and default options allows one to solve easily most common scenarios.
  If there is a need not covered by the exposed behavior an issue can be cut and we can search for a solution.

That being said, the implementation details are abstracted and users should not need knowledge about Ivy.
All the concepts should be documented here. The current implementation solves common problems which I found
during working with notebooks. Feel free to cut issues for things which cannot be done.

## What is a dependency

A *dependency* is a module which contains various artifacts encapsulating functionality, most of
the time packaged as `jar` files. Often times a dependency require other dependencies, and so on.
When a dependency is needed, all it's own dependencies are collected, also with their own dependencies.
This property is called transitivity. All the recursive dependencies of a dependencies are, thus,
called *transitive dependencies*.

The way how a dependency is identified is the common Ivy way. A dependency is identified
using five tokens: `groupId`, `artifactId` and `version` which are required and `extension` and `classifier` which
are optional. The identifier has the following syntax `<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>`.

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

## Repositories

A remote repository is a server which contains in a specific format various packages. When a dependency is requested,
it's metadata, data and dependencies are searched for in the registered remote repositories. By default, the
`rapaio-jupyter-kernel` has the following public IBiblio repositories defined:

        central, https://repo.maven.apache.org/maven2/
        jcenter, https://jcenter.bintray.com/
        jboss, https://repository.jboss.org/nexus/content/repositories/releases/
        atlassian, https://packages.atlassian.com/maven/public

We can always add IBiblio repositories the the default ones, in order to allow the notebook to download artifacts from
other repositories. This is an example of that:

    %dependency /add-repo google https://maven.google.com/

## Common scenario

The current implementation creates a module for each notebook. It also creates a cached local repository managed by Ivy
in order to avoid costly downloads. The remote repository are registered in the Ivy instance which is executed for each notebook.

If one needs to add a dependency, it can use the following magic command:

    %dependency /add com.github.javafaker:javafaker:1.0.2

This command adds as a direct dependency the javafaker specific version. If this is the only this which is needed, what should
be done is to call the *resolve* operation.

    %dependency /resolve

The resolve operation is a complex operation which aims to identify direct and
transitive dependencies in the registered remote repositories, detect the artifacts attached to them, detects
potential conflicts on those dependencies, solve the according to the selected conflict manager and other directives, and
finally brings the artifacts in the local cached repository and loads the into the virtual machine attached to the notebook instance.
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
    %dependency /add-repo name url
    Add Maven Repository using a name and an url
    %dependency /list-configuration
    List all the dependency configurations
    %dependency /list-artifacts
    List artifacts loaded after the last dependency resolve
    %dependency /resolve
    Resolve dependencies
    %dependency /add groupId:artifactId[:extension[:classifier]]:version
    %dependency /add groupId:artifactId[:extension[:classifier]]:version --optional
    Declares a dependency to the current notebook. Artifact's extension (default value jar) and classifier (default value is empty string) values are optional.
    ...



[Back to README.md](README.md)