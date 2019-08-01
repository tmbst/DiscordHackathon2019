package com.github.tmbst;

import java.io.InputStream;

public class Utility {

    /*
        Make sure you mark a resource folder in your IDE or marked otherwise in your Java environment
     */
    public static InputStream getResource(String name) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader.getResourceAsStream(name);
    }

}
