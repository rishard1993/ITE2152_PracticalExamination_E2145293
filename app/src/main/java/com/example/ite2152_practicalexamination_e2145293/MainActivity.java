package com.example.ite2152_practicalexamination_e2145293;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button button;
    String TAG = "WeatherRequest";
    RequestQueue queue;
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

         textView = findViewById(R.id.textView);
         button = findViewById(R.id.buttonRequest);


        // Instantiate the RequestQueue.
        //RequestQueue queue = Volley.newRequestQueue(this);
        queue = Helper_Api.getInstance(this.getApplicationContext()).
                getRequestQueue();

        String url = "https://api.weatherapi.com/v1/current.json?q=7.247431377255542,80.47446904634002&key=d03b1eb170264c5e8ec10840240406";

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                textView.setText("Response is: " + response.substring(0,500));
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




    }


    @Override
    protected void onStop() {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}