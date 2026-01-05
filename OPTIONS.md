# Options System

The rapaio-jupyter-kernel provides a configurable options system to customize display behavior and formatting.

## Accessing Options

The options are named global values which customize the kernel behavior. Options are identified by a key, they have a value, 
and also a description.

All the options are stored in a map-like class `Options` and they are available from the static utility class `Global`.

```java
// Get global options instance
Options options = Global.options(); 
```

or you ca simply use the following syntax, since it is already statically imported

```java
// Get global options instance using statically imported shortcut
Options options = options();
```

### Get option's values

There are two different ways to get option's values: using map=like getter methods with full key or using syntax-sugar utility interfaces.
Below is an example of obtaining the same option value in two different manners. 
Both syntaxes are equivalent, however, I prefer the second one since it allows me to use autocompletion.  

```java
// print the maximum number of rows to be printed in a table using full key getter
display(options().get("display.maxRows"));

// print the maximum number of rows to be printed in a table using utility interfaces
display(options().display().maxRows());

```

### Set option's values

The option's values can also be set using two syntaxes, which mimics the ones for getters.

```java
// setting the value of 100 to the number of displayed rows using setter syntax 
options().set("display.maxRows", "100");

// setting the value of 100 to the number of displayed rows using utility interfaces syntax  
options().display().maxRows(100);
```

Notice also that if you use the utility interface syntax you will have also types available, when it is the case. 
This is not possible with the setter syntax. This is an additional argument in favor of using the second syntax.  


## Display Options

### Basic Display Settings

| Option key                 | Utility syntax                         | Default     | Description                        |
|----------------------------|----------------------------------------|-------------|------------------------------------|
| `display.defaultMime`      | options().display().defaultMime()      | `text/html` | Default MIME type for display data |
| `display.defaultMimeImage` | options().display().defaultMimeImage() | `image/png` | Default MIME type for images       |

```java
// Configure basic display settings
options().display().defaultMime("html");
options().display().defaultMimeImage("png");
```

### Tabular Display Limits

| Option key            | Utility syntax                    | Default | Description                                        |
|-----------------------|-----------------------------------|---------|----------------------------------------------------|
| `display.maxRows`     | options().display().maxRows()     | `20`    | Maximum rows before truncation (0 = unlimited)     |
| `display.maxCols`     | options().display().maxCols()     | `20`    | Maximum columns before truncation (0 = unlimited)  |
| `display.maxColWidth` | options().display().maxColWidth() | `50`    | Maximum column width in characters (0 = unlimited) |
| `display.maxSeqItems` | options().display().maxSeqItems() | `50`    | Maximum sequence items to display (0 = unlimited)  |
| `display.showIndex`   | options().display().showIndex()   | `true`  | Show row index in tables                           |

```java
// Configure table limits
options().display().maxRows(50);
options().display().maxCols(10);
options().display().maxColWidth(100);
options().display().maxSeqItems(200);
options().display().showIndex(false);
```


### Format Options

| Option Key                 | Utility syntax                           | Default | Description                               |
|----------------------------|------------------------------------------|---------|-------------------------------------------|
| `display.format.na`        | options().display().format().na()        | `""`    | String representation for missing values  |
| `display.format.precision` | options().display().format().precision() | `6`     | Decimal places for floating point numbers |

```java
// Configure formatting
options().display().format().na("N/A");
options().display().format().precision(3);
```

### HTML Options

| Option Key            | Utility Syntax                      | Default | Description                      |
|-----------------------|-------------------------------------|---------|----------------------------------|
| `display.html.border` | options().display().html().border() | `1`     | Border attribute for HTML tables |

```java
// Configure HTML display
options().display().html().border(2);
```

## Configuration Examples

### Minimal Display
```java
display.maxRows(5);
display.maxCols(3);
display.showIndex(false);
```

### Detailed Display
```java
display.maxRows(0); // unlimited
display.maxCols(0); // unlimited
format.precision(8);
format.na("NULL");
```

### Markdown Output
```java
display.defaultMime("text/markdown");
```

## Option Loading

Options are loaded from `options.json` at startup and can be modified at runtime. Changes persist for the current kernel session only.