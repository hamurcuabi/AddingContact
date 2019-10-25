package com.emrhmrc.addcontactlist;

public class RangeModel {

    private int start;
    private int end;

    public RangeModel(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public RangeModel() {
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
