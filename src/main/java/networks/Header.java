package networks;

import java.math.BigInteger;
import java.util.Arrays;

public class Header {
    private String fileName;
    private byte[] fileHash;
    private int countOfPackets;

    public Header(byte[] headerArray) {
        parseHeaderArray(headerArray);
    }

    private void parseHeaderArray(byte[] headerArray) {

        int i = 0;
        int fileNameSize = new BigInteger(Arrays.copyOfRange(headerArray, i, Integer.BYTES)).intValue();

        i += Integer.BYTES;

        byte[] fileName = Arrays.copyOfRange(headerArray, i, i + fileNameSize);

        i += fileNameSize;

        this.fileName = new String(fileName);

        int hashSize = new BigInteger(Arrays.copyOfRange(headerArray, i, i + Integer.BYTES)).intValue();

        i += Integer.BYTES;

        fileHash = Arrays.copyOfRange(headerArray, i, i + hashSize);

        i += hashSize;

        this.countOfPackets = new BigInteger(Arrays.copyOfRange(headerArray, i, i + Integer.BYTES)).intValue();
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileHash() {
        return fileHash;
    }

    public int getCountOfPackets() {
        return countOfPackets;
    }
}
