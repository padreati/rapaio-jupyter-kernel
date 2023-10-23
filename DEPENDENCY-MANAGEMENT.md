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
using three tokens: `organization`, `name` and `revision`. The terminology differs for Maven, but retains its
meaning. For Maven the identifier is composed of `groupId`, `artifactId` and `version`.

The syntax used in dependency magic commands is the following:

    com.google.code.gson:gson:2.10.1

the three identifier tokens in the specified order separated by colons. The previous dependency identifier has
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

## Conflict dependencies

Ivy allows multiple ways to solve dependency conflicts through policies implemented into dependency conflict managers.
Those are the conflict managers available:

Here is a list of built-in conflict managers (which do not require anything in the settings file):

- *all*: this conflict manager resolve conflicts by selecting all revisions. Also called the NoConflictManager,
  it doesn’t evict any modules.
- *latest-time*: this conflict manager selects only the latest revision, latest being defined as the latest in time.
  Note that latest in time is costly to compute, so prefer latest-revision if you can.
- *latest-revision*: this conflict manager selects only the latest revision, latest being defined by a string comparison of revisions.
- *latest-compatible*: this conflict manager selects the latest version in the conflicts which can result
  in a compatible set of dependencies. This means that in the end, this conflict manager does not allow any conflicts (similar to the strict
  conflict manager), except that it follows a best effort strategy to try to find a set of compatible modules (according to the version
  constraints)
- *strict*: this conflict manager throws an exception (i.e. causes a build failure) whenever a conflict is found. (default value)

To select a conflict manager one can use magic commands. The following is an example:

    %dependency /conflict-manager latest-revision

The default conflict manager is *strict* (which is different than the usual one in Ivy, which is *all*). The main reason why
the default conflict manager is *strict* is for awareness. If a conflict appears it will be signaled immediately.
If a conflict arise, than changing the conflict manager can solve the problem, but at least the user knows there is an issue.

## Dependency overrides

Sometimes, when we have to deal with dependency conflicts, the simple selection of a dependency conflict manager is not enough.
For example, we might want to select a specific version of a given dependency, no matter if that dependency is a transitive
dependency with another revision. For that purpose we can use *dependency overrides**. In Ivy we can specify dependency overrides
at module level or at dependency level. We have choose to specify at module level since it is a simple way and avoids the
need to specify the same override for multiple dependencies. A dependency override is a directive given to dependency resolvers
to replace all occurrences of a given dependency identified by organization and name, with a dependency having a specific version.

To illustrate this situation we will use the following scenario.

Suppose that we are in the following situation:

    %dependency /add com.github.fakemongo:fongo:2.1.1
    %dependency /add org.apache.avro:avro:1.11.3
    %dependency /resolve

Since the default conflict manager is *strict* the following happens:

    StrictConflictException: com.fasterxml.jackson.core#jackson-core;2.14.2 (needed by [org.apache.avro#avro;1.11.3]) 
    conflicts with com.fasterxml.jackson.core#jackson-core;2.2.2 (needed by [de.grundid.opendatalab#geojson-jackson;1.2, 
    com.fasterxml.jackson.core#jackson-databind;2.2.2])
    
    at org.apache.ivy.plugins.conflict.StrictConflictManager.resolveConflicts(StrictConflictManager.java:42)
    at org.apache.ivy.core.resolve.ResolveEngine.resolveConflicts(ResolveEngine.java:1055)
    at org.apache.ivy.core.resolve.ResolveEngine.resolveConflict(ResolveEngine.java:933)
    at org.apache.ivy.core.resolve.ResolveEngine.resolveConflict(ResolveEngine.java:982)
    at org.apache.ivy.core.resolve.ResolveEngine.resolveConflict(ResolveEngine.java:852)
    at org.apache.ivy.core.resolve.ResolveEngine.fetchDependencies(ResolveEngine.java:719)
    at org.apache.ivy.core.resolve.ResolveEngine.doFetchDependencies(ResolveEngine.java:801)
    at org.apache.ivy.core.resolve.ResolveEngine.fetchDependencies(ResolveEngine.java:729)
    at org.apache.ivy.core.resolve.ResolveEngine.doFetchDependencies(ResolveEngine.java:792)
    at org.apache.ivy.core.resolve.ResolveEngine.fetchDependencies(ResolveEngine.java:729)
    at org.apache.ivy.core.resolve.ResolveEngine.doFetchDependencies(ResolveEngine.java:792)
    at org.apache.ivy.core.resolve.ResolveEngine.fetchDependencies(ResolveEngine.java:729)
    at org.apache.ivy.core.resolve.ResolveEngine.doFetchDependencies(ResolveEngine.java:801)
    at org.apache.ivy.core.resolve.ResolveEngine.fetchDependencies(ResolveEngine.java:729)
    at org.apache.ivy.core.resolve.ResolveEngine.getDependencies(ResolveEngine.java:607)
    at org.apache.ivy.core.resolve.ResolveEngine.resolve(ResolveEngine.java:250)
    at org.apache.ivy.Ivy.resolve(Ivy.java:522)
    at org.rapaio.jupyter.kernel.core.magic.dependencies.DependencyManager.resolve(DependencyManager.java:182)
    at org.rapaio.jupyter.kernel.core.magic.handlers.DependencyHandler.evalLineResolve(DependencyHandler.java:210)
    at org.rapaio.jupyter.kernel.core.magic.MagicHandler.eval(MagicHandler.java:36)
    at org.rapaio.jupyter.kernel.core.magic.MagicEngine.eval(MagicEngine.java:83)
    at org.rapaio.jupyter.kernel.core.RapaioKernel.handleExecuteRequest(RapaioKernel.java:186)
    at org.rapaio.jupyter.kernel.channels.ShellChannel.lambda$bind$0(ShellChannel.java:52)
    at org.rapaio.jupyter.kernel.channels.LoopThread.run(LoopThread.java:21)

We understand that `jackson-core` package has a conflict. We can solve the conflict in multiple ways.

We can choose the latest version through a conflict manager. The following would solve the problem:

    %dependency /conflict-manager latest-time
    %dependency /resolve

We can choose a different strategy to select the version (through a different conflict manager), and there is even
the possibility to load all the revisions in memory, which I would not advice, since you will not know what
version of the library will be used at runtime.

Another flexible option would be to configure a dependency override. If we don't want the latest revision for this specific package,
we can simply instruct the resolve to choose a specific one.

    %dependency /conflict-manager latest-revision
    %dependency /override de.grundid.opendatalab:geojson-jackson:1.2
    %dependency /resolve

Dependency override is a flexible way to solve conflicts, but also is a way to do overrides in general, even if we do
not have conflicts.

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
      The maven public repositories are searched for dependencies. Additionally, any maven 
      transitive dependency declared with magic handlers are included in classpath.
    Syntax:
      %dependency /list-repos
        List all repositories
      %dependency /add-repo name url
        Add Maven Repository using a name and an url
      %dependency /list-configuration
        List all the dependency configurations
      %dependency /list-artifacts
        List artifacts loaded after the last dependency resolve
      %dependency /conflict-manager all|latest-time|latest-revision(default)|latest-compatible|strict
        Configures a conflict manager. A conflict manager describes how conflicts are resolved.
          all: resolve conflicts by selecting all revisions, it doesn’t evict any modules.
          latest-time: selects only the latest in time revision.
          latest-revision: selects only the latest revision.
          latest-compatible: selects the latest version in the conflicts which can result in a compatible set of dependencies. This conflict manager does not allow any conflicts (similar to the strict conflict manager), except that it follows a best effort strategy to try to find a set of compatible modules (according to the version constraints)
          strict: throws an exception (i.e. causes a build failure) whenever a conflict is found. It does not take into consideration overrides. (default value)
      %dependency /resolve
        Resolve dependencies
      %dependency /add group_id:artifact_id:version
      %dependency /add group_id:artifact_id:version --force
        Declares a direct dependency to dependency manager.
        Flag /force can be used in order to force version overrides.
        This command does not resolve dependencies.
      %dependency /override group_id:artifact_id:version
        Declares an override, dependencies matched by group_id and artifact_id will be replaced with this override.
        A more flexible way to solve conflicts, even if a conflict actually does not exist.
        It cannot be used to override forced direct dependencies.


[Back to README.md](README.md)