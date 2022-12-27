package com.demo;

import org.junit.jupiter.api.Test;

public class Upload {
    @Test
    public void test1() {
        String fileName = "fuhkaj.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);
    }
}
