# Object display: how it works and how can be extended

## Display objects in Jupyter notebooks

The Jupyter notebooks uses a data structure called DisplayData as the mechanism to display information 
in the cell's outputs. By default, the display data contains raw information and associated MIME types, 
which describes how that information should be displayed. 

For example if somebody wants to produce a html fragment to be displayed in the output section, it will send 
to the notebook a display data object with the html fragment and uses "text/html" MIME type. In this way
the notebook renders the fragment using a dedicated display function which is capable to handle html fragments.

Most of the common MIME types are supported by default by the Jupyter lab and Jupyter notebooks, and probably 
also by all other implementations which you can find integrated in various IDEs like VSCode, IntelliJ, etc.

For more information you see also [IPython Jupyter notebook format](https://ipython.readthedocs.io/en/3.x/notebook/nbformat.html#display-data)
and [Jupyter client 8.7.1 display-data](https://jupyter-client.readthedocs.io/en/latest/messaging.html#display-data).

## Display objects in rapaio-jupyter-kernel

As any Jupyter kernel, rapaio-jupyter-kernel produces display-data structures to the notebooks to produce content 
for output cells. This can be realized from java code cells using one of the methods: `display(Object o)`, 
`display(String mime, Object o)`, `updateDisplay(String id, String mimeType, Object o)` or `updateDisplay(String id, Object o)`. 
A trivial example is the following:

    Map<Integer, String> numbers = Map.of(1, "one", 2, "two");
    display("html", numbers)

All those methods are statically imported into any notebook if this kernel is used, and they are actually public 
methods provided by the `Global` class.

The internal mechanism in the kernel is as it follows. When an object needs to be displayed then the kernel searches 
for a display renderer which is capable to handle that object and is also capable to produce content for the desired 
MIME type. If no renderer was found installed in the system (either provided by the kernel or by external libraries),
the kernel searches for a proper display transformer. A display transformer is a function which is able to transform 
the object which needs to be displayed into another type for which there is available a desired renderer. 

There are some limited amounts of display renderers and display transformer provided by default by the kernel.
But the display renderers, as also the display transformers, can be enriched by external libraries through SPI
(Service Provider Interface). When a library which provides SPI implementations are added to the class path, 
the SPI provided are also added to the available display facilities. 

Also, a library is provided for this purpose: `rapaio-jupyter-display` which serves a double purpose. 
It provides additional display renderers and transformers, and it is an illustrative example which can
be used by you to build your own libraries to extend the display facilities of the kernel.

