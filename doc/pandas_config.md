# Working document for pandas options

This document collcted pandas notebook options as a source of inspiration for the options 
of this kernel. 


## Selected configurations

**display.colheader_justify** : 'left'/'right'/'center'
default: right

**display.html.border** : int
A ``border=value`` attribute is inserted in the ``<table>`` tag for the DataFrame HTML repr.
default: 1

**display.max_columns** : int
If max_cols is exceeded, switch to truncate view. Objects are centrally truncated. 
'0' value means unlimited.
default: 0

**display.max_colwidth** : int or None
The maximum width in characters of a column in the repr of
a pandas data structure. When the column overflows, a "..." 
placeholder is embedded in the output. A 'None' value means unlimited.
default: 50

**display.max_rows** : int
If max_rows is exceeded, switch to truncate view. Objects are centrally truncated. 
'0' value means unlimited.
default: 60

**display.max_seq_items** : int
When pretty-printing a long sequence, no more then `max_seq_items`
will be printed. If items are omitted, they will be denoted by the
addition of "..." to the resulting string.
 If set to 0, the number of items to be printed is unlimited.
default: 100

**display.format.na** : str
The string representation for values identified as missing.
default: ''


## Pandas configurations

This document contains come pandas configurations which are interesting
to be implemented in the rapaio-jupyter-kernel 


display.date_dayfirst : boolean
When True, prints and parses dates with the day first, eg 20/01/2005
[default: False] [currently: False]

display.date_yearfirst : boolean
When True, prints and parses dates with the year first, eg 2005/01/20
[default: False] [currently: False]

display.encoding : str/unicode
Defaults to the detected encoding of the console.
Specifies the encoding to be used for strings returned by to_string,
these are generally strings meant to be displayed on the console.
[default: utf-8] [currently: utf8]

display.expand_frame_repr : boolean
Whether to print out the full DataFrame repr for wide DataFrames across
multiple lines, `max_columns` is still respected, but the output will
wrap-around across multiple "pages" if its width exceeds `display.width`.
[default: True] [currently: True]

display.float_format : callable
The callable should accept a floating point number and return
a string with the desired format of the number. This is used
in some places like SeriesFormatter.
See formats.format.EngFormatter for an example.
[default: None] [currently: None]


display.large_repr : 'truncate'/'info'
For DataFrames exceeding max_rows/max_cols, the repr (and HTML repr) can
show a truncated table, or switch to the view from
df.info() (the behaviour in earlier versions of pandas).
[default: truncate] [currently: truncate]

display.max_categories : int
This sets the maximum number of categories pandas should output when
printing out a `Categorical` or a Series of dtype "category".
[default: 8] [currently: 8]



display.max_info_columns : int
max_info_columns is used in DataFrame.info method to decide if
per column information will be printed.
[default: 100] [currently: 100]

display.max_info_rows : int
df.info() will usually show null-counts for each column.
For large frames this can be quite slow. max_info_rows and max_info_cols
limit this null check only to frames with smaller dimensions than
specified.
[default: 1690785] [currently: 1690785]


display.min_rows : int
The numbers of rows to show in a truncated view (when `max_rows` is
exceeded). Ignored when `max_rows` is set to None or 0. When set to
None, follows the value of `max_rows`.
[default: 10] [currently: 10]

display.multi_sparse : boolean
"sparsify" MultiIndex display (don't display repeated
elements in outer levels within groups)
[default: True] [currently: True]

display.pprint_nest_depth : int
Controls the number of nested levels to process when pretty-printing
[default: 3] [currently: 3]

display.precision : int
Floating point output precision in terms of number of places after the
decimal, for regular formatting as well as scientific notation. Similar
to ``precision`` in :meth:`numpy.set_printoptions`.
[default: 6] [currently: 6]

display.show_dimensions : boolean or 'truncate'
Whether to print out dimensions at the end of DataFrame repr.
If 'truncate' is specified, only print out the dimensions if the
frame is truncated (e.g. not display all rows and/or columns)
[default: truncate] [currently: truncate]

display.unicode.ambiguous_as_wide : boolean
Whether to use the Unicode East Asian Width to calculate the display text
width.
Enabling this may affect to the performance (default: False)
[default: False] [currently: False]

display.unicode.east_asian_width : boolean
Whether to use the Unicode East Asian Width to calculate the display text
width.
Enabling this may affect to the performance (default: False)
[default: False] [currently: False]

display.width : int
Width of the display in characters. In case python/IPython is running in
a terminal this can be set to None and pandas will correctly auto-detect
the width.
Note that the IPython notebook, IPython qtconsole, or IDLE do not run in a
terminal and hence it is not possible to correctly detect the width.
[default: 80] [currently: 80]

mode.chained_assignment : string
Raise an exception, warn, or no action if trying to use chained assignment,
The default is warn
[default: warn] [currently: warn]

mode.copy_on_write : bool
Use new copy-view behaviour using Copy-on-Write. Defaults to False,
unless overridden by the 'PANDAS_COPY_ON_WRITE' environment variable
(if set to "1" for True, needs to be set before pandas is imported).
[default: False] [currently: False]

plotting.matplotlib.register_converters : bool or 'auto'.
Whether to register converters with matplotlib's units registry for
dates, times, datetimes, and Periods. Toggling to False will remove
the converters, restoring any converters that pandas overwrote.
[default: auto] [currently: auto]

styler.format.decimal : str
The character representation for the decimal separator for floats and complex.
[default: .] [currently: .]

styler.format.escape : str, optional
Whether to escape certain characters according to the given context; html or latex.
[default: None] [currently: None]

styler.format.formatter : str, callable, dict, optional
A formatter object to be used as default within ``Styler.format``.
[default: None] [currently: None]

styler.format.precision : int
The precision for floats and complex numbers.
[default: 6] [currently: 6]

styler.format.thousands : str, optional
The character representation for thousands separator for floats, int and complex.
[default: None] [currently: None]

styler.html.mathjax : bool
If False will render special CSS classes to table attributes that indicate Mathjax
will not be used in Jupyter Notebook.
[default: True] [currently: True]

styler.render.encoding : str
The encoding used for output HTML and LaTeX files.
[default: utf-8] [currently: utf-8]

styler.render.repr : str
Determine which output to use in Jupyter Notebook in {"html", "latex"}.
[default: html] [currently: html]

styler.sparse.columns : bool
Whether to sparsify the display of hierarchical columns. Setting to False will
display each explicit level element in a hierarchical key for each column.
[default: True] [currently: True]

