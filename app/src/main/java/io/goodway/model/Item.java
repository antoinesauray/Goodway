package io.goodway.model;

/**
 * Created by antoine on 8/22/15.
 */
public abstract class Item implements Comparable<Item>{
    @Override
    public int compareTo(Item another) {
        return 0;
    }

    @Override
    public String toString(){
        return "abstract item";
    }
}
