package com.tslang;

public class Label {
    private int labelCounter;

    public void setLabelCounter(int labelCounter) {
        this.labelCounter = labelCounter;
    }

    public Label() {
        this.labelCounter = 0;
    }
    public String getLabel(){
        labelCounter++;
        return "label"+labelCounter;
    }
}
