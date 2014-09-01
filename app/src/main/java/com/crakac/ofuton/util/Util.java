package com.crakac.ofuton.util;

import java.io.Closeable;

public class Util {
    public static void closeQuietly(Closeable c) {
        if(c == null) return;
        try {
            c.close();
        } catch (Exception e) {
        }
    }
}