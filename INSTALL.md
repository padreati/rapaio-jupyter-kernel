# Installation

The latest version can be found as a download in the releases section. Rapaio Jupyter Kernel contains an
installing tool which allow users to quickly install the kernel. The installer is contained in the jar distribution archive
and can be called with:

    java -jar ./rapaio-jupyter-kernel-1.1.0.jar -i -auto

The `-auto` option uses an automatic mode for installing the kernel.

## Manual installation

Without using the automatic mode, the installer asks in an interactive way various information to customize the installed kernel.
An example of interactive installer mode:

    shell: java -jar ./rapaio-jupyter-kernel-1.1.0.jar -i
    Installing in interactive mode.
    Select installation path:
    [1] /home/ANT.AMAZON.COM/tutuianu/.local/share/jupyter/kernels
    [2] /usr/local/share/jupyter/kernels
    [3] /usr/share/jupyter/kernels
    1
    Select kernel dir (default 'rapaio-jupyter-kernel'):
    rapaio-custom-kernel
    Select display name (default 'Rapaio Kernel'):
    Rapaio Custom
    Select timeout in milliseconds:
    -1
    Select compiler options (default ''):
    
    Select init script (default ''):
    
    Installation path: /home/ANT.AMAZON.COM/tutuianu/.local/share/jupyter/kernels
    Kernel dir: rapaio-custom-kernel
    kernel.json:
    {
        "argv": [
            "java",
            "--enable-preview",
            "--add-modules",
            "jdk.incubator.vector,jdk.incubator.concurrent",
            "-jar",
            "/home/ANT.AMAZON.COM/tutuianu/.local/share/jupyter/kernels/rapaio-custom-kernel/rapaio-jupyter-kernel-1.1.0.jar",
            "{connection_file}"
        ],
        "display_name": "Rapaio Custom",
        "language": "java",
        "interrupt_mode": "message",
        "env": {
            "RJK_COMPILER_OPTIONS": "",
            "RJK_INIT_SCRIPT": "",
            "RJK_TIMEOUT_MILLIS": "-1"
        }
    }
    Installer is ready. Do you want to continue [Y/N] ?
    Y


All the previous steps can be realized manually by creating a kernel in a jupyter kernel specification directory. In that directory the
Java archive of versioned rapaio-jupyter-kernel will be copied and a file called `kernel.json` will be created with a content similar
with the one displayed in the previous installing script. 

Follow documentation from: [Jupyter Kernel Specs](https://jupyter-client.readthedocs.io/en/stable/kernels.html#kernel-specs) for 
more information regarding how a jupyter kernel should be installed.