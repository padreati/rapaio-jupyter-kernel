# rapaio-jupyter-kernel

Jupyter kernel for Java language based on JShell. It implements Jupyter message specification version 5.4, and it requires Java = 22.

## Introduction

I started implementing this kernel since I believe Java deserves a full feature and properly maintained jupyter kernel. 
Personally, I need that kernel to use features from a library I contribute to for statistics, ML, and data manipulation. 
However, this kernel has no connection with this library, and it is a general-purpose Java kernel.

Currently, this project is in a stable mode, and it contains the features I need. As such, I am the only maintainer. If the 
kernel prove useful and additional contributors jump into this boat, an organization can be created and a shared ownership 
can also be added, all of those for the sole purpose of continuity of improvements. The plan is to add new features if requested, 
so please add issues if you want this tool to be better and fit your needs. If possible you can contribute with code. 
Any feedback is appreciated.

## Features

Since this is a work in progress, many desirable features will come in the next releases. However, some of them are 
already implemented. Follow issues, vote on them, and create new ones, if you need improvements. 
Below are listed some features, besides running Java code, since this is the main purpose of the kernel:

* Display Javadoc documentation (need styling and HTML postprocessing to make it nicer)
* Console output with ANSI code colors
* Autocompletion and documentation
* Magic help function to facilitate navigation through the magic functions
* Placeholders in magic commands
* Display items and update items based on references
* JShell commands (not all commands are implemented since some of them are not applicable)
* Loading notebooks and JShell scripts
* Loading jar or jars from the directory to the classpath
* Adding directories with compiled classes to the classpath
* Display image from local file systems or URL
* Manage dependencies (with transitivity). More details: [DEPENDENCY-MANAGEMENT.md](DEPENDENCY-MANAGEMENT.md).

For a brief tour of the features check out the example notebook [here](example.ipynb).

## Installation

The latest version can be found as a download in the releases section. Rapaio Jupyter Kernel contains an 
installing tool which allows users to quickly set up the kernel. The installer is contained in the jar distribution archive 
and can be called with:

```sh
java -jar ./rapaio-jupyter-kernel-2.0.0.jar -i -auto
```

The `-auto` option uses an automatic mode for installing the kernel. The installer has also an interactive mode, and the kernel installation can be realized manually, also. 
For more details, you can follow [INSTALL.md](INSTALL.md).

## Maven central

The project is also stored in maven central repositories and can be added as a dependency in maven as:

```xml
<dependency>
    <groupId>io.github.padreati</groupId>
    <artifactId>rapaio-jupyter-kernel</artifactId>
    <version>2.0.0</version>
</dependency>
```

This is unnecessary, since the kernel is packaged as a self-contained jar archive (including all dependencies), 
and can be downloaded from releases. In the future, it would be possible to introduce a plug-in mechanism to allow others to 
extend the library for their own purposes. I see possibilities to allow others to create custom display code for objects of their 
own type or to implement new magic features. This kind of notebook extension can work for custom features, for generic features 
one can consider contributing to this project.
