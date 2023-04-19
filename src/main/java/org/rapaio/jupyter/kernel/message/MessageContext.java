package org.rapaio.jupyter.kernel.message;

import java.util.List;

public record MessageContext<T>(List<byte[]> identities, Header<T> header) {
}
