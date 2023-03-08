package com.example.demo.common;

/**
 * @author changda@chinapost.com.cn
 * @date 2021年12月07日 10:44
 * @desc  季节枚举
 */
public enum Season {
  SPRING(1,"春天"),
  SUMMER(2,"夏天"),
  AUTUMN(3,"秋天"),
  WINTER(4,"冬天");

  private int value;
  private String name;

  Season(int value, String name) {
    this.value = value;
	this.name=name;
  }
  public int getValue(){
	  return value;
  }
  public String getName(){
	  return name;
  }
}
