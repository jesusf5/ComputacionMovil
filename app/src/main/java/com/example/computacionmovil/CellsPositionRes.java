package com.example.computacionmovil;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CellsPositionRes {

    @SerializedName("result")
    @Expose
    private Integer result;
    @SerializedName("data")
    @Expose
    private Data data;

    public Integer getResult() { return result; }

    public void setResult(Integer result) {
        this.result = result;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }


}
