package com.example.app;

public class Contact {
    private String name;
    private String phone;

    // 생성자
    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    // getter, setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}