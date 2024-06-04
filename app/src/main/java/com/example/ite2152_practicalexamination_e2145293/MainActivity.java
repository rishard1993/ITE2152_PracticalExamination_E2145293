package com.example.ite2152_practicalexamination_e2145293;

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

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    TextView txtCoordinates, txtAddress, txtTime, txtCondition, txtTemperature, txtHumidity, txtLocation, txtDescription;
    ImageView imageIcon;
    Button btnRefresh, btnExit;
    String TAG = "WeatherRequest";
    RequestQueue queue;
    Timer timer;
    LocationManager locationManager;
    Helper_SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the UI elements
        txtCoordinates = findViewById(R.id.txtCoordinates);
        txtAddress = findViewById(R.id.txtAddress);
        txtTime = findViewById(R.id.txtTime);
        txtCondition = findViewById(R.id.txtCondition);
        txtTemperature = findViewById(R.id.txtTemperature);
        txtHumidity = findViewById(R.id.txtHumidity);
        txtLocation = findViewById(R.id.txtLocation);
        txtDescription = findViewById(R.id.txtDescription);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnExit = findViewById(R.id.btnExit);
        imageIcon = findViewById(R.id.imageIcon);

        //Initialize Shared Preferences
        sharedPreferences = new Helper_SharedPreferences(getApplicationContext());
        //Even though shared preferences were intended to store coordinates, and they were not used, because of performance and concurrency issues.

        //check Location Permissions
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
        // Initialize the location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Instantiate the timer
        timer = new Timer();
        //Create the task to update time
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                    }
                });
            }
        };
        //schedule the task to run periodically
        timer.schedule(task, 0, 500);

        // Instantiate the RequestQueue.
        queue = Helper_Api.getInstance(this.getApplicationContext()).
                getRequestQueue();
        //Load weather data at launch
        updateLocation();

        //Event handler for the refresh button
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Start Refreshing...", Toast.LENGTH_SHORT).show();
                updateLocation();
                Toast.makeText(getApplicationContext(), "Refresh Complete.", Toast.LENGTH_SHORT).show();
            }
        });

        //Event handler for the exit button
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
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

    private void updateLocation() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(), "Please turn on GPS.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Location Permissions Not Available.", Toast.LENGTH_SHORT).show();
            return;
        }
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                //update value of txtCoordinates
                txtCoordinates.setText("Latitude: " + latitude + "\nLongitude: " + longitude);

                //update value of txtAddress
                txtAddress.setText(getReverseGeocodedData(latitude, longitude));

                //call refresh method to update weather data
                refreshWeatherData(latitude,longitude);

                //Store Coordinates in Shared Preferences
                sharedPreferences.setLatitude(latitude);
                sharedPreferences.setLongitude(longitude);
                //Remove updates to get location only once
                locationManager.removeUpdates(this);
            }
        };
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.getMainLooper());
        } else {
            Toast.makeText(getApplicationContext(), "Unable to find location.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getReverseGeocodedData(double latitude, double longitude) {
        Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            Log.d("GeoData : Start: ", "---");
            List<Address> addresses = geoCoder.getFromLocation(latitude,longitude,1);
            String addressText = "";
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                addressText = address.getAddressLine(0);
            }
            return addressText;
        }
        catch (IOException e) {
            return "Geocoder Error: " + e.getMessage();
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
                        String strCondition = "Press Refresh to get weather --->";
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
                        } catch (JSONException error) {
                            Toast.makeText(getApplicationContext(), "JSON Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        txtLocation.setText(strLocation);
                        txtCondition.setText(strCondition);
                        txtTemperature.setText(strTemperature);
                        txtHumidity.setText(strHumidity);
                        txtDescription.setText(strDescription);

                        // Use Glide to load the image from the URL
                        Glide.with(MainActivity.this)
                                .load(strIconUrl)
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.baseline_wb_cloudy_24) // Placeholder image
                                        .error(R.drawable.baseline_wb_cloudy_24) // Error image in case of loading failure
                                )
                                .into(imageIcon);
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

}