public class Triplet {
    private int RUNLENGTH;
    private int SIZE;
    private int AMPLITUDE;

    public Triplet(int RUNLENGTH, int SIZE, int AMPLITUDE) {
        this.RUNLENGTH = RUNLENGTH;
        this.SIZE = SIZE;
        this.AMPLITUDE = AMPLITUDE;
    }

    public int getRUNLENGTH() {
        return RUNLENGTH;
    }

    public void setRUNLENGTH(int RUNLENGTH) {
        this.RUNLENGTH = RUNLENGTH;
    }

    public int getSIZE() {
        return SIZE;
    }

    public void setSIZE(int SIZE) {
        this.SIZE = SIZE;
    }

    public int getAMPLITUDE() {
        return AMPLITUDE;
    }

    public void setAMPLITUDE(int AMPLITUDE) {
        this.AMPLITUDE = AMPLITUDE;
    }
}
