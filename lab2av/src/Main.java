public class Main {

    public static void main(String[] args) {
        Encoder encoder = new Encoder("data/nt-P3.ppm");
        encoder.write_in_filesY("data/Y.txt");
        encoder.write_in_filesU("data/U.txt");
        encoder.write_in_filesV("data/V.txt");
        encoder.generateDCTBlocks();
        Decoder decoder = new Decoder("data/final_image.ppm", "data/YDCT.txt", "data/UDCT.txt", "data/VDCT.txt",
                encoder.width, encoder.height, encoder.header, encoder.Q_matrix, encoder.indexesList, encoder.matrix_of_DCT
        );
        decoder.generateFinalImage();
    }

}