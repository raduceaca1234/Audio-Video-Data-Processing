import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

public class Decoder {

    private final int width;
    private final int height;
    private final String resultingFile;
    private final List<String> header;
    private double[][] Y;
    private double[][] U;
    private double[][] V;
    private double[][] Y2;
    private double[][] U2;
    private double[][] V2;
    private int[][] Q;
    private List<List<Integer>> listOfIndexes8x8;
    private final List<int[][]> listOfMatrix8x8Y;
    private final List<int[][]> listOfMatrix8x8U;
    private final List<int[][]> listOfMatrix8x8V;
    public List<Triplet> entropyEncoding;
    public List<List<Integer>> amplitudeAndSize;

    public Decoder(String resultingFile, int width, int height, List<String> header, int[][] Q, List<List<Integer>> listOfIndexes8x8, List<Triplet> entropyEncoding, List<List<Integer>> amplitudeAndSize) {
        this.listOfMatrix8x8Y = new ArrayList<>();
        this.listOfMatrix8x8U = new ArrayList<>();
        this.listOfMatrix8x8V = new ArrayList<>();
        this.resultingFile = resultingFile;
        this.width = width;
        this.height = height;
        this.header = header;
        this.Y = new double[height][width];
        this.U = new double[height][width];
        this.V = new double[height][width];
        this.Y2 = new double[height][width];
        this.U2 = new double[height][width];
        this.V2 = new double[height][width];
        this.Q = Q;
        this.listOfIndexes8x8 = listOfIndexes8x8;
        this.entropyEncoding = entropyEncoding;
        this.amplitudeAndSize = amplitudeAndSize;
        readAndCreateYUVMatrix();
    }

    private void zigZagMatrix(int[][] arr, List<Integer> array, int n, int m) {
        int row = 0, col = 0;
        List<Integer> result = new ArrayList<>();

        // Boolean variable that will true if we
        // need to increment 'row' value otherwise
        // false- if increment 'col' value
        boolean row_inc = false;
        int nr = -1;

        // Print matrix of lower half zig-zag pattern
        int mn = Math.min(m, n);
        for (int len = 1; len <= mn; ++len) {
            for (int i = 0; i < len; ++i) {
                nr++;
                arr[row][col] = array.get(nr);

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

        // Print the next half zig-zag pattern
        int MAX = Math.max(m, n) - 1;
        for (int len, diag = MAX; diag > 0; --diag) {

            if (diag > mn)
                len = mn;
            else
                len = diag;

            for (int i = 0; i < len; ++i) {
                nr++;
                arr[row][col] = array.get(nr);

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
    }

    private void readAndCreateYUVMatrix() {
        int comp = -1;
        int i = 0;
        for (i = 0; i < entropyEncoding.size(); i++) {
            if (entropyEncoding.get(i).getRUNLENGTH() == -1) {
                comp++;
                List<Integer> matrixAsList = new ArrayList<>();
                matrixAsList.add(entropyEncoding.get(i).getAMPLITUDE());
                int nr = 0;
                while (!(entropyEncoding.get(i).getRUNLENGTH() == 0 && entropyEncoding.get(i).getSIZE() == 0 && entropyEncoding.get(i).getAMPLITUDE() == 0)) {
                    if (nr == 63) {
                        break;
                    }
                    i++;
                    Triplet triplet = entropyEncoding.get(i);
                    if (triplet.getRUNLENGTH() == 0) {
                        nr++;
                        matrixAsList.add(triplet.getAMPLITUDE());
                    } else {
                        int RUNLENGTH = triplet.getRUNLENGTH();
                        while (RUNLENGTH != 0) {
                            nr++;
                            matrixAsList.add(0);
                            RUNLENGTH--;
                        }
                        nr++;
                        matrixAsList.add(triplet.getAMPLITUDE());
                    }
                }
                if (nr != 63) {
                    while (nr < 63) {
                        matrixAsList.add(0);
                        nr++;
                    }
                }
                int[][] matrix = new int[8][8];
                zigZagMatrix(matrix, matrixAsList, 8, 8);
                for (int m = 0; m < 8; m++) {
                    for (int n = 0; n < 8; n++) {
                        matrix[m][n] = matrix[m][n] * Q[m][n];
                    }
                }
                if (comp % 3 == 0) {
                    listOfMatrix8x8Y.add(matrix);
                }
                if (comp % 3 == 1) {
                    listOfMatrix8x8U.add(matrix);
                }
                if (comp % 3 == 2) {
                    listOfMatrix8x8V.add(matrix);
                }
            }
        }
        int k, nr = -1;
        for (List<Integer> index : listOfIndexes8x8) {
            nr++;
            int[][] matrix1 = listOfMatrix8x8Y.get(nr);
            int[][] matrix2 = listOfMatrix8x8U.get(nr);
            int[][] matrix3 = listOfMatrix8x8V.get(nr);
            for (i = index.get(0), k = 0; i <= index.get(2); i++, k++)
                for (int j = index.get(1), l = 0; j <= index.get(3); j++, l++) {
                    Y[i][j] = matrix1[k][l];
                    U[i][j] = matrix2[k][l];
                    V[i][j] = matrix3[k][l];
                }
        }
    }

    private int sampling(int value) {
        if (value > 255)
            value = 255;
        if (value < 0)
            value = 0;
        return value;
    }


    private void applyFormulaAndDeQuantization(int i1, int j1, int i2, int j2) throws IOException {
        int u, v;
        for (int x = 0; x <= 7; x++) {
            for (int y = 0; y <= 7; y++) {
                double rez1 = 0;
                double rez2 = 0;
                double rez3 = 0;
                for (u = 0; u <= 7; u++) {
                    for (v = 0; v <= 7; v++) {
                        double alpha1 = u == 0 ? 1 / Math.sqrt(2) : 1;
                        double alpha2 = v == 0 ? 1 / Math.sqrt(2) : 1;
                        double cos = cos((((2 * x) + 1) * u * PI) / 16) * cos((((2 * y) + 1) * v * PI) / 16);
                        rez1 += Y[i1 + u][j1 + v] * alpha1 * alpha2 * cos;
                        rez2 += U[i1 + u][j1 + v] * alpha1 * alpha2 * cos;
                        rez3 += V[i1 + u][j1 + v] * alpha1 * alpha2 * cos;
                    }
                }
                rez1 = 1. / 4 * rez1;
                rez2 = 1. / 4 * rez2;
                rez3 = 1. / 4 * rez3;
                Y2[i1 + x][j1 + y] = rez1;
                U2[i1 + x][j1 + y] = rez2;
                V2[i1 + x][j1 + y] = rez3;
            }
        }
    }

    public void generateResultingFile() {
        for (List<Integer> matrix : listOfIndexes8x8) {
            try {
                applyFormulaAndDeQuantization(matrix.get(0), matrix.get(1), matrix.get(2), matrix.get(3));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Y2[i][j] += 128;
                U2[i][j] += 128;
                V2[i][j] += 128;
            }
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(resultingFile))) {
            for (String head : header) {
                bufferedWriter.write(head);
                bufferedWriter.newLine();
            }
            int i, j;
            for (i = 0; i < height; i++) {
                for (j = 0; j < width; j++) {
                    String R = Integer.toString(sampling(((int) (Y2[i][j] * 1.000 + V2[i][j] * 1.400 - 128 * 1.400))));
                    String G = Integer.toString(sampling((int) (Y2[i][j] * 1.000 + 128 * 0.343 - U2[i][j] * 0.343 + 128 * 0.711 - V2[i][j] * 0.711)));
                    String B = Integer.toString(sampling((int) (Y2[i][j] * 1.000 - 128 * 1.765 + U2[i][j] * 1.765)));
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
