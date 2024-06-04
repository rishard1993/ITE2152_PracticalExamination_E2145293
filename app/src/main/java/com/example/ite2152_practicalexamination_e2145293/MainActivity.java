package com.example.ite2152_practicalexamination_e2145293;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
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

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button btnApi, btnLocation;
    String TAG = "WeatherRequest";
    RequestQueue queue;
    Timer timer;
    LocationManager locationManager;

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
        textView = findViewById(R.id.textView);
        btnApi = findViewById(R.id.buttonRequest);
        btnLocation = findViewById(R.id.buttonLocation);


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
                        textView.setText(Calendar.getInstance().getTime().toString());
                    }
                });
            }
        };
        //schedule the task to run periodically
        timer.schedule(task, 0, 1000);


        // Instantiate the RequestQueue.
        //RequestQueue queue = Volley.newRequestQueue(this);
        queue = Helper_Api.getInstance(this.getApplicationContext()).
                getRequestQueue();

        String url = "https://api.weatherapi.com/v1/current.json?q=7.247431377255542,80.47446904634002&key=d03b1eb170264c5e8ec10840240406";

        btnApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                textView.setText("Response is: " + response.substring(0, 500));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textView.setText("Error: " + error.getMessage());
                    }
                });

                // Set the tag on the request.
                stringRequest.setTag(TAG);

                // Add the request to the RequestQueue.
                //queue.add(stringRequest);

                // Add a request (in this example, called stringRequest) to your RequestQueue.
                Helper_Api.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
            }
        });


        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        Toast.makeText(MainActivity.this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_LONG).show();
                        locationManager.removeUpdates(this);  // Remove updates to get location only once
                    }
                };

                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.getMainLooper());
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to find location.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}