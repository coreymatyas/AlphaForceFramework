package com.coreymatyas.irc.AlphaForceFramework;

import java.io.BufferedReader;

public class ChatInThread extends Thread{
    private BufferedReader reader = null;
    private IrcBot bot = null;
    private boolean running = false;

    public ChatInThread(BufferedReader reader, IrcBot bot){
        this.reader = reader;
        this.bot = bot;
        
        running = true;
        this.setName("IRCBot-InputThread");
    }

    public void stopWorking(){
        running = false;
    }

    public void run() {
        while(running){
            String lineIn = "";
            try {
                lineIn = reader.readLine();
            } catch (Exception ex){
                ex.printStackTrace();                
            }
            if(lineIn != null){
                if(!lineIn.equals("")){
                    String[] lines = lineIn.split("\n");
                    for(String line:lines){
                        bot.processChat(line);
                    }
                }
            }
        }
    }

}
