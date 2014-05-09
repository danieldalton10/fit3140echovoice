import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioClip {
    private interface Effect {
        public void perform ();
    }

    private byte[] header;
    private byte[] data;
    private byte[] newHeader;
    private byte[] newData;

    AudioClip () {
        this.header = new byte[44];
        this.newHeader = new byte[44];
    }

    public class AudioFile {
        public void loadFile (String filename) throws
            FileNotFoundException, IOException {
            FileInputStream fis = new FileInputStream (filename);
            // read off the header 
            int j = 0;
            for (int i = 0; i < 44; i++) {
                header[j++] = (byte) fis.read ();
            }
            // read off the data 
            data = new byte[getDataSize (header)];
            fis.read (data);
        }
    }

    private int getDataSize (byte[] currentHeader) {
        int size = 0;
        for (int i = 43; i >= 40; i--) { // convert the 4 bytes to int 
            size = size * 256 + currentHeader[i]; 
        }
        return size;
    }

    public void selectEffect (int option) {
        switch (option) {
        case 0: // reverse
            manipulateClip(new Reverse ());
            break;
        default:
            return;
        }
    }

    private void manipulateClip (Effect effect) {
        effect.perform ();
    }

    public class Reverse implements Effect {
        @Override
        public void perform () {
            /* TODO: extract this info from the header*/
            int sampleSize = 2; 
            int endEnd = getDataSize(header);
            int endStart = endEnd-sampleSize;
            int startStart = 0;
            int startEnd = sampleSize;

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
        }
    }

    public void write (String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream (filename);
        fos.write (header);
        fos.write (data);
    }

    public static void main (String[] args) throws
        FileNotFoundException, IOException { 
        AudioClip audioClip = new AudioClip ();
        AudioClip.AudioFile audioFile = audioClip.new AudioFile ();
        audioFile.loadFile (args[0]);
        audioClip.selectEffect(0);
        audioClip.write ("output.wav");
    }
}
