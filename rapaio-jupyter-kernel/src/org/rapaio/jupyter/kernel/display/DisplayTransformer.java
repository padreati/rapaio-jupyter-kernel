package org.rapaio.jupyter.kernel.display;

/**
 * A display transformer is responsible for transforming a given object into a displayable object.
 * A displayable object is an object which can be handled by a renderer. The renderer will be responsible
 * for displaying the object. The responsible renderer will be chosen based on the mime type and the class.
 * <p>
 * In the display system there are available only a few renderers, even if those renderers are enriched
 * through SPI. If you want to display your own objects you need to implement a renderer for
 * your object and desired MIME types, or to provide a transformer from your object's class
 * to a class that can be rendered.
 * <p>
 * The transformer is responsible for checking if the object can be transformed, and if it can, it should
 * return a wrapper/adapter from your object's class type to the target class type.
 */
public interface DisplayTransformer {

    /**
     * Describes if the transformer can transform the given object.
     *
     * @param o object to be transformed
     * @return true if the transformer can transform the given object, false otherwise
     */
    boolean canTransform(Object o);

    /**
     * Transformation target class type.
     *
     * @return class type of the transformed object
     */
    Class<?> transformedClass();

    /**
     * Transforms the given object into a transformed object.
     *
     * @param o object to be transformed
     * @return transformed object
     */
    Object transform(Object o);
}
