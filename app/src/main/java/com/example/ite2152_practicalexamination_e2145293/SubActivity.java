package com.example.ite2152_practicalexamination_e2145293;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SubActivity extends AppCompatActivity {
    TextView subTxtSearch, subTxtAddress, subTxtTime, subTxtCondition, subTxtTemperature, subTxtHumidity, subTxtLocation, subTxtDescription;
    ImageView subImageIcon;
    Button subBtnSearch, subBtnBack;
    String TAG = "WeatherRequestSub";
    RequestQueue queue;
    Timer subTimer;
    LocationManager locationManager;
    Helper_SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sub);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the UI elements
        subTxtSearch = findViewById(R.id.subEditTextSearch);
        subTxtAddress = findViewById(R.id.subTxtAddress);
        subTxtDescription = findViewById(R.id.subTxtDescription);
        subTxtLocation = findViewById(R.id.subTxtLocation);
        subTxtCondition = findViewById(R.id.subTxtCondition);
        subTxtHumidity = findViewById(R.id.subTxtHumidity);
        subTxtTemperature = findViewById(R.id.subTxtTemperature);
        subTxtTime = findViewById(R.id.subTxtTime);
        subImageIcon = findViewById(R.id.subImageIcon);
        subBtnSearch = findViewById(R.id.subBtnSearch);
        subBtnBack = findViewById(R.id.subBtnBack);

        //Initialize Shared Preferences
        sharedPreferences = new Helper_SharedPreferences(getApplicationContext());

        double latitude = Double.parseDouble(sharedPreferences.getLatitude());
        double longitude = Double.parseDouble(sharedPreferences.getLongitude());
        refreshWeatherData(latitude,longitude);

        //Instantiate the timer
        subTimer = new Timer();
        //Create the task to update time
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        subTxtTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                    }
                });
            }
        };
        //schedule the task to run periodically
        subTimer.schedule(task, 0, 500);

        // Instantiate the RequestQueue.
        queue = Helper_Api.getInstance(this.getApplicationContext()).
                getRequestQueue();

        subBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SubActivity.this, "Search Clicked! Search: " + subTxtSearch.getText().toString(), Toast.LENGTH_SHORT).show();
                getGeocodedData(subTxtSearch.getText().toString());
            }
        });

        subBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel all the pending requests at app close
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

    private void refreshWeatherData(double latitude, double longitude) {
        String api_key = "d03b1eb170264c5e8ec10840240406";
        String url = "https://api.weatherapi.com/v1/current.json?q=" + latitude + "," + longitude + "&key=" + api_key;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String strLocation = "location not set";
                        String strCondition = "";
                        String strIconUrl = "";
                        String strTemperature = "--.-";
                        String strHumidity = "--";
                        String strDescription = "Waiting for API response...";

                        try {
                            JSONObject responseJson = new JSONObject(response);
                            JSONObject location = responseJson.getJSONObject("location");
                            strLocation = location.getString("name") + ", " + location.getString("country");
                            JSONObject current = responseJson.getJSONObject("current");
                            strCondition = current.getJSONObject("condition").getString("text");
                            strIconUrl = "https:" + current.getJSONObject("condition").getString("icon");
                            strTemperature = current.getString("temp_c") + "°C";
                            strHumidity = "Humidity is " + current.getString("humidity") + "%";
                            strDescription = "The weather is " + strCondition + " with a cloud cover of " + current.getString("cloud") + "%.";
                            strDescription += current.getString("wind_kph") + " km/h winds blow from " + current.getString("wind_dir") + " direction.";
                            strDescription += "The UV index is " + current.getString("uv") + " while net precipitation is " + current.getString("precip_mm") + "mm. \n[as at " + current.getString("last_updated") + "]";
                        } catch (JSONException error) {
                            Toast.makeText(getApplicationContext(), "JSON Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        subTxtLocation.setText(strLocation);
                        subTxtCondition.setText(strCondition);
                        subTxtTemperature.setText(strTemperature);
                        subTxtHumidity.setText(strHumidity);
                        subTxtDescription.setText(strDescription);

                        // Use Glide to load the image from the URL
                        Glide.with(SubActivity.this)
                                .load(strIconUrl)
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.baseline_wb_cloudy_24) // Placeholder image
                                        .error(R.drawable.baseline_wb_cloudy_24) // Error image in case of loading failure
                                )
                                .into(subImageIcon);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("HTTP Error", error.toString());
            }
        });

        // Set the tag on the request.
        stringRequest.setTag(TAG);

        // Add the request to the RequestQueue.
        Helper_Api.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void getGeocodedData(String locationName) {
        Geocoder geocoder = new Geocoder(SubActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                //display geocoded location
                subTxtAddress.setText("Latitude: " + latitude + ",\nLongitude: " + longitude);

                //Load weather data for the new location
                refreshWeatherData(latitude,longitude);

                //update shared preferences
                sharedPreferences.setLatitude(latitude);
                sharedPreferences.setLongitude(longitude);
            } else {
                subTxtAddress.setText("No coordinates found for the location.");
                subTxtTemperature.setText("--");
                subTxtCondition.setText("");
                subTxtHumidity.setText("");
                subTxtLocation.setText("Please Try Again.");
                subTxtDescription.setText("");
                subImageIcon.setImageResource(R.drawable.baseline_wb_cloudy_24);
            }
        } catch (Exception exception) {
            Log.d("SubActivity:Geocoder", "Geocoder Error: " + exception.getMessage());
        }
    }
}