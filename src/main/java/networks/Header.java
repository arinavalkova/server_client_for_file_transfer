package networks;

public class Header {
    private String fileName;
    private byte[] fileHash;
    private byte countOfPackets;

    public Header(byte[] headerArray) {
        parseHeaderArray(headerArray);
    }

    private void parseHeaderArray(byte[] headerArray) {
        byte[] fileName = new byte[headerArray[0]];
        int i;

        for(i = 1; i <= headerArray[0]; i++) {
            fileName[i - 1] = headerArray[i];
        }

        this.fileName = new String(fileName);

        byte hashSize = headerArray[i++];
        int j, k;
        fileHash = new byte[hashSize];

        for(j = i, k = 0; j < i + hashSize; j++, k++) {
            this.fileHash[k] = headerArray[j];
        }

        this.countOfPackets = headerArray[j];
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileHash() {
        return fileHash;
    }

    public byte getCountOfPackets() {
        return countOfPackets;
    }
}
