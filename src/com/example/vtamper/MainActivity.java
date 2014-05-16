package com.example.vtamper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity
{
    private final String TAG = "VTAMPER";
    final int PICK_WAVE = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void onLoad (View view) {
        // File browser 
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("audio/*"); // Doesn't really work for some reason
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, PICK_WAVE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
        case PICK_WAVE: {
            if (resultCode == RESULT_OK){
                Uri uri = data.getData();
                goToEffect (uri.toString ());
            }
        }
        }
    }

    void goToEffect (String path) {
        Intent intent = new Intent (this, EffectActivity.class);
        intent.putExtra("filePath", path);
        startActivity (intent);
    }

    public void onRecord (View view) {
        Intent intent = new Intent (this, RecordActivity.class);
        startActivity (intent);        
    }
}

