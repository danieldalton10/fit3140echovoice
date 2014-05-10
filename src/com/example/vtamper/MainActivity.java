package com.example.vtamper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void onLoad (View view) {
        Intent intent = new Intent (this, EffectActivity.class);
        startActivity (intent);
    }

    public void onRecord (View view) {
        return;
    }
}
