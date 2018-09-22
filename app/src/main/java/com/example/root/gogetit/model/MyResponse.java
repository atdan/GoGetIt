package com.example.root.gogetit.model;

import java.util.List;

public class MyResponse {
    public  long multicast_id;
    public int success, failure;
    public int canonical_ids;
    public List<Result> results;
}
