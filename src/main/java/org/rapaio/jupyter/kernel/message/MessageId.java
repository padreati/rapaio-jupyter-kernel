package org.rapaio.jupyter.kernel.message;

import java.util.List;

public record MessageId<T>(List<byte[]> identities, Header<T> header) {
}
