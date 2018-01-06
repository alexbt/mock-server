package com.alexbt.mock;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author alexbt
 */
@SpringBootApplication
public class Launcher {
    public static void main(String[] args) {
        System.setProperty("spring.config.name", "mock");
        new SpringApplicationBuilder(Launcher.class)
                .run(args);
    }
}
