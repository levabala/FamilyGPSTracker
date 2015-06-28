package com.gps.lev.gps;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Array;
import java.util.ArrayList;


public class MainActivity22Activity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity22);

        LinearLayout historyLayout = (LinearLayout)findViewById(R.id.HistoryLineralLayout);
        String history = getIntent().getStringExtra("History");
        if (history == "") {
            super.finish();
        }
        Toast toast = Toast.makeText(getApplicationContext(), history, Toast.LENGTH_SHORT);
        toast.show();

        TextView v = new TextView(this);
        v.setText(history);
        historyLayout.addView(v);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity22, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    

    private String formatLocation(Location location){
        if (location == null) return "";
        return String.format("Coordinates: lat=%1$.4f, lon=%2$.4f, time=%3$tf %3$tT", location.getLatitude(), location.getLongitude(), location.getTime());
    }
}
