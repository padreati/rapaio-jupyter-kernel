package org.rapaio.jupyter.kernel.util;

public final class Formatter {

    private Formatter(){}

    public static String formatAddress(String transport, String ip, int port) {
        return transport + "://" + ip + ":" + port;
    }


}
