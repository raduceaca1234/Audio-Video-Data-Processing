import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

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
    private double[][] YFinal;
    private double[][] UFinal;
    private double[][] VFinal;
    private double[][] Q;
    private List<List<Integer>> indexesList;
    public double[][] DCT;

    public Decoder(String resultingFile, String yFile, String uFile, String vFile, int width, int height, List<String> header, double[][] Q, List<List<Integer>> indexesList, double[][] DCT, double[][] Y
             , double[][] U
             , double[][] V) {
        this.resultingFile = resultingFile;
        this.yFile = yFile;
        this.uFile = uFile;
        this.vFile = vFile;
        this.width = width;
        this.height = height;
        this.header = header;
        this.Y = new double[height][width];
        this.U = new double[height][width];
        this.V = new double[height][width];
        this.YFinal = new double[height][width];
        this.UFinal = new double[height][width];
        this.VFinal = new double[height][width];
        this.Q = Q;
        this.indexesList = indexesList;
        createYUVandDeQuantization(yFile, "Y");
        createYUVandDeQuantization(uFile, "U");
        createYUVandDeQuantization(vFile, "V");
        this.DCT = DCT;
    }

    private void createYUVandDeQuantization(String filename, String component) {
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
                    int i, j, k, l, a, b;
                    if (component.equals("Y")) {

                        for (i = i1,a=0, k = 0; i <= i2; i++, k++, a++) {
                            lineVal = lines.get(k).split(" ");
                            for (j = j1,b=0, l = 0; j <= j2; j++, l++, b++) {
                                Y[i][j] = Double.parseDouble(lineVal[l])*Q[a][b];
                            }
                        }
                    }
                    if (component.equals("U")) {
                        for (i = i1,a=0, k = 0; i <= i2; i++, k++, a++) {
                            lineVal = lines.get(k).split(" ");
                            for (j = j1,b=0, l = 0; j <= j2; j++, l++, b++) {
                                U[i][j] = Double.parseDouble(lineVal[l])*Q[a][b];
                            }
                        }
                    }
                    if (component.equals("V")) {
                        for (i = i1,a=0, k = 0; i <= i2; i++, k++, a++) {
                            lineVal = lines.get(k).split(" ");
                            for (j = j1,b=0, l = 0; j <= j2; j++, l++, b++) {
                                V[i][j] = Double.parseDouble(lineVal[l])*Q[a][b];
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

    private int samplingFunction(int value) {
        if (value > 255)
            value = 255;
        if (value < 0)
            value = 0;
        return value;
    }


    private void inverseDCT(int i1, int j1) throws IOException {
        int u, v;
        for (int x=0; x <= 7;  x++) {
            for (int y = 0; y <= 7; y++) {
                double rez1 = 0;
                double rez2 = 0;
                double rez3 = 0;
                for (u = 0; u <=7; u++) {
                    for (v = 0; v <=7; v++) {
                        double alpha1 = 0, alpha2 = 0;
                        if(u==0)
                            alpha1 = 1.0 / sqrt(2);
                        else if(u>0)
                            alpha1 = 1.0;
                        if(v==0)
                            alpha2 = 1.0 / sqrt(2);
                        else if(v>0)
                            alpha2 = 1.0;
                        double cos = cos((((2 * x) + 1) * u * PI) / 16) * cos((((2 * y) + 1) * v * PI) / 16);
                        rez1 += Y[i1+u][j1+v] * alpha1 * alpha2 * cos;
                        rez2 += U[i1+u][j1+v] * alpha1 * alpha2 * cos;
                        rez3 += V[i1+u][j1+v] * alpha1 * alpha2 * cos;
                    }
                }
                System.out.println(rez1);
                rez1 = 0.25 * rez1;
                System.out.println(rez1);
                rez2 = 0.25 * rez2;
                rez3 = 0.25 * rez3;
                YFinal[i1+x][j1+y] = rez1;
                UFinal[i1+x][j1+y] = rez2;
                VFinal[i1+x][j1+y] = rez3;
            }
        }
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

    public void generateFinalImage() {
        for (List<Integer> matrix : indexesList) {
            try {
                inverseDCT(matrix.get(0), matrix.get(1));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
