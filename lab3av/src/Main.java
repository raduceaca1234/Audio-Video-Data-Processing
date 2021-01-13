public class Main {

    public static void main(String[] args) {
        Encoder encoder = new Encoder("nt-P3.ppm");
        encoder.writeInFile("Y.txt", "Y");
        encoder.writeInFile("U.txt", "U");
        encoder.writeInFile("V.txt", "V");
        encoder.entropyEncoding();
        Decoder decoder = new Decoder("nt-P3new2.ppm",
                encoder.width, encoder.height, encoder.header, encoder.Q, encoder.listOfIndexes8x8, encoder.entropyEncoding, encoder.amplitudeAndSize);
        decoder.generateResultingFile();
    }

}
