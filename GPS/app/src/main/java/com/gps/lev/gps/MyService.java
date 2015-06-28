package com.gps.lev.gps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyService extends Service {
    public MyService() {
    }

    private LocationManager locationManager;
    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, locationListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Служба получения геоданных запущена...", Toast.LENGTH_LONG).show();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Служба получения геоданных остановлена...", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            String s = formatLocation(location);
            WriteHistory(s);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void WriteHistory(String s){
        FileOutputStream fos;
        try{
            fos = openFileOutput("Locations.data", Context.MODE_PRIVATE);
            fos.write(s.getBytes());
            fos.close();
        }
        catch (java.io.FileNotFoundException e){
            Toast.makeText(this,"Ошибка чтения файла\n", Toast.LENGTH_LONG).show();
        }
        catch (java.io.IOException e){
            Toast.makeText(this,"Ошибка записи в файл\n", Toast.LENGTH_LONG).show();
        }
    }
    private String formatLocation(Location location){
        if (location == null) return "";

        String s = "";
        try {
            InputStream is = openFileInput("Locations.data");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder string = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                string.append(line);
            }
            s = string.toString() + "<trkpt lat=\"" + location.getLatitude() + "\" lon=\"" + location.getLongitude() + "\">";
            SimpleDateFormat[] formats = new SimpleDateFormat[]{
                    new SimpleDateFormat("yyyy-MM-dd"),
                    new SimpleDateFormat("kk:mm:ss")
            };
            s += "<time>" +
                    (formats[0].format(new Date(location.getTime()))) +
                    "T" +
                    (formats[1].format(new Date(location.getTime()))) +
                    "Z</time>";
            s += "<fix>2d</fix>";
            Bundle b = location.getExtras();
            s += "</trkpt>";
            is.close();
            return s;
        }
        catch (java.io.IOException e){
            Toast.makeText(this,"Ошибка чтения файла\n", Toast.LENGTH_LONG).show();
        }
        return "";
    }

    private String formatDate(Date d){

        String date = "";
        date += String.valueOf(d.getYear()) + '-' + String.valueOf(d.getMonth()) + '-' + String.valueOf(d.getDay()) + 'T' + String.valueOf(d.getHours()) + ':' + String.valueOf(d.getMinutes()) + ':' + String.valueOf(d.getSeconds()) + 'Z';
        return date;
    };
}
