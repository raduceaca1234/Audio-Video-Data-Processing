import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

public class Encoder {

    public List<String> header;
    public int width;
    public int height;
    public double[][] Q = {
            {6, 4, 4, 6, 10, 16, 20, 24},
            {5, 5, 6, 8, 10, 23, 24, 22},
            {6, 5, 6, 10, 16, 23, 28, 22},
            {6, 7, 9, 12, 20, 35, 32, 25},
            {7, 9, 15, 22, 27, 44, 41, 31},
            {10, 14, 22, 26, 32, 42, 45, 37},
            {20, 26, 31, 35, 41, 48, 48, 40},
            {29, 37, 38, 39, 45, 40, 41, 40}
    };
    public List<List<Integer>> indexesList;
    private final String file;
    public double[][] Y;
    public double[][] U;
    public double[][] V;
    public double[][] DCT;

    public Encoder(String file) {
        this.file = file;
        initializeYUV();
    }

    private void initializeYUV() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            int nr = 0, val = 0, i = 0, j = 0, R = 0, G = 0, B = 0;
            String line;
            header = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                if (nr < 3) {
                    if (nr == 1 && !line.startsWith("#")) {
                        width = Integer.parseInt(line.split(" ")[0]);
                        height = Integer.parseInt(line.split(" ")[1]);
                        Y = new double[height][width];
                        U = new double[height][width];
                        V = new double[height][width];
                    }
                    if (!line.startsWith("#"))
                        nr++;
                    header.add(line);
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
        initializeListOfIndexes();
    }

    private void initializeListOfIndexes() {
        indexesList = new ArrayList<>();
        int i1 = 0, j1 = 0, i2 = 7, j2 = 7;
        while (i2 < height) {
            if (j2 > width) {
                j1 = 0;
                j2 = 7;
                i1 += 8;
                i2 += 8;
            } else {
                indexesList.add(new ArrayList<>(Arrays.asList(i1, j1, i2, j2)));
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
                for (List<Integer> matrix : indexesList) {
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
                for (List<Integer> matrix : indexesList) {
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

    private void transformFrom4x4To8x8(String filename, String component) {
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

                    ok=true;
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void substract128() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Y[i][j] -= 128;
                U[i][j] -= 128;
                V[i][j] -= 128;
            }
        }
    }

    private void applyFormulaAndQuantizationPhase(int i1, int j1, int i2, int j2, String filename, String component) throws IOException {
        DCT = new double[8][8];
        int u, v;
        double alpha1, alpha2;
        for (u = 0; u <=7; u++) {
            for (v = 0; v <=7; v++) {
                double rez = 0;
                for (int x=0; x <= 7; x++) {
                    for (int y=0; y <= 7;  y++) {
                        double cos = cos((((2 * x) + 1) * u * PI) / 16) * cos((((2 * y) + 1) * v * PI) / 16);

                        if (component.equals("Y")) {
                            rez += Y[i1+x][j1+y] * cos;
                        }
                        if (component.equals("U")) {
                            rez += U[i1+x][j1+y] * cos;
                        }
                        if (component.equals("V")) {
                            rez += V[i1+x][j1+y] * cos;
                        }
                    }
                }
                alpha1 = 0; alpha2 = 0;
                if(u==0)
                    alpha1 = 1.0 / sqrt(2);
                else if(u>0)
                    alpha1 = 1.0;
                if(v==0)
                    alpha2 = 1.0 / sqrt(2);
                else if(v>0)
                    alpha2 = 1.0;
                double rez2 = 0.25 * alpha1 * alpha2 * rez;
                DCT[u][v] = rez2;
            }
        }

        FileWriter fileWriter;
        if (i1 == 0 && j1 == 0)
            fileWriter = new FileWriter(filename);
        else
            fileWriter = new FileWriter(filename, true);
        try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(Integer.toString(i1) + " " + Integer.toString(j1) + " " +
                    Integer.toString(i2) + " " + Integer.toString(j2));
            bufferedWriter.newLine();
            for (int i = 0; i <= 7; i++) {
                for (int j = 0; j <= 7; j++) {
                    bufferedWriter.write(Double.toString(DCT[i][j] / Q[i][j]));
                    bufferedWriter.write(" ");
                }
                bufferedWriter.newLine();
            }
            bufferedWriter.newLine();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }


    void generateDCTBlocks(String filename1, String filename2, String filename3) {
        transformFrom4x4To8x8("V.txt", "V");
        transformFrom4x4To8x8("U.txt", "U");
        substract128();
        for (List<Integer> indexes : indexesList) {
            try {
                applyFormulaAndQuantizationPhase(indexes.get(0), indexes.get(1), indexes.get(2), indexes.get(3),
                        filename1, "Y");
                applyFormulaAndQuantizationPhase(indexes.get(0), indexes.get(1), indexes.get(2), indexes.get(3),
                        filename2, "U");
                applyFormulaAndQuantizationPhase(indexes.get(0), indexes.get(1), indexes.get(2), indexes.get(3),
                        filename3, "V");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
