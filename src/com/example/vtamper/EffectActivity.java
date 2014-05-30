package com.example.vtamper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vtamper.AudioClip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

public class EffectActivity extends Activity
{
    private final String TAG = "VTamper";
    private final String VTAMPER_DIR = "VTamper clips";
    private AudioClip audioClip;
    private Context context;
    private boolean echo = false;
    private boolean reversed = false;
    private boolean startPlay = true;
    private MediaPlayer audioPlayer;
    private File tmpFile = null;
    final String TMP_FILE = "vtamper_clip_shared.wav";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.effect);
        context = getApplicationContext();
        audioClip = new AudioClip ();
        Intent intent = getIntent();
        String path = intent.getExtras().getString("filePath");
        loadFile (path);
    }

    @Override
    public void onDestroy () {
        if (tmpFile != null) {
            try {
                tmpFile.delete ();
            } catch (Exception e) {
                // should never get here, but don't worry about it
            }
        }
        super.onDestroy ();
    }
    
    /**
     * Called from the MainActivity onLoad after a file is selected
     * @param filename
     */

    void loadFile (String filename) {
        Log.d(TAG, "file is " + filename);
        boolean open = false;
        int duration = Toast.LENGTH_SHORT;
        CharSequence text = "";
        InputStream is = null;
        try {
            Log.d(TAG, "Filename: "+filename);
            Uri uri = Uri.parse (filename);
            if (uri.getScheme ().equals("content")) {
                is = getContentResolver().openInputStream(uri);
            } else { // raw file path
                is = new FileInputStream (uri.getPath ());
                Log.d(TAG, "available: "+is.available ());
                Log.d(TAG, "path: "+uri.getPath ());
            }
            open = true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found");
        } catch (IOException e) {
            Log.d(TAG, "an io exception occurred");
        }

        if (open && is != null) {
            try {
                audioClip.loadFile (is);
                text = "Audio loaded";
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } catch (IOException e) {
                Log.d(TAG, "Got io error");
                open = false;
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "no data");
                open = false;
            } catch (Exception e) {
                open = false; // Something else is wrong eg. wrong format
            }
        }

        if (!open) {
            text = "Could not load the selected file.";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finishAffinity ();
        }
    }


    /**
     * Called when 'reverse' button is press
     * To reverse a sound clip
     * @param view
     */
    public void onReverse (View view) {
        EditText editText = (EditText) findViewById(R.id.start_time);
        double start;
        try {
            start = Double.parseDouble (editText.getText().toString ());
        } catch (Exception e) {
            start = 0;
        }
        editText = (EditText) findViewById(R.id.end_time);
        double end;
        try {
            end = Double.parseDouble (editText.getText().toString ());
        } catch (Exception e) {
            end = audioClip.getDuration ();
        }

        if (end < 0 || end > audioClip.getDuration ()) {
            end = audioClip.getDuration ();
        }
        if (start < 0 || start > audioClip.getDuration ()) {
            start = 0;
        }
        int duration = Toast.LENGTH_SHORT;
        CharSequence text;
        audioClip.selectEffect(AudioClip.Option.REVERSE, audioClip.new
                               EffectArguments ().setStart
                               (start).setEnd (end));
        reversed = !reversed; // reversing the reverse track gives back the normal original track
        if (reversed) {
            text = "The track has been reversed";
        } else {
            text = "The track is no longer reversed";
        }
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * Called when 'Echo' button is pressed
     * Resulted sound clip will be echo'ed
     * @param view
     */
    public void onEcho (View view) {
        EditText editText = (EditText) findViewById(R.id.repeats);
        int repeats;
        try {
            repeats = Integer.parseInt (editText.getText().toString ());
        } catch (Exception e) {
            repeats = 0;
        }
        if (repeats < 1) {
            repeats = 1;
        }

        editText = (EditText) findViewById(R.id.start_time);
        double start;
        try {
            start = Double.parseDouble (editText.getText().toString ());
        } catch (Exception e) {
            start = 0;
        }
        editText = (EditText) findViewById(R.id.end_time);
        double end;
        try {
            end = Double.parseDouble (editText.getText().toString ());
        } catch (Exception e) {
            end = audioClip.getDuration ();
        }

        if (end < 0 || end > audioClip.getDuration ()) {
            end = audioClip.getDuration ();
        }
        if (start < 0 || start > audioClip.getDuration ()) {
            start = 0;
        }

        int duration = Toast.LENGTH_SHORT;
        CharSequence text = "Added echo effect";
        audioClip.selectEffect(AudioClip.Option.ECHO, audioClip.new
        EffectArguments ().setVolume ((float) 0.3).setStart
                               (start).setEnd (end).setRepeats (repeats));
        echo = true;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void onClipping (View view) {
        EditText editText = (EditText) findViewById(R.id.start_time);
        double start;
        try {
            start = Double.parseDouble (editText.getText().toString ());
        } catch (Exception e) {
            start = 0;
        }
        editText = (EditText) findViewById(R.id.end_time);
        double end;
        try {
            end = Double.parseDouble (editText.getText().toString ());
        } catch (Exception e) {
            end = audioClip.getDuration ();
        }

        if (end < 0 || end > audioClip.getDuration ()) {
            end = audioClip.getDuration ();
        }
        if (start < 0 || start > audioClip.getDuration ()) {
            start = 0;
        }

        int duration = Toast.LENGTH_SHORT;
        CharSequence text = "Added clipping effect";
        audioClip.selectEffect(AudioClip.Option.CLIPPING, audioClip.new
        EffectArguments ().setStart
                               (start).setEnd (end));
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private File writeFile (String filename) throws IOException {
        File path = Environment.getExternalStoragePublicDirectory(
                                                                  VTAMPER_DIR);
        File file = new File(path, filename);
        // Make sure the vtamper directory exists.
        path.mkdirs();
        audioClip.write (file);
        return file;
    }
    
    /**
     * Called when the “save" button is pressed
     * Save the sound clip into the device
     * @param view
     */

    public void onSave (View view) {
        int duration = Toast.LENGTH_SHORT;
        CharSequence text;
        String filename = "vtamper_clip_" + System.currentTimeMillis() + ".wav"; //generate unique filename
        try {
            File file = writeFile (filename);
            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(this,
                                            new String[] { file.toString() }, null,
                                            new MediaScannerConnection.OnScanCompletedListener() {
                                                public void onScanCompleted(String path, Uri uri) {
                                                }
                                            });
            text = "Clip saved.";
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            text = "Failed to write file.";
        }
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     * Called when the "Play" button is pressed
     * @param view
     */
    public void onPlay (View view) {

        String path;
        try {
            tmpFile = writeFile (TMP_FILE);
            path = tmpFile.toString ();
        } catch (IOException e) {
            return;
        }
        Button button = (Button) findViewById(R.id.btn_play);
        if (startPlay) {
            if (startPlay(path)) {
                button.setText ("Stop"); 
            } else {
                return;
            }
        } else {
            button.setText ("Play");
            stopPlay ();
        }
        startPlay = !startPlay;
    }
    
    private boolean startPlay(String path) {
        audioPlayer = new MediaPlayer();
        audioPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer audioPlayer) {
                    Button button = (Button) findViewById(R.id.btn_play);
                    button.setText ("Play"); 
                    startPlay = true;
                    stopPlay ();
                }
            });
        try {
            audioPlayer.setDataSource(path);
            audioPlayer.prepare();
            audioPlayer.start();
        }
        
        catch (IOException e) {
        
            int duration = Toast.LENGTH_SHORT;
            CharSequence text = "Error starting playing.";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return false;
            
        }
        
        return true;
    }	

    private void stopPlay() {
        audioPlayer.release();
        audioPlayer = null;
    } 

    /**
     * Called when shared button is pressed
     * Share the sound clip to suitable installed application
     * @param view
     */
    public void onShare(View view) {
        String path;
        try {
            tmpFile = writeFile (TMP_FILE);
            path = tmpFile.toString ();
        } catch (IOException e) {
            return;
        }
        Button button = (Button) findViewById(R.id.btn_share);
	        
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/*");
        share.putExtra(Intent.EXTRA_STREAM,Uri.parse("file:///"+path));
        startActivity(Intent.createChooser(share, "Share Sound File"));
    }
}
