package org.rapaio.jupyter.kernel.core.format;

import org.rapaio.jupyter.kernel.core.RapaioKernel;

import java.util.List;

@FunctionalInterface
public interface ErrorFormatter<T extends Throwable> {

    List<String> format(RapaioKernel kernel, T e);
}
