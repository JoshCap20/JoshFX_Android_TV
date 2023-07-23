package com.example.myapplication.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface API {
    @GET("{pathToFile}")
    Call<ResponseBody> getVideoFile(@Path(value = "pathToFile", encoded=true) String pathToFile);
}