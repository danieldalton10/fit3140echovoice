package com.example.vtamper;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A class to manipulate the sound clip with the effects (Echo and Reverse) implemented within.
 * @author Daniel Dalton and Choon Yin Yeap
 * @since May 2014
 */
public class AudioClip {
	
    public enum Option {
        REVERSE, ECHO
    }

    private interface Effect {
        public void perform ();
    }

    private byte[] header;
    private byte[] data;
    private byte[] newHeader;
    private byte[] newData;

    public AudioClip () {
        this.header = new byte[44];
        this.newHeader = new byte[44];
    }

    /**
     * 
     * A class that gas the functionalities to control the AudioClip's volume, start, end and numberofRepeat
     * @author Daniel Dalton
     * @since  May 2014
     *
     */
    public class EffectArguments {
        private float volumeFactor = 1;
        private int start = 0;
        private int end = getDataSize (header);
        private int repeats = 1;

        public float getVolume () {
            return volumeFactor;
        }

        public int getStart () {
            return start;
        }

        public int getEnd () {
            return end;
        }

        public int getRepeats () {
            return repeats;
        }

        public EffectArguments setVolume (float volume) {
            volumeFactor = volume;
            return this;
        }

        public EffectArguments setStart (double start) {
            this.start = getTimeIndex (header, start);
            return this;
        }

        public EffectArguments setEnd (double end) {
            this.end = getTimeIndex(header, end);
            return this;
        }

        public EffectArguments setRepeats (int r) {
            repeats = r;
            return this;
        }
    }

    public void loadFile (InputStream fis) throws IOException, IllegalArgumentException {
        //            FileInputStream fis = new FileInputStream (filename);
        // read off the header 
        int j = 0;
        for (int i = 0; i < 44; i++) {
            header[j++] = (byte) fis.read ();
        }
        // read off the data 
        data = new byte[getDataSize (header)];
        fis.read (data);
        fis.close ();
        Log.d ("VTAMPER", "Size of data: "+data.length);
        if (data.length == 0) {
            throw new IllegalArgumentException ("File has no data");
        }
    }

    /**
     * To get the field of a sound clip and returns its size
     * @param currentHeader
     * @param start
     * @param end
     * @return size
     */
    private int getField (byte[] currentHeader, int start, int end) {
        int size = 0;
        for (int i = end; i >= start; i--) { // convert the 4 bytes to int 
            size = size * 256 + currentHeader[i]; 
        }
        return size;
    }

    private int getDataSize (byte[] currentHeader) {
        return getField(currentHeader, 40, 43);
    }

    private int getSampleSize (byte[] currentHeader) {
        return getField(currentHeader, 34, 35) / 8; // in bytes, not bits
    }

    private int getTimeIndex (byte[] header, double time) {
        return (int) ((double) getField (header, 28, 31) * time);
    }

    public double getDuration () {
        return (double) data.length / getField (header, 28, 31);
    }

    /**
     * Convert integer to byteStream
     * @param n
     * @return
     */
    private byte[] intToBytes (int n) {
        byte[] bytes = new byte[4];
        int mask = 255;
        int j = 0;
        for (int i = 0; i < 32; i+=8) {
            bytes[j++] = (byte) (n >>> i & mask);
        }
        return bytes;
    }

    private void insertBytes (byte[] source, byte[] dest, int start) {
        if ((start + source.length) > dest.length) {
            throw new IllegalArgumentException ("Array is not long enough");
        }

        // now add the bytes 
        for (int i = 0; i < source.length; i++) {
            dest[start + i] = source[i];
        }
    }

    private void updateSize (byte[] currentHeader, int newDataSize) {
        byte[] dataSize = intToBytes (newDataSize);
        byte[] fileSize = intToBytes (newDataSize + 36);
        insertBytes (dataSize, currentHeader, 40);
        insertBytes (fileSize, currentHeader, 4);
    }

   /**
    * Choose the available effects i.e. Echo and Reverse 
    * @param option
    * @param args
    */
    public void selectEffect (Option option, EffectArguments args) {
        switch (option) {
        case REVERSE: // reverse
            manipulateClip(new Reverse (args)); 
            break;
        case ECHO: //echo
            manipulateClip (new Echo (args));
            break;
        }
    }

    private void manipulateClip (Effect effect) {
        effect.perform ();
    }

    private void setData (EffectArguments arguments) {
        int start = arguments.getStart ();
        int end = arguments.getEnd ();
        if (start != 0 || end != getDataSize (header)) {
            int length = arguments.getEnd () - arguments.getStart ();
            byte[] tempData = new byte[length];
            for (int i = start; i < end; i++) {
                tempData[i-start] = data[i];
            }
            data = tempData;
        }
    }

    /**
     * A class of reverse Effect that enables user to reverse the selected sound clip in WAVE Format
     * @author Daniel Dalton
     * @since  May 2014
     *
     */
    public class Reverse implements Effect {
        private EffectArguments arguments;

        public Reverse (EffectArguments args) {
            arguments = args;
        }

        @Override
        public void perform () {
            setData (arguments);
            int sampleSize = getSampleSize (header); 
            int endEnd = data.length; // header is out of date currently
            int startStart = 0;
            int endStart = endEnd-sampleSize;
            int startEnd = sampleSize;
            int length = data.length;

            while (startEnd <= endStart) {
                byte[] temp = new byte[sampleSize];
                for (int i = 0; i < sampleSize; i++) {
                    temp[i] = data[startStart+i];
                    data[startStart+i] = data[endStart+i];
                }
                for (int i = 0; i < sampleSize; i++) {
                    data[endStart+i] = temp[i];
                }
                startStart+=sampleSize;
                startEnd+=sampleSize;
                endStart-=sampleSize;
                endEnd-=sampleSize;
            }
            updateSize (header, length);
        }
    }

    /**
     * A class of echo Effect that enables user to echo the selected sound clip in WAVE Format
     * @author Daniel Dalton
     * @since  May 2014
     *
     */
    public class Echo implements Effect {
        private EffectArguments arguments;

        public Echo (EffectArguments args) {
            arguments = args;
        }

        @Override 
        public void perform () {
            setData (arguments);
            int length = data.length; // header out of date currently
            ByteBuffer combined = ByteBuffer.allocate (length * arguments.getRepeats () + length);
            combined.put (data);
            byte[] lowered = new byte[length];
            int j = 0;
            for (int i = 0; i < length; i++) {
                lowered[j++] = (byte) (data[i] * arguments.getVolume ());
            }
            // For each repeat add the original sound at 50% volume.
            for (int n = 1; n <= arguments.getRepeats (); n++) {
                // Add each byte of the original sound into our combined output sound.
                combined.put(lowered);
            }
            // use the format from the original input stream.
            data = combined.array ();
            updateSize (header, length * (arguments.getRepeats() + 1));
        }
    }

    /**
     * Write a file with input of file
     * @param file
     * @throws IOException
     */
    public void write (File file) throws IOException {
        FileOutputStream fos = new FileOutputStream (file);
        fos.write (header);
        fos.write (data);
        fos.close ();
    }
}
