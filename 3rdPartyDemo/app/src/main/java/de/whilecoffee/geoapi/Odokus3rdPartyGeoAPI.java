package de.whilecoffee.geoapi;

/**
 * Created by jd on 19.09.17.
 *  Copyright © 2017 whileCoffee Software Development - Johannes Dürr. All rights reserved.
 *
 *
 *             .__    .__.__         _________         _____  _____
 *     __  _  _|  |__ |__|  |   ____ \_   ___ \  _____/ ____\/ ____\____   ____
 *     \ \/ \/ /  |  \|  |  | _/ __ \/    \  \/ /  _ \   __\\   __\/ __ \_/ __ \
 *      \     /|   Y  \  |  |_\  ___/\     \___(  <_> )  |   |  | \  ___/\  ___/
 *       \/\_/ |___|  /__|____/\___  >\______  /\____/|__|   |__|  \___  >\___  >
 *                  \/             \/        \/                        \/     \/
 *     Released under MIT License for Hansenhof _electronic
 *
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import android.app.Activity;
import com.thetransactioncompany.jsonrpc2.client.RawResponse;
import com.thetransactioncompany.jsonrpc2.client.RawResponseInspector;

// The Client sessions package
import com.thetransactioncompany.jsonrpc2.client.*;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;

// The Base package for representing JSON-RPC 2.0 messages
import com.thetransactioncompany.jsonrpc2.*;
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator;

// The JSON Smart package for JSON encoding/decoding (optional)
import net.minidev.json.*;

import java.net.URL;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Odokus3rdPartyGeoAPI {

    JSONRPC2Session mySession = null;
    JSONRPC2Response response = null;

    String userName = null;
    String userPass = null;
    Activity act = null;
    URL serverURL = null;

    private List<OdokusGeoApiListener> listeners;

    public Odokus3rdPartyGeoAPI(Activity activity, String userName, String userPass) {
        this.act = activity;
        this.userName = userName;
        this.userPass = userPass;

        // The JSON-RPC 2.0 server URL
        try {
            serverURL = new URL("https://developer.odokus.de/odokus2/json-rpc");
        }catch (Exception e){
            e.printStackTrace();
        }

        // Create new JSON-RPC 2.0 client session
        mySession = new JSONRPC2Session(serverURL);
        String[] rct = {"application/json", "application/json-rpc", "application/jsonrequest"};
        mySession.getOptions().setAllowedResponseContentTypes(rct);
        mySession.setConnectionConfigurator(new BasicAuthenticator());
        mySession.setRawResponseInspector(new MyInspector());

        checkPermissionACCESS_FINE_LOCATION();
        checkPermissionACCESS_COARSE_LOCATION();
        checkPermissionExternalStorage();

        listeners = new ArrayList<>();
    }

    public void setOdokusGeoApiListener(OdokusGeoApiListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(OdokusGeoApiListener listener){
        this.listeners.remove(listener);
    }

    public void removeAllListeners(){
        this.listeners.clear();
    }

    public class BasicAuthenticator implements ConnectionConfigurator {

        public void configure(HttpURLConnection connection) {
            String authString = Base64.encodeToString((String.format("%s:%s",userName,userPass)).getBytes(), Base64.DEFAULT);
            System.out.println(authString);
            connection.addRequestProperty("Authorization", "Basic " + authString);
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setUseCaches(false);
        }
    }

    public Date dateFromOdokusDateString(String odokusDateString)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'.000+0000'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date d = null;
        try {
            d = sdf.parse(odokusDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public String odokusDateStringFromDate(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'.000+0000'");
        String resultString = null;
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        resultString = sdf.format(date);
        return resultString;
    }
    public class MyInspector implements RawResponseInspector {

        public void inspect(RawResponse response) {

            // print the HTTP status code
            System.out.println("HTTP status: " + response.getStatusCode());

            // print the value of the "Date" HTTP header
            System.out.println("Date: " + response.getHeaderField("Date"));
        }
    }

    private void checkPermissionExternalStorage(){
        if (ContextCompat.checkSelfPermission(act,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(act,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                //@TODO: Add code to show the User why your Application needs access to ext storage

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(act,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    private void checkPermissionACCESS_COARSE_LOCATION(){
        if (ContextCompat.checkSelfPermission(act,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(act,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                //@TODO: Add code to show the User why your Application needs access to location

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(act,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1);
            }
        }
    }

    private void checkPermissionACCESS_FINE_LOCATION(){
        if (ContextCompat.checkSelfPermission(act,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(act,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //@TODO: Add code to show the User why your Application needs access to location

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(act,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        }
    }

    public void getGeoEvents(Date startDate, Date endDate)
    {
        String method = "getGeoEvents";
        int requestID = 32;
        Map<String, Object> params = new HashMap<String, Object>();
        List<Object> paramList = new ArrayList<>();

        String stString = odokusDateStringFromDate(startDate);
        String edString = odokusDateStringFromDate(endDate);

        params.put("startDate", stString );
        params.put("endDate", edString);
        paramList.add(params);

        JSONRPC2Request request = new JSONRPC2Request(method, paramList, requestID);
        try {
            response = mySession.send(request);

        } catch (JSONRPC2SessionException e) {

            System.err.println(e.getMessage());
            // handle exception...
        }

        // Print response result / error
        if (response != null) {
            if (response.indicatesSuccess()) {

                System.out.println(response.getResult());
                List<GeoEvent> resultList = new ArrayList<>();
                JSONArray respArray = (JSONArray) response.getResult();
                if (respArray != null && respArray.size() > 0)
                {
                    for (Object jo: respArray)
                    {
                        JSONObject o = (JSONObject) jo;
                        if (o != null)
                        {
                            double lat = (double) o.get("latitude");
                            double lon = (double) o.get("longitude");
                            String sdate = (String)o.get("date");
                            Date date = dateFromOdokusDateString(sdate);
                            Map<String, String> extensions = (Map<String, String>) o.get("extensions");

                            GeoEvent e = new GeoEvent(lat, lon, "Coffee", "I grabbed some coffee here", date, extensions);
                            resultList.add(e);
                        }
                    }
                    for (OdokusGeoApiListener l: listeners
                         ) {
                        if (l != null){
                            l.receivedGeoEvents(resultList);
                        }
                    }
                }


                System.out.println("done");
            } else {
                System.out.println(response.getError().getMessage());
            }
        }
    }

    public void setGeoEvent(GeoEvent evt, String api_type_string)
    {
        String method = "saveGeoEvent";
        int requestID = 33;
        Map<String, Object> params = new HashMap<String, Object>();
        List<Object> paramList = new ArrayList<>();

        String dateString = odokusDateStringFromDate(evt.getEventDate());

        params.put("type", api_type_string );
        params.put("date", dateString);
        params.put("longitude", evt.getLongitude());
        params.put("latitude", evt.getLatitude());
        params.put("extensions", evt.getExtensions());

        paramList.add(params);

        JSONRPC2Request request = new JSONRPC2Request(method, paramList, requestID);
        try {
            response = mySession.send(request);

        } catch (JSONRPC2SessionException e) {

            System.err.println(e.getMessage());
            // handle exception...
        }

        // Print response result / error
        if (response != null) {
            if (response.indicatesSuccess()) {

                System.out.println(response.getResult());
                System.out.println("done");
            } else {
                System.out.println(response.getError().getMessage());
            }
        }
    }

    public interface OdokusGeoApiListener {
        public void receivedGeoEvents(List<GeoEvent>events);
    }

}
