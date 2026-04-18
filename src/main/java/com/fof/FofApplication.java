package com.fof;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FofApplication {

  public static void main(String[] args) {
    SpringApplication.run(FofApplication.class, args);
  }
}

