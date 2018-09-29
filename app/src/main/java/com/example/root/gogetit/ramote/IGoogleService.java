package com.example.root.gogetit.ramote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleService {
    @GET
    Call<String> getAddress(@Url String url);
}
