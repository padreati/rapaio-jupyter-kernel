# Installation

The latest version can be found as a download in the releases section. Rapaio Jupyter Kernel contains an
installing tool which allow users to quickly install the kernel. The installer is contained in the jar distribution archive
and can be called with:

    java -jar ./rapaio-jupyter-kernel-3.0.1.jar -i -auto

The `-auto` option uses an automatic mode for installing the kernel.

Starting with version 2.1.0 there are also installation profiles. The role of an installation profile is just to offer a predefined 
installation setup configured for some special situations. The same thing offered by an installation profile can be obtained by 
manually changing the `kernel.json` file.

The following profiles are defined:

* `java -jar ./rapaio-jupyter-kernel-3.0.1.jar -i -auto -preview22` - enables preview features for Java 22 release.
* `java -jar ./rapaio-jupyter-kernel-3.0.1.jar -i -auto -preview23` - enables preview features for Java 23 release.
* `java -jar ./rapaio-jupyter-kernel-3.0.1.jar -i -auto -preview24` - enables preview features for Java 24 release.
* `java -jar ./rapaio-jupyter-kernel-3.0.1.jar -i -auto -preview25` - enables preview features for Java 25 release.

If no profile is specified, then the standard installation is performed which has a proper configurations for all Java versions >= 17. 

## Manual installation

Without using the automatic mode, the installer asks in an interactive way various information to customize the installed kernel.
An example of interactive installer mode:

    shell: java -jar ./rapaio-jupyter-kernel-3.0.1.jar -i
    Installing in interactive mode.
    Select installation path:
    [1] /home/ANT.AMAZON.COM/tutuianu/.local/share/jupyter/kernels
    [2] /usr/local/share/jupyter/kernels
    [3] /usr/share/jupyter/kernels
    1
    Select kernel dir (default 'rapaio-jupyter-kernel'):
    
    Select mima cache folder (default 'mima_cache'):
    
    Select display name (default 'Java (rjk 3.0.1)'):
    
    Select timeout in milliseconds:
    
    Select compiler options (default ''):
    
    Select init script (default '':
    
    Installation path: /home/ANT.AMAZON.COM/tutuianu/.local/share/jupyter/kernels
    Kernel dir: rapaio-jupyter-kernel
    kernel.json:
    {
    "argv": [
    "java",
    "-jar",
    "/home/ANT.AMAZON.COM/tutuianu/.local/share/jupyter/kernels/rapaio-jupyter-kernel/rapaio-jupyter-kernel-3.0.0.jar",
    "{connection_file}"
    ],
    "display_name": "Java (rjk 3.0.1)",
    "language": "java",
    "interrupt_mode": "message",
    "env": {
    "RJK_CLASSPATH": "",
    "RJK_COMPILER_OPTIONS": "",
    "RJK_INIT_SCRIPT": "",
    "RJK_TIMEOUT_MILLIS": "-1",
    "RJK_MIMA_CACHE": "mima_cache"
    }
    }
    Installer is ready. Do you want to continue [Y|y/N|n] ?
    y


All the previous steps can be realized manually by creating a kernel in a jupyter kernel specification directory. In that directory the
Java archive of versioned rapaio-jupyter-kernel will be copied and a file called `kernel.json` will be created with a content similar
with the one displayed in the previous installing script. 

Follow documentation from: [Jupyter Kernel Specs](https://jupyter-client.readthedocs.io/en/stable/kernels.html#kernel-specs) for 
more information regarding how a jupyter kernel should be installed.