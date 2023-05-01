package org.rapaio.jupyter.kernel.core.magic;

public record MagicEvalResult(boolean handled, Object result) {
}
