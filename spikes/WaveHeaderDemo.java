import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/* Demonstrate how to extend the length of  a wave file. */
class WaveHeaderDemo {
    /**
     * Read the header of a RIFF WAVE file.
     * Todo: don't read extra one bit at the end of the header 
     * Note: Some fields are only 2 bytes not 4, but we only care about the size fields which are 4 bytes.
     */
    public static int[] getHeader (String filename) throws IOException, FileNotFoundException {
        FileInputStream fis = new FileInputStream (filename);
        byte[] b = new byte[4]; // temp var for each 4 bytes of the header
        int a;
        int j = 0;
        int[] header = new int[11]; // header consists of 44 bytes = 11 ints (int = 4 bytes)
        for (int i = 0; i <= 44; i++) { // extract header
            if ((i%4) == 0 && i > 0) { // at the end of a sequence of 4 bytes
                j = 0; 
                a = 0;
                for (int x=3; x >=0; x--) { // convert the 4 bytes to int 
                    a = a * 256 + b[x]; 
                }
                header[i/4-1] = a; // add the next int of 4 bytes in the header
                System.out.println (i/4+": "+a); // print out the header fields as we go
            }
            b[j++] = (byte) fis.read (); // read next byte
        }
        return header; // returns the header as 11 integers array 
    }

    /* Convert a header back to bytes to be written to file. */
    public static byte[] intToBytes (int n) {
        byte[] bytes = new byte[4];
        int mask = 255;
        int j = 0;
        for (int i = 0; i < 32; i+=8) {
            bytes[j++] = (byte) (n >>> i & mask);
        }
        return bytes;
    }

    public static void main (String[] args) throws
        FileNotFoundException, IOException {
        int[] header = getHeader(args[0]);
        int dataSize = header[10]; // old data size in bytes
        byte[] data = new byte[dataSize *3]; // repeat file 3 times 
        FileInputStream fis = new FileInputStream (args[0]);
        byte[] bHeader = new byte[44];
        for (int i = 0; i < 44; i++) { // read header
            bHeader[i] = (byte) fis.read ();
        }
        System.out.println (fis.read (data, 0, dataSize)); // read in data to data array and print number of bytes read
        int j = 0;
        // repeat the original data 3 times to completely fill up the data array 
        for (int i = dataSize; i < 3*dataSize; i++) {
            if (j >= dataSize) { // end of existing data start at 0th byte
                j = 0;
            }
            data[i] = (byte) (data[j++]*0.5); // half volume like an echo 
        }
        FileOutputStream fos = new FileOutputStream ("output_new.wav");
        byte[] ds = intToBytes(dataSize * 3); // convert new data size to an array of 4 bytes
        byte[] dsm = intToBytes(dataSize*3+36); // other size header into 4 bytes array
        j = 0;
        // second field of header updated to reflect new data size+36
        for (int i = 4; i < 8; i++) { 
            bHeader[i] = ds[j++];
        }
        j=0;
        // 11th field of header updated to reflect new data size
        for (int i = 40; i < 44; i++) {
            bHeader[i] = dsm[j++];
        }
        fos.write (bHeader); // rite the header first
        fos.write (data); // write the data 
    }
}
