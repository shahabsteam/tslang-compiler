package com.tslang;

import java.util.HashMap;
import java.util.Map;

public class TempVar {
    private int counter;
    private final Map<String, String> values = new HashMap<>();
     TempVar(){
        this.counter=0;
    }
    public String  getTempVar(){
         String name = "r"+Integer.toString(counter);
         counter++;
         return name;
    }
    public void setCounter(int counter){
         this.counter=counter;
    }
    String get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }

     return null;

    }
    String getOrSet(String name ){
        if (values.containsKey(name)) {
            return values.get(name);
        }
        return assign(name);


    }
    String   assign(String name){
         String temp =getTempVar();
         values.put(name,temp);
         return temp;
    }

}
