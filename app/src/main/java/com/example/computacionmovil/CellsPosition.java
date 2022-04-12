package com.example.computacionmovil;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CellsPosition {
    @GET("cell")
    Call<JSONObject> listLocation(@Query("v") Double v,
                                  @Query("data") String data,
                                  @Query("mcc") Integer mcc,
                                  @Query("mnc") Integer mnc,
                                  @Query("lac") Integer lac,
                                  @Query("cellid") Integer cellid);
}
