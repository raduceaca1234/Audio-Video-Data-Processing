package project.domain;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Encoder {

    private List<String> file_header;
    private int width, height;
    private final String file;
    private double[][] Y;
    private double[][] U;
    private double[][] V;
    private List<List<Integer>> listOfIndexes8x8;

    public Encoder(String file) {
        this.file = file;
        initializeYUV();
    }

    private void initializeYUV() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            int headerNumberOfLines = 0, val = 0, i = 0, j = 0, R = 0, G = 0, B = 0;
            String line;
            file_header = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                if (headerNumberOfLines < 3) {
                    if (headerNumberOfLines == 1 && !line.startsWith("#")) {
                        width = Integer.parseInt(line.split(" ")[0]);
                        height = Integer.parseInt(line.split(" ")[1]);
                        Y = new double[height][width];
                        U = new double[height][width];
                        V = new double[height][width];
                    }
                    if (!line.startsWith("#"))
                        headerNumberOfLines++;
                    file_header.add(line);
                } else {
                    if (val == 0) {
                        R = Integer.parseInt(line);
                        val++;
                    } else if (val == 1) {
                        G = Integer.parseInt(line);
                        val++;
                    } else if (val == 2) {
                        B = Integer.parseInt(line);
                        Y[i][j] = 0.299 * R + 0.587 * G + 0.144 * B;
                        U[i][j] = 128 - 0.169 * R - 0.331 * G + 0.5 * B;
                        V[i][j] = 128 + 0.5 * R - 0.419 * G - 0.081 * B;
                        j++;
                        if (j == width) {
                            i++;
                            j = 0;
                        }
                        val = 0;
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        initializeListOfIndexes8x8();
    }

    private void initializeListOfIndexes8x8() {
        listOfIndexes8x8 = new ArrayList<>();
        int i1 = 0, j1 = 0, i2 = 7, j2 = 7;
        while (i2 < height) {
            if (j2 == width - 1) {
                j1 = 0;
                j2 = 7;
                i1 += 8;
                i2 += 8;
            } else {
                listOfIndexes8x8.add(new ArrayList<>(Arrays.asList(i1, j1, i2, j2)));
                j1 += 8;
                j2 += 8;
            }
        }
    }

    private double[][] calculate4x4Matrix(int i1, int j1, int i2, int j2, String component) {
        double[][] matrix = new double[4][4];
        for (int i = i1, k = 0; i <= i2 - 1; i += 2, k++)
            for (int j = j1, l = 0; j <= j2 - 1; j += 2, l++)
                if (component.equals("U")) {
                    matrix[k][l] = (U[i][j] + U[i + 1][j] + U[i][j + 1] + U[i + 1][j + 1]) / 4;
                } else {
                    matrix[k][l] = (V[i][j] + V[i + 1][j] + V[i][j + 1] + V[i + 1][j + 1]) / 4;
                }
        return matrix;
    }

    public void writeInFile(String filename, String component) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename))) {
            int i, j;
            NumberFormat formatter = new DecimalFormat("#0.000");
            if (component.equals("Y")) {
                for (List<Integer> matrix : listOfIndexes8x8) {
                    bufferedWriter.write(matrix.get(0) + " " + matrix.get(1) + " " + matrix.get(2) + " "
                            + matrix.get(3));
                    bufferedWriter.newLine();
                    for (i = matrix.get(0); i <= matrix.get(2); i++) {
                        for (j = matrix.get(1); j <= matrix.get(3); j++) {
                            bufferedWriter.write(formatter.format(Y[i][j]));
                            bufferedWriter.write(" ");
                        }
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.newLine();
                }
            } else {
                for (List<Integer> matrix : listOfIndexes8x8) {
                    double[][] matrix4X4;
                    bufferedWriter.write(matrix.get(0) / 2 + " " + matrix.get(1) / 2 + " " +
                            matrix.get(2) / 2 + " " + matrix.get(3) / 2);
                    bufferedWriter.newLine();
                    if (component.equals("U")) {
                        matrix4X4 = calculate4x4Matrix(matrix.get(0), matrix.get(1), matrix.get(2), matrix.get(3),
                                "U");
                    } else {
                        matrix4X4 = calculate4x4Matrix(matrix.get(0), matrix.get(1), matrix.get(2), matrix.get(3),
                                "V");
                    }
                    for (i = 0; i <= 3; i++) {
                        for (j = 0; j <= 3; j++) {
                            bufferedWriter.write(formatter.format(matrix4X4[i][j]));
                            bufferedWriter.write(" ");
                        }
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.newLine();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public List<String> getFile_header() {
        return file_header;
    }

    public void setFile_header(List<String> file_header) {
        this.file_header = file_header;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getFile() {
        return file;
    }

    public double[][] getY() {
        return Y;
    }

    public void setY(double[][] y) {
        Y = y;
    }

    public double[][] getU() {
        return U;
    }

    public void setU(double[][] u) {
        U = u;
    }

    public double[][] getV() {
        return V;
    }

    public void setV(double[][] v) {
        V = v;
    }

    public List<List<Integer>> getListOfIndexes8x8() {
        return listOfIndexes8x8;
    }

    public void setListOfIndexes8x8(List<List<Integer>> listOfIndexes8x8) {
        this.listOfIndexes8x8 = listOfIndexes8x8;
    }
}
