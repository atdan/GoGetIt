package com.example.root.gogetit.model;

import java.util.List;

public class Request {
    private String phone;
    private String address;
    private String name;
    private String total;
    private String comment;
    private List<Order> foods; // list of food orders

    private String status;

    public Request() {
    }

    public Request(String phone, String address, String name, String total, String comment, List<Order> foods, String status) {
        this.phone = phone;
        this.address = address;
        this.name = name;
        this.total = total;
        this.comment = comment;
        this.foods = foods;
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }
}
