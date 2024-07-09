package org.rapaio.jupyter.kernel.core.magic.interpolate;

import org.rapaio.jupyter.kernel.core.RapaioKernel;

public record StringToken(String originalValue, boolean canInterpolate) {

    public String interpolate(RapaioKernel kernel) {
        if(!canInterpolate) {
            return originalValue;
        }
        return "INTERPOLATE(" + originalValue + ")";
    }
}
