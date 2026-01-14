## Custom configuration kernel.json

Each Jupyter kernel is launched based on a `kernel.json` configuration file. This file can be found in the location you specify during
the installation process. One configuration file which I have on my host looks like the following:

    {
        "argv": [
            "java",
            "-jar",
            "/home/ANT.AMAZON.COM/tutuianu/.local/share/jupyter/kernels/rapaio-jupyter-kernel/rapaio-jupyter-kernel-3.0.1.jar",
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

This allows one to configure various things regarding this kernel.

### Enabling preview for Java 25 release

In order to enable preview features for a Java 25 release you can add options for java command line in `argv` command line. This enables
preview features in the java process which hosts the kernel. Additionally, you should add enable preview and release option to the
`RJK_COMPILER_OPTIONS`. The later will add those options to the JShell instance hosted in the kernel process. To make things clearer, one
could change the display name of the kernel on `display_name` entry. This will affect how Jupyter notebook will display this kernel.
The changed kernel file looks like the following:

    {
        "argv": [
            "java",
            "--enable-preview",
            "--add-modules",
            "java.base,jdk.incubator.vector",
            "-jar",
            "/home/ANT.AMAZON.COM/tutuianu/.local/share/jupyter/kernels/rapaio-jupyter-kernel-preview25/rapaio-jupyter-kernel-3.0.1.jar",
            "{connection_file}"
        ],
        "display_name": "Java (rjk 3.0.1 preview25)",
        "language": "java",
        "interrupt_mode": "message",
        "env": {
            "RJK_CLASSPATH": "",
            "RJK_COMPILER_OPTIONS": "--enable-preview --release 25",
            "RJK_INIT_SCRIPT": "",
            "RJK_TIMEOUT_MILLIS": "-1",
            "RJK_MIMA_CACHE": "mima_cache"
        }
    }

In a similar way you can enable preview features for older versions of jvm like 22, 23, or 24.

### Altering classpath on notebook launch

You can alter the classpath of a notebook using the environment variable entry `RJK_CLASSPATH`. The entries in the classpath are elements
which are separated by `;`. The elements from the classpath obey the *glob* syntax (you can use placeholders like `?`, `*` or `**`).
If an element of the classpath is not an absolute path, then the path is relative to the path of the notebook.

*Warning*: The `RJK_CLASSPATH` affects all the notebooks interpreted with this kernel, so the configurations should apply to all notebooks.

An illustrative example you can found below:

    {
        "argv": [
            "java",
            "-jar",
            "/home/ANT.AMAZON.COM/tutuianu/.local/share/jupyter/kernels/rapaio-jupyter-kernel-preview23/rapaio-jupyter-kernel-3.0.1.jar",
            "{connection_file}"
        ],
        "display_name": "Java (rjk 3.0.1)",
        "language": "java",
        "interrupt_mode": "message",
        "env": {
            "RJK_CLASSPATH": "/home/ati/work/rapaio/rapaio-core/target/*.jar;/home/ati/work/rapaio/rapaio-core/target/classes",
            "RJK_COMPILER_OPTIONS": "",
            "RJK_INIT_SCRIPT": "",
            "RJK_TIMEOUT_MILLIS": "-1"
        }
    }

If everything works fine you should see in the kernel output some logging lines as following:

    [I 2024-10-30 16:44:38.830 ServerApp] Kernel started: 52e8f068-0f55-4391-b0e3-94e3eda46c22
    WARNING: Using incubator modules: jdk.incubator.vector
    Parsing classpath: /home/ati/work/rapaio/rapaio-core/target/*.jar
    Add /home/ati/work/rapaio/rapaio-core/target/rapaio-core-7.0.0-sources.jar to classpath
    Add /home/ati/work/rapaio/rapaio-core/target/rapaio-core-7.0.0.jar to classpath
    Add /home/ati/work/rapaio/rapaio-core/target/rapaio-core-7.0.0-javadoc.jar to classpath
    Parsing classpath: /home/ati/work/rapaio/rapaio-core/target/classes
    Add /home/ati/work/rapaio/rapaio-core/target/classes to classpath

The log lines describes what elements were loaded which corresponds to each entry from the classpath.