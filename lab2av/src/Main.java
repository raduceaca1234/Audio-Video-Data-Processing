public class Main {

    public static void main(String[] args) {
        Encoder encoder = new Encoder("nt-P3.ppm");
        encoder.writeInFile("Y.txt", "Y");
        encoder.writeInFile("U.txt", "U");
        encoder.writeInFile("V.txt", "V");
        encoder.generateDCTBlocks("DCT-Y.txt", "DCT-U.txt", "DCT-V.txt");
        Decoder decoder = new Decoder("nt-P3new.ppm", "DCT-Y.txt", "DCT-U.txt", "DCT-V.txt",
                encoder.width, encoder.height, encoder.header, encoder.Q, encoder.indexesList, encoder.DCT,
                encoder.Y, encoder.U, encoder.V);
        decoder.generateFinalImage();
    }

}
