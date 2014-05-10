package com.example.vtamper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
    private String loadFilePath;
    final int PICK_WAVE = 1;

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
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("audio/x-wav/*"); // Doesn't really work for some reason
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, PICK_WAVE);
    }

    void handleLoadFile (String filename) {
        AudioClip.AudioFile audioFile = audioClip.new AudioFile ();
        int duration = Toast.LENGTH_SHORT;
        CharSequence text = "";
        if (filename.equals ("")) {
            text = "An error occurred loading the file.";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish ();
        }

        try {
            audioFile.loadFile (filename);
        } catch (FileNotFoundException e) {
            text = "The file was not found.";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish ();
        } catch (IOException e) {
            text = "An IO Error has occurred.";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish ();
        } catch (Exception e) { // All exceptions are the same to us the file didn't load in any case
            text = "There was a problem loading the file";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish ();
        } finally {
            text = "Audio loaded";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
        case PICK_WAVE: {
            if (resultCode == RESULT_OK){
                Uri uri = data.getData();
                String filePath = uri.getPath();
                Log.d(TAG, "path = "+filePath);
                handleLoadFile (filePath);
                return;
            }
        }
        }
        handleLoadFile (""); // A way of handling errors
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
