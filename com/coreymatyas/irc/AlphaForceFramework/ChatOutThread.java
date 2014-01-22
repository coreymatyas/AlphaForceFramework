package com.coreymatyas.irc.AlphaForceFramework;

import java.io.BufferedWriter;

public class ChatOutThread extends Thread{
    CustomQueue outQueue = new CustomQueue();
    CustomQueue outQueueLow = new CustomQueue();
    private IrcBot bot = null;
    private boolean running = false;
    private String messageOut = "";

    private final int SHORT_DELAY = 300;
    private final int LONG_DELAY = 800;

    private int delay = LONG_DELAY;
    private BufferedWriter writer = null;

    public ChatOutThread(IrcBot bot,BufferedWriter writer){
        this.writer = writer;
        this.bot = bot;
        running = true;
        this.setName("IRCBot-OutputThread");
    }

    public void stopWorking(){
        running = false;
    }

    public void setDelay(Integer delay){
        this.delay = delay;
    }

    public void addMessage(String message,ChatPriority priority){
        switch(priority){
            case NORMAL:
                outQueue.add(message);
                break;
            case HIGH:
                outQueue.addFront(message);
                break;
            case LOW:
                outQueueLow.add(message);
                break;
        }
    }

    public void run(){
        Integer tempDelay = delay;
        Boolean lowPriority = false;
        try{
            while(running){
                Thread.sleep(tempDelay);

                if(!outQueue.hasNext() && outQueueLow.hasNext()){
                    messageOut = (String)outQueueLow.next();
                    lowPriority = true;
                }else if(outQueue.hasNext()){
                    messageOut = (String)outQueue.next();                    
                }
                if(!messageOut.equals("")){
                    writer.write(messageOut+"\r\n");
                    writer.flush();
                }
                if(lowPriority){
                    tempDelay = delay;
                }else{
                    tempDelay = SHORT_DELAY;
                }
                messageOut = "";
                lowPriority = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
