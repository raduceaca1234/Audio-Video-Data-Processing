package project.domain;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Decoder {

    private final int width;
    private final int height;
    private final String resultingFile;
    private final String yFile;
    private final String uFile;
    private final String vFile;
    private final List<String> header;
    private double[][] Y;
    private double[][] U;
    private double[][] V;

    public Decoder(String resultingFile, String yFile, String uFile, String vFile, int width, int height, List<String> header) {
        this.resultingFile = resultingFile;
        this.yFile = yFile;
        this.uFile = uFile;
        this.vFile = vFile;
        this.width = width;
        this.height = height;
        this.header = header;
        Y = new double[height][width];
        U = new double[height][width];
        V = new double[height][width];
        readAndCreateYUVMatrix(yFile, "Y");
        readAndCreateYUVMatrix(uFile, "U");
        readAndCreateYUVMatrix(vFile, "V");
    }

    private void readAndCreateYUVMatrix(String filename, String component) {
        int i1 = 0, j1 = 0, i2 = 0, j2 = 0;
        boolean ok = true;
        String line;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineVal = line.split(" ");
                if (lineVal.length == 4 && ok) {
                    i1 = Integer.parseInt(lineVal[0]);
                    j1 = Integer.parseInt(lineVal[1]);
                    i2 = Integer.parseInt(lineVal[2]);
                    j2 = Integer.parseInt(lineVal[3]);
                    ok = false;
                } else {
                    List<String> lines = new ArrayList<>();
                    lines.add(line);
                    while (!(line = bufferedReader.readLine()).equals("")) {
                        lines.add(line);
                    }
                    int i, j, k, l;
                    if (component.equals("Y")) {
                        for (i = i1, k = 0; i <= i2; i++, k++) {
                            lineVal = lines.get(k).split(" ");
                            for (j = j1, l = 0; j <= j2; j++, l++) {
                                Y[i][j] = Double.parseDouble(lineVal[l]);
                            }
                        }
                    } else {
                        for (i = i1 * 2, k = 0; i <= i2 * 2 + 1; i += 2, k++) {
                            lineVal = lines.get(k).split(" ");
                            for (j = j1 * 2, l = 0; j <= j2 * 2 + 1; j += 2, l++) {
                                if (component.equals("U")) {
                                    U[i][j] = Double.parseDouble(lineVal[l]);
                                    U[i + 1][j] = Double.parseDouble(lineVal[l]);
                                    U[i][j + 1] = Double.parseDouble(lineVal[l]);
                                    U[i + 1][j + 1] = Double.parseDouble(lineVal[l]);
                                } else {
                                    V[i][j] = Double.parseDouble(lineVal[l]);
                                    V[i + 1][j] = Double.parseDouble(lineVal[l]);
                                    V[i][j + 1] = Double.parseDouble(lineVal[l]);
                                    V[i + 1][j + 1] = Double.parseDouble(lineVal[l]);
                                }
                            }
                        }
                    }
                    ok = true;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private int sampling(int value) {
        if (value < 0)
            value = 0;
        if (value > 255)
            value = 255;
        return value;
    }

    public void generateResultingFile() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(resultingFile))) {
            for (String head : header) {
                bufferedWriter.write(head);
                bufferedWriter.newLine();
            }
            int i, j;
            for (i = 0; i < height; i++) {
                for (j = 0; j < width; j++) {
                    String R = Integer.toString(sampling(((int) (Y[i][j] * 1.000 + V[i][j] * 1.400 - 128 * 1.400))));
                    String G = Integer.toString(sampling((int) (Y[i][j] * 1.000 + 128 * 0.343 - U[i][j] * 0.343 + 128 * 0.711 - V[i][j] * 0.711)));
                    String B = Integer.toString(sampling((int) (Y[i][j] * 1.000 - 128 * 1.765 + U[i][j] * 1.765)));
                    bufferedWriter.write(R);
                    bufferedWriter.newLine();
                    bufferedWriter.write(G);
                    bufferedWriter.newLine();
                    bufferedWriter.write(B);
                    bufferedWriter.newLine();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
