package dk.hydrozoa.hydrowiki.database;

public class Counter {

    private int count = 0;

    public void reset() {
        count = 0;
    }

    public void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }

}
