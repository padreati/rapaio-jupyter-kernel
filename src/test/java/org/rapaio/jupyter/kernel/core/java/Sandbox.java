package org.rapaio.jupyter.kernel.core.java;

import java.util.ArrayList;
import java.util.List;

public class Sandbox {

    public static void main(String[] args) {
        List<Integer> l = new ArrayList<>();
        l.add(7);
        var x = l.get(0);
        System.out.println(l);
    }
}
