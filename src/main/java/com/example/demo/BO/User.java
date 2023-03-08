package com.example.demo.BO;

import lombok.Data;

@Data
public class User {
    private Long id;       //主键id
    private String name;   //姓名
    private String school; //学校
    private Integer age;   //年龄

    public User(Long id, String name, Integer age, String school) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.school = school;
    }
}
