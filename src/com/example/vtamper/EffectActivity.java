package com.example.vtamper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.vtamper.AudioClip;

import java.io.FileNotFoundException;
import java.io.IOException;

public class EffectActivity extends Activity
{
    private final String TAG = "VTamper";
    private AudioClip audioClip;
    private Context context;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.effect);
        context = getApplicationContext();
        audioClip = new AudioClip ();
        load ();
    }

    void load () {
        // TODO: Do some stuff display a file picker and save the path to the filename variable
        String filename = "/sdcard/original.wav";
        AudioClip.AudioFile audioFile = audioClip.new AudioFile ();
        int duration = Toast.LENGTH_SHORT;
        CharSequence text = "";
        try {
            audioFile.loadFile (filename);
        } catch (FileNotFoundException e) {
            text = "File not found";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish ();
        } catch (IOException e) {
            text = "An IO error occurred.";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish ();
        }
    }

    public void onReverse (View view) {
        audioClip.selectEffect(AudioClip.Option.REVERSE, audioClip.new EffectArguments ());
        Log.d(TAG, "Made reverse");
    }

    public void onEcho (View view) {
        audioClip.selectEffect(AudioClip.Option.ECHO, audioClip.new EffectArguments ().setVolume ((float) 0.3));
        Log.d(TAG, "Made echo");
    }

    public void onSave (View view) {
        // TODO: file picker choose where to save etc.
        String filename = "/sdcard/output.wav";
        int duration = Toast.LENGTH_SHORT;
        CharSequence text = "";
        try {
            audioClip.write (filename);
            Log.d(TAG, "Saved file successfully");
            text = "wrote file.";
        } catch (IOException e) {
            Log.d(TAG, "Failed to save file.");
            text = "Failed to write file.";
        }
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
