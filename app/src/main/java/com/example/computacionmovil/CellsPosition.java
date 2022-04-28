package com.example.computacionmovil;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CellsPosition {
    @GET("cell")
    Call<CellsPositionRes> listLocation(@Query("v") Double v,
                                  @Query("data") String data,
                                  @Query("mcc") Integer mcc,
                                  @Query("mnc") Integer mnc,
                                  @Query("lac") Integer lac,
                                  @Query("cellid") Integer cellid);
}

