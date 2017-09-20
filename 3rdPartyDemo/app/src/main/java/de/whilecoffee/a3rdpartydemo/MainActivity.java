package de.whilecoffee.a3rdpartydemo;

import android.app.Activity;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.whilecoffee.geoapi.GeoEvent;
import de.whilecoffee.geoapi.Odokus3rdPartyGeoAPI;
import de.whilecoffee.geoapi.Odokus3rdPartyGeoAPI.OdokusGeoApiListener;

public class MainActivity extends AppCompatActivity implements OdokusGeoApiListener{

    Odokus3rdPartyGeoAPI geoAPI;

    // UI Elements
    private EditText loginNameEditText;
    private EditText loginPassEditText;
    private EditText resultEditText;
    private Button startApiTestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // update thread policy for demo
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // setup UI
        loginNameEditText = (EditText) findViewById(R.id.main_userNameEditText);
        loginPassEditText = (EditText) findViewById(R.id.main_passwordEditText);
        resultEditText    = (EditText) findViewById(R.id.main_resultEditText);

        // Test Button and listener
        startApiTestButton = (Button) findViewById(R.id.main_StartTestButton);
        startApiTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                odokus_api_init();
            }
        });
    }

    private void odokus_api_init()
    {
        // instantiate geoApi
        if (geoAPI == null)
        {
            geoAPI = new Odokus3rdPartyGeoAPI(this, loginNameEditText.getText().toString(),
                    loginPassEditText.getText().toString());
            // add ourself as listener
            geoAPI.setOdokusGeoApiListener(this);
        }



        // make api call to receive events for the last 3 days.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -3);
        Date start = cal.getTime();
        Date end = new Date();

        // make request with geoApi
        geoAPI.getGeoEvents(start, end);
    }

    @Override
    public void receivedGeoEvents(List<GeoEvent> events) {
        for (GeoEvent e : events){
            resultEditText.setText(String.format("%s \n%s \n%s \n\n",
                    e.getEventDate().toString(), e.getEventName(), e.getEventDescription()));
        }
    }
}
