package org.rapaio.jupyter.kernel.core.format;

import java.util.List;

import org.rapaio.jupyter.kernel.core.java.JavaEngine;

@FunctionalInterface
public interface ExceptionFormatter<T extends Exception> {

    List<String> format(JavaEngine javaEngine, T e);
}
