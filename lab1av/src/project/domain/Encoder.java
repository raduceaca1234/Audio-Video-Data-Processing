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
    private double[][] y_matrix;
    private double[][] u_matrix;
    private double[][] v_matrix;
    private List<List<Integer>> listOfIndexes8x8;

    public Encoder(String file) {
        this.file = file;
        initializeYUV();
    }

    private void initializeYUV() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            int headerNumberOfLines = 0, i = 0, j = 0, R = 0, G = 0, B = 0;
            String val = "r";
            String line;
            file_header = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                if (headerNumberOfLines < 3) {
                    if (headerNumberOfLines == 1 && !line.startsWith("#")) {
                        width = Integer.parseInt(line.split(" ")[0]);
                        height = Integer.parseInt(line.split(" ")[1]);
                        y_matrix = new double[height][width];
                        u_matrix = new double[height][width];
                        v_matrix = new double[height][width];
                    }
                    if (!line.startsWith("#"))
                        headerNumberOfLines++;
                    file_header.add(line);
                } else {
                    if (val.equals("r")) {
                        R = Integer.parseInt(line);
                        val="g";
                    } else if (val.equals("g")) {
                        G = Integer.parseInt(line);
                        val="b";
                    } else {
                        B = Integer.parseInt(line);
                        y_matrix[i][j] = 0.299 * R + 0.587 * G + 0.114 * B;
                        u_matrix[i][j] = 128 - 0.1687 * R - 0.3312 * G + 0.5 * B;
                        v_matrix[i][j] = 128 + 0.5 * R - 0.4186 * G - 0.0813 * B;
                        j++;
                        if (j == width) {
                            i++;
                            j = 0;
                        }
                        val = "r";
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
            if (j2 > width) {
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
        for (int i = i1, k = 0; i <= i2; i += 2, k++)
            for (int j = j1, l = 0; j <= j2; j += 2, l++)
                if (component.equals("U")) {
                    matrix[k][l] = (u_matrix[i][j] + u_matrix[i + 1][j] + u_matrix[i][j + 1] + u_matrix[i + 1][j + 1]) / 4;
                } else {
                    matrix[k][l] = (v_matrix[i][j] + v_matrix[i + 1][j] + v_matrix[i][j + 1] + v_matrix[i + 1][j + 1]) / 4;
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
                            bufferedWriter.write(formatter.format(y_matrix[i][j]));
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
                    for (i = 0; i < 4; i++) {
                        for (j = 0; j < 4; j++) {
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

    public double[][] getY_matrix() {
        return y_matrix;
    }

    public void setY_matrix(double[][] y_matrix) {
        this.y_matrix = y_matrix;
    }

    public double[][] getU_matrix() {
        return u_matrix;
    }

    public void setU_matrix(double[][] u_matrix) {
        this.u_matrix = u_matrix;
    }

    public double[][] getV_matrix() {
        return v_matrix;
    }

    public void setV_matrix(double[][] v_matrix) {
        this.v_matrix = v_matrix;
    }

    public List<List<Integer>> getListOfIndexes8x8() {
        return listOfIndexes8x8;
    }

    public void setListOfIndexes8x8(List<List<Integer>> listOfIndexes8x8) {
        this.listOfIndexes8x8 = listOfIndexes8x8;
    }
}
