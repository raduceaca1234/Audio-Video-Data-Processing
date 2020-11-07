package project;

import project.domain.Decoder;
import project.domain.Encoder;

public class Main {

    public static void main(String[] args) {
        Encoder encoder = new Encoder("C:\\Users\\ceaca\\Documents\\Audio-Video-Data-Processing\\lab1av\\src\\project\\data\\given_data\\nt-P3.ppm");
        encoder.writeInFile("C:\\Users\\ceaca\\Documents\\Audio-Video-Data-Processing\\lab1av\\src\\project\\data\\y_matrices.txt", "Y");
        encoder.writeInFile("C:\\Users\\ceaca\\Documents\\Audio-Video-Data-Processing\\lab1av\\src\\project\\data\\u_matrices.txt", "U");
        encoder.writeInFile("C:\\Users\\ceaca\\Documents\\Audio-Video-Data-Processing\\lab1av\\src\\project\\data\\v_matrices.txt", "V");
        Decoder decoder = new Decoder(
                "C:\\Users\\ceaca\\Documents\\Audio-Video-Data-Processing\\lab1av\\src\\project\\data\\nt-P3new.ppm",
                "C:\\Users\\ceaca\\Documents\\Audio-Video-Data-Processing\\lab1av\\src\\project\\data\\y_matrices.txt",
                "C:\\Users\\ceaca\\Documents\\Audio-Video-Data-Processing\\lab1av\\src\\project\\data\\u_matrices.txt",
                "C:\\Users\\ceaca\\Documents\\Audio-Video-Data-Processing\\lab1av\\src\\project\\data\\v_matrices.txt",
                encoder.getWidth(), encoder.getHeight(), encoder.getFile_header());
        decoder.generateResultingFile();
    }

}