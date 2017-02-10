package com.kkontus.cloudcamera.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.kkontus.cloudcamera.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Intent intent;

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_dropbox) {
            Toast.makeText(this, "Dropbox", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, DropboxActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_google_plus) {
            Toast.makeText(this, "Google+", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, GooglePlusActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_one_drive) {
            Toast.makeText(this, "OneDrive", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, OneDriveActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_box) {
            Toast.makeText(this, "Box", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, BoxActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_facebook) {
            Toast.makeText(this, "Facebook", Toast.LENGTH_SHORT).show();
            intent = new Intent(this, FacebookActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
