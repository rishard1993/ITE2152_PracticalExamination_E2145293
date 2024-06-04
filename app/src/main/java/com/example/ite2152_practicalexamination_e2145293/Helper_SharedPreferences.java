package com.example.ite2152_practicalexamination_e2145293;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class Helper_SharedPreferences {
    Context context;
    private double latitude, longitude;
    private SharedPreferences sharedPreferences;

    public Helper_SharedPreferences(Context context) {
        this.context = context;
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("ITE2152_Practical_Test_E2145293", MODE_PRIVATE);
            this.latitude = sharedPreferences.getFloat("latitude", 0);
            this.longitude = sharedPreferences.getFloat("longitude", 0);
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        sharedPreferences.edit().putFloat("latitude", (float) latitude).apply();
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        sharedPreferences.edit().putFloat("longitude", (float) longitude).apply();
    }
}