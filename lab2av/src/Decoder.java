import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class Decoder {

    private final int width;
    private final int height;
    private final String resultingFile;
    private final List<String> header;
    private final double[][] matrix_of_Y;
    private final double[][] matrix_of_U;
    private final double[][] matrix_of_V;
    private final double[][] YFinal;
    private final double[][] UFinal;
    private final double[][] VFinal;
    private final double[][] Q_matrix;
    private final List<List<Integer>> indexesList;
    public double[][] DCT;

    public Decoder(String resultingFile, String yFile, String uFile, String vFile, int width, int height, List<String> header, double[][] Q_matrix, List<List<Integer>> indexesList, double[][] DCT) {
        this.resultingFile = resultingFile;
        this.width = width;
        this.height = height;
        this.header = header;
        this.matrix_of_Y = new double[height][width];
        this.matrix_of_U = new double[height][width];
        this.matrix_of_V = new double[height][width];
        this.YFinal = new double[height][width];
        this.UFinal = new double[height][width];
        this.VFinal = new double[height][width];
        this.Q_matrix = Q_matrix;
        this.indexesList = indexesList;
        populateYMatrices(yFile);
        populateUMatrices(uFile);
        populateVMatrices(vFile);
        this.DCT = DCT;
    }

    private int samplingFunction(int value) {
        if (value > 255)
            value = 255;
        if (value < 0)
            value = 0;
        return value;
    }

    private void adding128(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                YFinal[i][j] += 128;
                UFinal[i][j] += 128;
                VFinal[i][j] += 128;
            }
        }
    }

    private void readFromFilesAndApplyDeQuantization(int i1, int j1, int i2, int j2, List<String> lines, double[][] matrix) {
        int i, a, k, j, b, l;
        String[] lineVal;
        for (i = i1,a=0, k = 0; i <= i2; i++, k++, a++) {
            lineVal = lines.get(k).split(" ");
            for (j = j1,b=0, l = 0; j <= j2; j++, l++, b++) {
                matrix[i][j] = Double.parseDouble(lineVal[l])* Q_matrix[a][b];
            }
        }
    }

    private void populateYMatrices(String filename) {
        populate(filename, matrix_of_Y);
    }

    private void populateUMatrices(String filename) {
        populate(filename, matrix_of_U);
    }

    private void populateVMatrices(String filename) {
        populate(filename, matrix_of_V);
    }

    private void populate(String filename, double[][] matrix_of_y) {
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
                    readFromFilesAndApplyDeQuantization(i1, j1, i2, j2, lines, matrix_of_y);
                    ok = true;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void inverseDCTForY(int i1, int j1) {
        populateFinalMatrix(i1, j1, matrix_of_Y, YFinal);
    }

    private void inverseDCTForU(int i1, int j1) {
        populateFinalMatrix(i1, j1, matrix_of_U, UFinal);
    }

    private void inverseDCTForV(int i1, int j1) {
        populateFinalMatrix(i1, j1, matrix_of_V, VFinal);
    }

    private void populateFinalMatrix(int i1, int j1, double[][] matrix_of_v, double[][] vFinal) {
        double sum;
        for (int x=0; x <= 7;  x++) {
            for (int y = 0; y <= 7; y++) {
                sum = computeSum(i1, j1, x, y, matrix_of_v);
                sum = 0.25 * sum;
                vFinal[i1+x][j1+y] = sum;
            }
        }
    }

    private double computeSum(int i1, int j1, int x, int y, double[][] matrix_of_y) {
        double sum;
        int u;
        int v;
        sum = 0;
        for (u = 0; u <=7; u++) {
            for (v = 0; v <=7; v++) {
                double alp1 = 0, alp2 = 0;
                if(u==0)
                    alp1 = 1.0 / sqrt(2);
                else if(u>0)
                    alp1 = 1.0;
                if(v==0)
                    alp2 = 1.0 / sqrt(2);
                else if(v>0)
                    alp2 = 1.0;
                double cos = cos((((2 * x) + 1) * u * PI) / 16) * cos((((2 * y) + 1) * v * PI) / 16);
                sum += matrix_of_y[i1+u][j1+v] * alp1 * alp2 * cos;
            }
        }
        return sum;
    }


    public void generateFinalImage() {
        for (List<Integer> matrix : indexesList) {
            inverseDCTForY(matrix.get(0), matrix.get(1));
            inverseDCTForU(matrix.get(0), matrix.get(1));
            inverseDCTForV(matrix.get(0), matrix.get(1));
        }
        adding128();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(resultingFile))) {
            for (String head : header) {
                bufferedWriter.write(head);
                bufferedWriter.newLine();
            }
            int i, j;
            for (i = 0; i < height; i++) {
                for (j = 0; j < width; j++) {
                    String R = Integer.toString(samplingFunction(((int) (YFinal[i][j] * 1.000 + VFinal[i][j] * 1.400 - 128 * 1.400))));
                    String G = Integer.toString(samplingFunction((int) (YFinal[i][j] * 1.000 + 128 * 0.343 - UFinal[i][j] * 0.343 + 128 * 0.711 - VFinal[i][j] * 0.711)));
                    String B = Integer.toString(samplingFunction((int) (YFinal[i][j] * 1.000 - 128 * 1.765 + UFinal[i][j] * 1.765)));
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