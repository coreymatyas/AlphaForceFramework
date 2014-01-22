package com.coreymatyas.irc.AlphaForceFramework;

import java.util.ArrayList;

public class CustomQueue {
    ArrayList queue = new ArrayList();

    public CustomQueue(){}

    public void add(Object o){
        queue.add(o);
    }

    public void addFront(Object o){
        queue.add(0, o);
    }

    public boolean hasNext(){
        return !queue.isEmpty();
    }

    public Object next(){
        Object ret = queue.get(0);
        queue.remove(0);
        return ret;
    }

    public int size(){
        return queue.size();
    }
}
