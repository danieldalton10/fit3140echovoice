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
import android.widget.Toast;

import com.example.vtamper.AudioClip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EffectActivity extends Activity
{
    private final String TAG = "VTamper";
    private final String VTAMPER_DIR = "VTamper clips";
    private AudioClip audioClip;
    private Context context;
    private String loadFilePath;
    private boolean echo = false;
    private boolean reversed = false;
    private boolean startPlay = true;
    private MediaPlayer audioPlayer;
    private File tmpFile = null;
    final int PICK_WAVE = 1;
    final String TMP_FILE = "vtamper_clip_shared.wav";

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

    private File writeFile (String filename) throws IOException {
        File path = Environment.getExternalStoragePublicDirectory(
                                                                  VTAMPER_DIR);
        File file = new File(path, filename);
        // Make sure the vtamper directory exists.
        path.mkdirs();
        audioClip.write (file);
        return file;
    }

    public void onSave (View view) {
        int duration = Toast.LENGTH_SHORT;
        CharSequence text;
        String filename = "vtamper_clip_" + System.currentTimeMillis() + ".wav";
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
