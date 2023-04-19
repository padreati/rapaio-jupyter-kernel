package org.rapaio.jupyter.kernel.base.message;

import java.util.List;

import org.rapaio.jupyter.kernel.base.message.Header;

public record MessageContext<T>(List<byte[]> identities, Header<T> header) {
}
