package org.rapaio.jupyter.kernel.display;

public interface DisplayWrapper<T extends Displayable> {

    boolean canWrap(Object o);

    T wrap(Object object);
}
