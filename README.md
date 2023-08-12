# rapaio-jupyter-kernel

Jupyter kernel for Java language based on JShell. It implements Jupyter message specification version 5.4, and it requires Java >= 20.

## Introduction

I started implementing this kernel since I believe Java deserves a full feature and properly-maintained jupyter kernel. 
Personally I need that kernel to use features from a library I contribute to for statistics, ML and data manipulation. 
However, this kernel has no connection with this library, and it is a general purpose Java kernel.

Currently, this project is in a stable mode, and it contains the features I need. As such, I am the only maintainer. If the 
kernel prove useful and additional contributors jump into this boat, an organization can be created and a shared ownership 
can also be added, all of those for the sole purpose of continuity of improvements. The plan is to add new features if requested, 
so please add issues if you want this tool to be better and fit your needs, if you cannot contribute with code, also. 
Any feedback is appreciated and supported, if possible.

## Features

Since this is a work in progress, many desirable features will come in next releases. However, some of them are 
already implemented. Follow issues, vote them and create new ones, if you need improvements. 
Below are listed some features, besides running Java code, since this is the main purpose of the kernel:

* Display javadoc documentation (need styling and html postprocessing to make it nicer)
* Console output with ANSI code colors
* Autocompletion and documentation
* Magic help function to facilitate navigation through the magic functions
* Display items and update items based on references
* JShell commands (not all command are implemented since some of them are not applicable)
* Loading notebooks and JShell scripts
* Loading jar or jars from directory to classpath
* Adding directories with compiled classes to classpath
* Display image from local file systems or URL
* Attach maven dependencies (with transitive dependencies)

## Installation

The latest version can be found as a download in the releases section. Rapaio Jupyter Kernel contains an 
installing tool which allow users to quickly set up the kernel. The installer is contained in the jar distribution archive 
and can be called with:

    java -jar ./rapaio-jupyter-kernel-1.2.2.jar -i -auto

The `-auto` option uses an automatic mode for installing the kernel. The installer has also an interactive mode, and the kernel installation can be realized manually, also. 
For more details you can follow [INSTALL.md](INSTALL.md).

## Maven central

The project is also stored in maven central repositories and can be added as dependency in maven as:

    <dependency>
        <groupId>io.github.padreati</groupId>
        <artifactId>rapaio-jupyter-kernel</artifactId>
        <version>1.2.2</version>
    </dependency>

This is not necessary now, since the kernel is packaged as a self-contained jar archive (it includes all dependencies), 
and can be downloaded from releases. In the future it would be possible to introduce a plug-in mechanism to allow others to 
extend library for their own purposes. I see possibilities to allow others to create custom display code for objects of their 
own type or to implement new magic features. This kind of extension of notebook can work for custom features, for generic features 
one can consider contributing to this project.