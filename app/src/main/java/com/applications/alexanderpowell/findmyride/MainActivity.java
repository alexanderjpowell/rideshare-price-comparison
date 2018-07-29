package com.applications.alexanderpowell.findmyride;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.location.Location;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.Manifest;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.widget.Toast;
import android.content.Context;

import java.util.HashMap;

import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.rides.client.services.RidesService;
import com.uber.sdk.rides.client.UberRidesApi;

import com.lyft.lyftbutton.LyftButton;
import com.lyft.lyftbutton.RideParams;
import com.lyft.lyftbutton.RideTypeEnum;
import com.lyft.networking.ApiConfig;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    public String TAG = "log";

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    public boolean pickupTextInProgress = false;
    public boolean dropoffTextInProgress = false;

    // Global address data
    public double pickupLatitude;
    public double pickupLongitude;
    public String pickupNickname;
    public String pickupFormattedAddress;

    public double dropoffLatitude;
    public double dropoffLongitude;
    public String dropoffNickname;
    public String dropoffFormattedAddress;

    public String product_id;

    public UberApiHandler uberApi;

    RideRequestButton rideRequestButton;
    LyftButton lyftButton;

    EditText pickupEditText;// = (EditText)findViewById(R.id.pickup_address_edit_text);
    EditText dropoffEditText;// = (EditText)findViewById(R.id.dropoff_address_edit_text);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pickupEditText = (EditText)findViewById(R.id.pickup_address_edit_text);
        dropoffEditText = (EditText)findViewById(R.id.dropoff_address_edit_text);

        rideRequestButton = findViewById(R.id.uber_button);
        lyftButton = (LyftButton)findViewById(R.id.lyft_button);

        // Set EditText setOnTouchListeners
        EditText pickupEditText = (EditText)findViewById(R.id.pickup_address_edit_text);
        pickupEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pickupTextInProgress = true;
                dropoffTextInProgress = false;
                return false;
            }
        });

        EditText dropoffEditText = (EditText)findViewById(R.id.dropoff_address_edit_text);
        dropoffEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dropoffTextInProgress = true;
                pickupTextInProgress = false;
                return false;
            }
        });

        pickupEditText.setText("");
        dropoffEditText.setText("");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //pickupEditText = (EditText)findViewById(R.id.pickup_address_edit_text);
        //dropoffEditText = (EditText)findViewById(R.id.dropoff_address_edit_text);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                if (pickupTextInProgress) {
                    pickupEditText.setText(place.getName());
                    this.pickupLatitude = place.getLatLng().latitude;
                    this.pickupLongitude = place.getLatLng().longitude;
                    this.pickupNickname = place.getName().toString();
                    this.pickupFormattedAddress = place.getAddress().toString();
                }
                else if (dropoffTextInProgress) {
                    dropoffEditText.setText(place.getName());
                    this.dropoffLatitude = place.getLatLng().latitude;
                    this.dropoffLongitude = place.getLatLng().longitude;
                    this.dropoffNickname = place.getName().toString();
                    this.dropoffFormattedAddress = place.getAddress().toString();
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());
                Log.i(TAG, "RESULT_ERROR");

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i(TAG, "RESULT_CANCELED");
            }
        }
    }

    // Sends API requests and configures Uber and Lift ride request buttons in background thread
    public void findRidesButtonOnClick(View view) {
        try {
            if ((pickupEditText.getText().toString().equals("")) || (dropoffEditText.getText().toString().equals(""))) {

                Context context = getApplicationContext();
                CharSequence text = "Please enter both a pickup and destination location";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            AsyncTaskRunner runner = new AsyncTaskRunner();
            runner.execute(Double.toString(pickupLatitude), Double.toString(pickupLongitude));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Reset the EditText boxes
    public void resetButtonOnClick(View view) {
        try {
            rideRequestButton.setVisibility(View.INVISIBLE);
            lyftButton.setVisibility(View.INVISIBLE);
            pickupEditText.setText("");
            dropoffEditText.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void launchAddressAutocompleteIntent(View v) {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
            startActivityForResult(intent, 1);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
            Log.i(TAG, "GooglePlayServicesRepairableException");
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
            Log.i(TAG, "GooglePlayServicesNotAvailableException");
        }
    }

    public void configureUberButton(String productId) {

        SessionConfiguration config = new SessionConfiguration.Builder()
                // mandatory
                .setClientId("R6etsUC87XYKkcAMiLA4enSu5_E9rmWa")
                // required for enhanced button features
                .setServerToken("pgX3-Nj_Kv605V4t_W8xY7h-DnAs9ynf8VgbBnGq")
                // required for implicit grant authentication
                //.setRedirectUri("<REDIRECT_URI>")
                //.setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
                // optional: set sandbox as operating environment
                //.setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build();

        UberSdk.initialize(config);

        //RideRequestButton requestButton = findViewById(R.id.uber_button);

        RideParameters rideParams = new RideParameters.Builder()
                // Optional product_id from /v1/products endpoint (e.g. UberX). If not provided, most cost-efficient product will be used
                .setProductId(productId)
                .setPickupLocation(this.pickupLatitude, this.pickupLongitude, this.pickupNickname, this.pickupFormattedAddress)
                .setDropoffLocation(this.dropoffLatitude, this.dropoffLongitude, this.dropoffNickname, this.dropoffFormattedAddress)
                .build();
        rideRequestButton.setRideParameters(rideParams);

        ServerTokenSession session = new ServerTokenSession(config);
        rideRequestButton.setSession(session);
        rideRequestButton.loadRideInformation();

        RidesService service = UberRidesApi.with(session).build().createService();
    }

    public void configureLyftButton(int rideType) {

        ApiConfig apiConfig = new ApiConfig.Builder()
                .setClientId("BK5m_FfGAQyF")
                .setClientToken("u5B0QFVyzwdrCP1+HZ4xY3A2IHMfXWnLUvp72mTHUjbZmh7Nw+KSHQUWK5E3mUK3A/bpRETxceLdZN6DYgIg9efWj9LwivJNBvOKymmx7UkYOhk+H27Xeis=")
                .build();

        //LyftButton lyftButton = (LyftButton)findViewById(R.id.lyft_button);
        lyftButton.setApiConfig(apiConfig);

        RideParams.Builder rideParamsBuilder = new RideParams.Builder()
                .setPickupLocation(this.pickupLatitude, this.pickupLongitude)
                .setDropoffLocation(this.dropoffLatitude, this.dropoffLongitude);

        if (rideType == 1)
            rideParamsBuilder.setRideTypeEnum(RideTypeEnum.LINE);
        else if (rideType == 2)
            rideParamsBuilder.setRideTypeEnum(RideTypeEnum.CLASSIC);
        else if (rideType == 3)
            rideParamsBuilder.setRideTypeEnum(RideTypeEnum.PLUS);
        else if (rideType == 4)
            rideParamsBuilder.setRideTypeEnum(RideTypeEnum.PLUS);

        lyftButton.setRideParams(rideParamsBuilder.build());
        lyftButton.load();

    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        int rideType;

        @Override
        protected String doInBackground(String... params) {
            String ret = "";
            try {
                String request = "https://api.uber.com/v1.2/products?latitude=" + params[0] + "&longitude=" + params[1];
                URL url = new URL(request);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");

                Log.i(TAG, "request: " + request);

                // Set headers
                connection.setRequestProperty("Accept-Language", "en_US");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer KA.eyJ2ZXJzaW9uIjoyLCJpZCI6IktaVnVOSnNEUWRPcHN3ZVdzYWFRNmc9PSIsImV4cGlyZXNfYXQiOjE1MzM2MDI2MDIsInBpcGVsaW5lX2tleV9pZCI6Ik1RPT0iLCJwaXBlbGluZV9pZCI6MX0.0CM-AA1pRaMQPgFrsQIlCvQzWtT9zR1GlUBx7oELyEA");

                int responseCode = connection.getResponseCode();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();

                ret = response.toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return ret;
        }

        @Override
        protected void onPostExecute(String result) {

            uberApi = new UberApiHandler(result);

            RadioGroup radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
            Integer radioButtonId = radioGroup.getCheckedRadioButtonId();
            if (radioButtonId == R.id.button1) {
                product_id = uberApi.getProduct_id_uber_pool();
                rideType = 1;
            } else if (radioButtonId == R.id.button2) {
                product_id = uberApi.getProduct_id_uber_x();
                rideType = 2;
            } else if (radioButtonId == R.id.button3) {
                product_id = uberApi.getProduct_id_uber_xl();
                rideType = 3;
            } else if (radioButtonId == R.id.button4) {
                product_id = uberApi.getProduct_id_uber_black();
                rideType = 4;
            }

            rideRequestButton.setVisibility(View.VISIBLE);
            lyftButton.setVisibility(View.VISIBLE);

            configureUberButton(product_id);
            configureLyftButton(rideType);
        }
    }
}
