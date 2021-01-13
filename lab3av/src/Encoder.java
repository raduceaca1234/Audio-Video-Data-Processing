import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Encoder {

    public List<String> header;
    public int width;
    public int height;
    public int[][] Q = {
            {6, 4, 4, 6, 10, 16, 20, 24},
            {5, 5, 6, 8, 10, 23, 24, 22},
            {6, 5, 6, 10, 16, 23, 28, 22},
            {6, 7, 9, 12, 20, 35, 32, 25},
            {7, 9, 15, 22, 27, 44, 41, 31},
            {10, 14, 22, 26, 32, 42, 45, 37},
            {20, 26, 31, 35, 41, 48, 48, 40},
            {29, 37, 38, 39, 45, 40, 41, 40}
    };
    public List<List<Integer>> listOfIndexes8x8;
    public List<Triplet> entropyEncoding;
    public List<List<Integer>> amplitudeAndSize;
    private final String file;
    private double[][] Y;
    private double[][] U;
    private double[][] V;
    private final List<int[][]> listOfMatrix8x8Y;
    private final List<int[][]> listOfMatrix8x8U;
    private final List<int[][]> listOfMatrix8x8V;

    public Encoder(String file) {
        this.file = file;
        this.listOfMatrix8x8Y = new ArrayList<>();
        this.listOfMatrix8x8U = new ArrayList<>();
        this.listOfMatrix8x8V = new ArrayList<>();
        this.entropyEncoding = new ArrayList<>();
        this.amplitudeAndSize = new ArrayList<>();
        this.amplitudeAndSize.add(List.of(0, 1));
        this.amplitudeAndSize.add(List.of(2, 3));
        this.amplitudeAndSize.add(List.of(4, 7));
        this.amplitudeAndSize.add(List.of(8, 15));
        this.amplitudeAndSize.add(List.of(16, 31));
        this.amplitudeAndSize.add(List.of(32, 63));
        this.amplitudeAndSize.add(List.of(64, 127));
        this.amplitudeAndSize.add(List.of(128, 255));
        this.amplitudeAndSize.add(List.of(256, 511));
        this.amplitudeAndSize.add(List.of(512, 1023));
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

    private void convert4x4To8x8(String filename, String component) {
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
                    ok = true;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void substract128FromBlocks() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Y[i][j] -= 128;
                U[i][j] -= 128;
                V[i][j] -= 128;
            }
        }
    }

    private int[][] applyFormulaAndQuantization(double[][] matrix) {
        double[][] formulaMatrix = new double[8][8];
        int[][] rezQuant = new int[8][8];
        int u, v;
        for (u = 0; u < 8; u++) {
            for (v = 0; v < 8; v++) {
                double rez = 0;
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        var cos = Math.cos((2 * x + 1) * u * Math.PI / 16) * Math.cos((2 * y + 1) * v * Math.PI / 16);
                        rez += matrix[x][y] * cos;
                    }
                }
                double alpha1 = u == 0 ? 1 / Math.sqrt(2) : 1;
                double alpha2 = v == 0 ? 1 / Math.sqrt(2) : 1;
                rez = 1. / 4 * alpha1 * alpha2 * rez;
                formulaMatrix[u][v] = rez;
            }
        }
        for (u = 0; u < 8; u++)
            for (v = 0; v < 8; v++)
                rezQuant[u][v] = (int) formulaMatrix[u][v] / Q[u][v];
        return rezQuant;
    }

    private void generateDCTBlocks() {
        convert4x4To8x8("V.txt", "V");
        convert4x4To8x8("U.txt", "U");
        substract128FromBlocks();
        for (List<Integer> matrix : listOfIndexes8x8) {
            double[][] block = new double[8][8];
            for (int i = matrix.get(0), x = 0; i <= matrix.get(2); i++, x++) {
                for (int j = matrix.get(1), y = 0; j <= matrix.get(3); j++, y++)
                    block[x][y] = Y[i][j];
            }
            listOfMatrix8x8Y.add(applyFormulaAndQuantization(block));
        }
        for (List<Integer> matrix : listOfIndexes8x8) {
            double[][] block = new double[8][8];
            for (int i = matrix.get(0), x = 0; i <= matrix.get(2); i++, x++) {
                for (int j = matrix.get(1), y = 0; j <= matrix.get(3); j++, y++)
                    block[x][y] = U[i][j];
            }
            listOfMatrix8x8U.add(applyFormulaAndQuantization(block));
        }
        for (List<Integer> matrix : listOfIndexes8x8) {
            double[][] block = new double[8][8];
            for (int i = matrix.get(0), x = 0; i <= matrix.get(2); i++, x++) {
                for (int j = matrix.get(1), y = 0; j <= matrix.get(3); j++, y++)
                    block[x][y] = V[i][j];
            }
            listOfMatrix8x8V.add(applyFormulaAndQuantization(block));
        }
    }

    private List<Integer> zigZagMatrix(int[][] arr, int n, int m) {
        int row = 0, col = 0;
        List<Integer> result = new ArrayList<>();

        // Boolean variable that will true if we
        // need to increment 'row' value otherwise
        // false- if increment 'col' value
        boolean row_inc = false;

        int mn = Math.min(m, n);
        for (int len = 1; len <= mn; ++len) {
            for (int i = 0; i < len; ++i) {
                result.add(arr[row][col]);

                if (i + 1 == len)
                    break;
                // If row_increment value is true
                // increment row and decrement col
                // else decrement row and increment
                // col
                if (row_inc) {
                    ++row;
                    --col;
                } else {
                    --row;
                    ++col;
                }
            }
            System.out.println(result+"\n");

            if (len == mn)
                break;

            // Update row or col valaue according
            // to the last increment
            if (row_inc) {
                ++row;
                row_inc = false;
            } else {
                ++col;
                row_inc = true;
            }
        }

        // Update the indexes of row and col variable
        if (row == 0) {
            if (col == m - 1)
                ++row;
            else
                ++col;
            row_inc = true;
        } else {
            if (row == n - 1)
                ++col;
            else
                ++row;
            row_inc = false;
        }

        int MAX = Math.max(m, n) - 1;
        for (int len, diag = MAX; diag > 0; --diag) {

            len = Math.min(diag, mn);

            for (int i = 0; i < len; ++i) {
                result.add(arr[row][col]);

                if (i + 1 == len)
                    break;

                // Update row or col value according
                // to the last increment
                if (row_inc) {
                    ++row;
                    --col;
                } else {
                    ++col;
                    --row;
                }
            }

            // Update the indexes of row and col variable
            if (row == 0 || col == m - 1) {
                if (col == m - 1)
                    ++row;
                else
                    ++col;

                row_inc = true;
            } else if (col == 0 || row == n - 1) {
                if (row == n - 1)
                    ++col;
                else
                    ++row;

                row_inc = false;
            }
        }
        return result;
    }

    private int getSize(int AMPLITUDE) {
        int size = 0;
        for (int j = 0; j < 10; j++) {
            if (AMPLITUDE >= amplitudeAndSize.get(j).get(0) && AMPLITUDE <= amplitudeAndSize.get(j).get(1)
                    || AMPLITUDE <= -amplitudeAndSize.get(j).get(0) && AMPLITUDE >= -amplitudeAndSize.get(j).get(1)) {
                size = j + 1;
            }
        }
        return size;
    }

    public void entropyEncoding() {
        generateDCTBlocks();
        for (int i = 0; i < 7500; i++) {
            List<Integer> zigzagY = zigZagMatrix(listOfMatrix8x8Y.get(i), 8, 8);
            List<Integer> zigzagU = zigZagMatrix(listOfMatrix8x8U.get(i), 8, 8);
            List<Integer> zigzagV = zigZagMatrix(listOfMatrix8x8V.get(i), 8, 8);
            int size1 = getSize(zigzagY.get(0)), size2 = getSize(zigzagU.get(0)), size3 = getSize(zigzagV.get(0));
            entropyEncoding.add(new Triplet(-1, size1, zigzagY.get(0)));
            int nr0 = 0;
            for (int j = 1; j < zigzagY.size(); j++) {
                int AMPLITUDE = zigzagY.get(j);
                if (j == 63 && AMPLITUDE == 0) {
                    entropyEncoding.add(new Triplet(0, 0, 0));
                } else {
                    if (AMPLITUDE == 0) {
                        nr0++;
                    } else {
                        entropyEncoding.add(new Triplet(nr0, getSize(AMPLITUDE), AMPLITUDE));
                        nr0 = 0;
                    }
                }
            }
            entropyEncoding.add(new Triplet(-1, size2, zigzagU.get(0)));
            nr0 = 0;
            for (int j = 1; j < zigzagU.size(); j++) {
                int AMPLITUDE = zigzagU.get(j);
                if (j == 63 && AMPLITUDE == 0) {
                    entropyEncoding.add(new Triplet(0, 0, 0));
                } else {
                    if (AMPLITUDE == 0) {
                        nr0++;
                    } else {
                        entropyEncoding.add(new Triplet(nr0, getSize(AMPLITUDE), AMPLITUDE));
                        nr0 = 0;
                    }
                }
            }
            entropyEncoding.add(new Triplet(-1, size3, zigzagV.get(0)));
            nr0 = 0;
            for (int j = 1; j < zigzagV.size(); j++) {
                int AMPLITUDE = zigzagV.get(j);
                if (j == 63 && AMPLITUDE == 0) {
                    entropyEncoding.add(new Triplet(0, 0, 0));
                } else {
                    if (AMPLITUDE == 0) {
                        nr0++;
                    } else {
                        entropyEncoding.add(new Triplet(nr0, getSize(AMPLITUDE), AMPLITUDE));
                        nr0 = 0;
                    }
                }
            }
        }
    }
}
