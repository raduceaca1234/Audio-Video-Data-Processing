package project;

import project.domain.Decoder;
import project.domain.Encoder;

public class Main {

    public static void main(String[] args) {
        Encoder encoder = new Encoder("nt-P3.ppm");
        encoder.writeInFile("y_matrices.txt", "Y");
        encoder.writeInFile("u_matrices.txt", "U");
        encoder.writeInFile("v_matrices.txt", "V");
        Decoder decoder = new Decoder("nt-P3new.ppm", "y_matrices.txt", "u_matrices.txt", "v_matrices.txt",
                encoder.getWidth(), encoder.getHeight(), encoder.getFile_header());
        decoder.generateResultingFile();
    }

}