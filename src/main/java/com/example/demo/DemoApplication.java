package com.example.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CPSD-CHANGDA
 */
@SpringBootApplication
public class DemoApplication {

  public static void main(String[] args) {

    String[] Season = {"Spring", "Summer", "Autumn", "Winter"};

    Map<Integer, String> year = new HashMap<>();
    year.put(1, "2001");
    year.put(2, "2002");
    year.put(3, "2003");
    year.put(4, "2004");
  }
}
