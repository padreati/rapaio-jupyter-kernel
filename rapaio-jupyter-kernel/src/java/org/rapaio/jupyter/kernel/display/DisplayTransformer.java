package org.rapaio.jupyter.kernel.display;

public interface DisplayTransformer {

    Class<?> transformerClass();

    boolean canTransform(Object o);

    Object transform(Object o);
}
