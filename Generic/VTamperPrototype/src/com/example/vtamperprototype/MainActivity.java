package com.example.vtamperprototype;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder; 
import android.media.MediaRecorder.AudioSource; 
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {
    private static final int HEADER_LENGTH = 44; // The size of WAV header in bytes.

    private byte[] audio = new byte[10000000]; // max audio size is 10000000 bytes for now
    private int audioLength = 0;
    private Context context;
    private MediaPlayer audioPlayer = null; 

    private MediaRecorder recorder = null; /*create new instance of MediaRecorder*/
    private boolean startRecord = true;
    private boolean startPlay = true;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context = getApplicationContext();
    }

    public void loadFile (View view) {
        int duration = Toast.LENGTH_SHORT;
        CharSequence text = "";
        EditText editText = (EditText) findViewById(R.id.file_path);
        String path = editText.getText().toString();
        try {
            FileInputStream fileInputStream = new FileInputStream (path);
            audioLength = fileInputStream.read (audio);
            text = "Loaded " + path +"...";
            fileInputStream.close ();
        } catch (FileNotFoundException e) {
            text = "File " + path + " not found...";
        } catch (IOException e) {
            text = path + ": An IOException occurred...";
        }
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void writeFile (View view) {
        int duration = Toast.LENGTH_SHORT;
        CharSequence text = "";
        String path = "/sdcard/output.wav";
        try {
            FileOutputStream fos = new FileOutputStream (path, false);
            fos.write (audio);
            fos.close ();
            text = "Wrote file " + path + "...";
        } catch (FileNotFoundException e) {
            text = "File " + path +" not found...";
        } catch (IOException e) {
            text = path + ": IOException...";
        }
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();    	
    }

    public void onRecord(View view) {
        EditText editText = (EditText) findViewById(R.id.file_path);
        String path = editText.getText().toString();
        Button button = (Button) findViewById(R.id.button_rec);
        if (startRecord) {
            if (startRecord (path)) {
                button.setText ("Stop"); 
            } else {
                return;
            }
        } else {
            button.setText ("Record");
            stopRecord ();
        }
        startRecord = !startRecord;
    }

	private boolean startRecord(String path) {
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); /*Output the file format in 3GPP*/
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(path);

		try {
			recorder.prepare();
            recorder.start();
		} catch (IOException e) {
            int duration = Toast.LENGTH_SHORT;
            CharSequence text = "Error starting recording.";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return false;
		}
        return true;
	}

	private void stopRecord() {
		recorder.stop();
		recorder.release();
		recorder = null; /*empty the recorder*/
	}

    public void onPlay(View view) {
        EditText editText = (EditText) findViewById(R.id.file_path);
        String path = editText.getText().toString();
        Button button = (Button) findViewById(R.id.button_play);
          
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
                    Button button = (Button) findViewById(R.id.button_play);
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
        EditText editText = (EditText) findViewById(R.id.file_path);
        String path = editText.getText().toString();
        Button button = (Button) findViewById(R.id.button_share);
	        
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/*");
        share.putExtra(Intent.EXTRA_STREAM,Uri.parse("file:///"+path));
        startActivity(Intent.createChooser(share, "Share Sound File"));
    }
	 
    public void onModify (View view) {
        for (int i = HEADER_LENGTH; i < audioLength; i++) {
            audio[i] = (byte) (audio[i]*0.25);
        }
        writeFile (view);
    }
}
