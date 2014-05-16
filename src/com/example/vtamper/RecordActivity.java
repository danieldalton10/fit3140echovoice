package com.example.vtamper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordActivity extends Activity
{
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDER_BPP = 16;

    private final String VTAMPER_DIR = "VTamper clips";
    private final String TMP_NAME = ".tmp_rec_data.raw";
    
    private String TMP_FILE = null;
    private boolean isRecording = false;

    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record);
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                                                  AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                                  AudioFormat.ENCODING_PCM_16BIT);
        Button button = (Button) findViewById(R.id.btn_stop);
        button.setEnabled (false);
    }
    public void onStartRecording(View view) {
        Button button = (Button) findViewById(R.id.btn_start_record);
        button.setEnabled (false);
        button = (Button) findViewById(R.id.btn_stop);
        button.setEnabled (true);

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                   RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
                
        int i = recorder.getState();
        if(i==1) {
            recorder.startRecording();
        }                
        isRecording = true;
                
        recordingThread = new Thread(new Runnable() {
                        
                @Override
                    public void run() {
                    writeAudioDataToFile();
                }
            },"AudioRecorder Thread");
                
        recordingThread.start();
    }

    private void writeAudioDataToFile () {
        File path = Environment.getExternalStoragePublicDirectory(
                                                                  VTAMPER_DIR);
        File file = new File(path, TMP_NAME);
        // Make sure the vtamper directory exists.
        path.mkdirs();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream (file);
        } catch (IOException e) {
            // handled below 
        }
        TMP_FILE = file.getPath ();
        if (null != fos){
            int read = 0;
            byte[] data = new byte[bufferSize];
            while(isRecording){
                read = recorder.read(data, 0, bufferSize);
                if (AudioRecord.ERROR_INVALID_OPERATION != read){
                    Log.d("RECORDER", "Attempting to write.");
                    try {
                        fos.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
                        
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onStopRecording (View view) {
        if (null != recorder){
            isRecording = false;
                        
            int i = recorder.getState();
            if(i==1) {
                recorder.stop();
            }
            recorder.release();
                        
            recorder = null;
            recordingThread = null;
        }
                
        String path = copyWaveFile(TMP_FILE, "rec_"+System.currentTimeMillis
                     ()+".wav");
        deleteTempFile();
        Intent intent = new Intent (this, EffectActivity.class);
        intent.putExtra("filePath", path);
        startActivity (intent);
    }

    private void deleteTempFile() {
        File file = new File (TMP_FILE);
                
        file.delete();
    }

    private String copyWaveFile(String inFilename,String outFilename){
        File path = Environment.getExternalStoragePublicDirectory(
                                                                  VTAMPER_DIR);
        File file = new File(path, outFilename);
        // Make sure the vtamper directory exists.
        path.mkdirs();
        String filename = file.toString ();
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
                
        byte[] data = new byte[bufferSize];
                
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(filename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
                        
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                                longSampleRate, channels, byteRate);
            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaScannerConnection.scanFile(this,
                                        new String[] { file.toString() }, null,
                                        new MediaScannerConnection.OnScanCompletedListener() {
                                            public void onScanCompleted(String path, Uri uri) {
                                            }
                                        });
        return Uri.fromFile (file).toString ();
    }

    private void WriteWaveFileHeader(
                                     FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels,
                                     long byteRate) throws IOException {
                
        byte[] header = new byte[44];
                
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}
