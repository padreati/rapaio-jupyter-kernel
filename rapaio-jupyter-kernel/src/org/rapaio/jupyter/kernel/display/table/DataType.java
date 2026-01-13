package org.rapaio.jupyter.kernel.display.table;

import java.util.function.Function;

import org.rapaio.jupyter.kernel.global.Global;

public enum DataType {
    STRING(o -> {
        if (o == null) {
            return Global.options().display().format().na();
        }
        String value = o.toString();
        int maxColWidth = Global.options().display().maxColWidth();
        return value.length() > maxColWidth ? value.substring(0, maxColWidth) + "..." : value;
    }),
    INTEGER(o -> {
        if (o == null) {
            return Global.options().display().format().na();
        }
        return o.toString();
    }),
    FLOAT(o -> {
        if (o == null) {
            return Global.options().display().format().na();
        }
        try {
            Double value = Double.parseDouble(o.toString());
            String format = "%." + Global.options().display().format().precision() + "f";
            return String.format(format, value);
        } catch (Exception e) {
            return o.toString();
        }
    });

    DataType(Function<Object, String> formatFunction) {
        this.formatFunction = formatFunction;
    }

    private final Function<Object, String> formatFunction;

    public String format(Object o) {
        return formatFunction.apply(o);
    }

    public static DataType detectType(Object obj) {
        if (obj instanceof Byte ||
                obj instanceof Short ||
                obj instanceof Integer ||
                obj instanceof Long) {
            return DataType.INTEGER;
        }
        return DataType.STRING;
    }
}
