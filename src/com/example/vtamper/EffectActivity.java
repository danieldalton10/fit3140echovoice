package com.example.vtamper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.vtamper.AudioClip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EffectActivity extends Activity
{
    private final String TAG = "VTamper";
    private final String VTAMPER_DIR = "vtamper-clips";
    private AudioClip audioClip;
    private Context context;
    private String loadFilePath;
    private boolean echo = false;
    private boolean reversed = false;
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
        int duration = Toast.LENGTH_SHORT;
        CharSequence text;
        audioClip.selectEffect(AudioClip.Option.REVERSE, audioClip.new EffectArguments ());
        reversed = !reversed; // reversing the reverse track gives back the normal original track
        if (reversed) {
            text = "The track has been reversed";
        } else {
            text = "The track is no longer reversed";
        }
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void onEcho (View view) {
        int duration = Toast.LENGTH_SHORT;
        CharSequence text = "Added echo effect";
        audioClip.selectEffect(AudioClip.Option.ECHO, audioClip.new EffectArguments ().setVolume ((float) 0.3));
        echo = true;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void onSave (View view) {
        int duration = Toast.LENGTH_SHORT;
        CharSequence text;

        File path = Environment.getExternalStoragePublicDirectory(
                                                                  VTAMPER_DIR);
        String filename = "vtamper_clip_" + System.currentTimeMillis() +
                                                                  ".wav";
        File file = new File(path, filename);

        try {
            // Make sure the vtamper directory exists.
            path.mkdirs();
            audioClip.write (file);
            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(this,
                                            new String[] { file.toString() }, null,
                                            new MediaScannerConnection.OnScanCompletedListener() {
                                                public void onScanCompleted(String path, Uri uri) {
                                                    Log.i(TAG, "Scanned " + path + ":");
                                                    Log.i(TAG, "-> uri=" + uri);
                                                }
                                            });
            text = "Clip saved.";
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w(TAG, "Error writing " + file, e);
            text = "Failed to write file.";
        }
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
