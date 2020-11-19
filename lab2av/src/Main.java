public class Main {

    public static void main(String[] args) {
        Encoder encoder = new Encoder("data/nt-P3.ppm");
        encoder.write_in_files("data/Y.txt", "Y");
        encoder.write_in_files("data/U.txt", "U");
        encoder.write_in_files("data/V.txt", "V");
        encoder.generateDCTBlocks();
        Decoder decoder = new Decoder("data/final_image.ppm", "data/YDCT.txt", "data/UDCT.txt", "data/VDCT.txt",
                encoder.width, encoder.height, encoder.header, encoder.Q_matrix, encoder.indexesList, encoder.matrix_of_DCT
        );
        decoder.generateFinalImage();
    }

}
