package com.example.ite2152_practicalexamination_e2145293;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Helper_SharedPreferences {
    Context context;
    private double latitude, longitude;
    private SharedPreferences sharedPreferences;

    public Helper_SharedPreferences(Context context) {
        this.context = context;
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("ITE2152_Practical_Test_E2145293", MODE_PRIVATE);
            this.latitude = Double.parseDouble(sharedPreferences.getString("latitude", "0"));
            this.longitude = Double.parseDouble(sharedPreferences.getString("longitude", "0"));
        }
    }

    public String getLatitude() {
        Log.d("SharedPreferences: latRead-", this.latitude + "");
        return Double.toString(this.latitude);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        Log.d("SharedPreferences: lat-", latitude + "");
        sharedPreferences.edit().putString("latitude", Double.toString(latitude)).apply();
    }

    public String getLongitude() {
        Log.d("SharedPreferences: lonRead-", this.longitude + "");
        return Double.toString(this.longitude);
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        Log.d("SharedPreferences: lon-", longitude + "");
        sharedPreferences.edit().putString("longitude", Double.toString(longitude)).apply();
    }
}