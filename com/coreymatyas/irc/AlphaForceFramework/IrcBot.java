package com.coreymatyas.irc.AlphaForceFramework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class IrcBot {
    protected String realName = "IRC Bot";
    protected String nickName = "IRCBot";
    protected String host = "";
    protected String login = nickName;
    protected boolean verbose = false;
    protected final String DATA_DIR = "data/";

    public BufferedReader bufferedReader = null;
    public BufferedWriter bufferedWriter = null;

    protected ArrayList<ArrayList> commandHandlers = new ArrayList<ArrayList>();
    protected HashMap<String,Object> commandPersistantVariables = new HashMap<String, Object>();

    protected ChatInThread inThread = null;
    protected ChatOutThread outThread = null;

    public IrcBot(){
        registerCommand("PING", new PingHandler());
        onStart();
    }

    public void setNickname(String nickName){
        this.nickName = nickName;
    }
    public void setRealName(String realName){
        this.realName = realName;
    }
    public void setLogin(String login){
        this.login = login;
    }
    public void setVerbose(boolean state){
        this.verbose = state;
    }
    public String getNickname(){
        return nickName;
    }
    public String getRealName(){
        return realName;
    }
    public String getLogin(){
        return login;
    }
    public boolean getVerbose(){
        return verbose;
    }
    public String getHost(){
        return host;
    }

    public void connect(String hostname) throws UnknownHostException, IOException{
        connect(hostname, null, 6667);
    }

    public void connect(String hostname, String password, Integer port) throws UnknownHostException, IOException{
        Socket socket = new Socket(hostname, port);
        OutputStreamWriter sockWriter = null;
        InputStreamReader sockReader = null;

        sockReader = new InputStreamReader(socket.getInputStream());
        sockWriter = new OutputStreamWriter(socket.getOutputStream());        

        bufferedReader = new BufferedReader(sockReader);
        bufferedWriter = new BufferedWriter(sockWriter);

        inThread = new ChatInThread(bufferedReader,this);
        outThread = new ChatOutThread(this,bufferedWriter);
        inThread.start();
        outThread.start();
        
        if(password != null){
            if(!password.equals("")){
                sendRawLine("PASS "+password);
            }
        }
        changeNickname(nickName);
        sendRawLine("USER "+login+" "+hostname+" * :"+realName);
        host = hostname;
        onConnect();
    }

    public void identServer(String hostname) throws UnknownHostException, IOException{
        Socket identSocket = new Socket(hostname, 113);
        BufferedReader identReader = new BufferedReader(new InputStreamReader(identSocket.getInputStream()));
        BufferedWriter identWriter = new BufferedWriter(new OutputStreamWriter(identSocket.getOutputStream()));
        String identResponse = identReader.readLine();
        System.out.println("Ident response: "+identResponse);
        identWriter.write(identResponse+" : USERID : WINDOWS 7 : "+login);
        identWriter.flush();
    }

    public void quitServer(){
        quitServer("");
    }

    public void quitServer(String reason){
        sendRawLine("QUIT :"+reason, ChatPriority.HIGH);
    }

    public void joinChannel(String channel){
        sendRawLine("JOIN "+channel);
        onJoinChannel(channel);
    }

    public void leaveChannel(String channel){
        sendRawLine("PART "+channel);
    }
    
    public void changeNickname(String nickname){
        this.nickName = nickname;
        sendRawLine("NICK "+nickName);
    }

    public synchronized void processChat(String chat){
        String from = "";String hostname = "";
        String channel = "";String message = "";
        String command = "";String rawMessage = "";
        boolean isCommand = false;

        log(">>>"+chat);

        chat = chat.trim();
        try {
            Pattern regex = Pattern.compile(":(.*)!(.*?)@(?:.*?) (.*?) :(.*)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Pattern altRegex = Pattern.compile(":(.*)!(.*?)@(.*?) (.+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher regexMatcher = regex.matcher(chat);
            Matcher altRegexMatcher = altRegex.matcher(chat);
            if (regexMatcher.find()) {
                from = regexMatcher.group(1);
                hostname = regexMatcher.group(2);
                rawMessage = regexMatcher.group(3);
                message = regexMatcher.group(4);
                String[] temp = rawMessage.split(" ");
                if(temp.length >=2){
                    channel = temp[1];
                    command = temp[0];
                }
            }else if(altRegexMatcher.find()){
                from = altRegexMatcher.group(1);
                hostname = altRegexMatcher.group(2);
                rawMessage = altRegexMatcher.group(4);
                command = rawMessage.split(" ")[0];
                if(!rawMessage.isEmpty()){
                    Integer spaceIndex = rawMessage.indexOf(" ");
                    if(spaceIndex != -1){
                        message = rawMessage.substring(spaceIndex);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        StringTokenizer argumentTokenizer = new StringTokenizer(message," ");
        Integer numArgs = argumentTokenizer.countTokens();

        String[] args = new String[numArgs];        
        int t=0;
        while(argumentTokenizer.hasMoreTokens()){
            args[t] = argumentTokenizer.nextToken();
            t++;
        }

        if(command.equalsIgnoreCase("MODE")){
            channel = args[0];
        }

        Iterator itr = commandHandlers.iterator();
        while(itr.hasNext()){
            ArrayList content = (ArrayList)itr.next();
            String key = (String)content.get(0);
            if(key.startsWith("*")){
                if(chat.toLowerCase().contains(key.substring(1).toLowerCase())){
                    isCommand = true;
                }
            }else if(key.startsWith("~")){
                if(rawMessage.toLowerCase().startsWith(key.substring(1).toLowerCase())){
                    isCommand = true;
                }
            }else{
                if(!command.equals("PRIVMSG")){
                    if(chat.toLowerCase().startsWith(key.toLowerCase())){
                        isCommand = true;
                    }
                }else{
                    if(message.toLowerCase().startsWith(key.toLowerCase())){
                        isCommand = true;
                    }
                }
            }
            if(isCommand){
                CommandHandler handler = (CommandHandler)content.get(1);
                handler.executeCommand(this, args, chat, from, hostname, channel);
            }
            isCommand = false;
        }
        onChatIn(chat);
    }

    public void registerCommand(String command,CommandHandler handler){
        ArrayList content = new ArrayList();
        content.add(command);
        content.add(handler);
        commandHandlers.add(content);
    }

    public void registerVariable(String key,Object value){
        commandPersistantVariables.put(key,value);
    }

    public Object getVariable(String name){
        return commandPersistantVariables.get(name);
    }

    public void changeVariable(String key,Object value){
        commandPersistantVariables.remove(key);
        commandPersistantVariables.put(key,value);
    }

    public void sendRawLine(String message,ChatPriority priority){
        try{
            outThread.addMessage(message, priority);
            log("<<<"+message);
        }catch(Exception e){
        }
        onChatOut(message);
    }

    public void sendRawLine(String message){
        sendRawLine(message,ChatPriority.NORMAL);
    }

    public void sendMessage(String recipient, String message){
        sendRawLine("PRIVMSG "+recipient+" :"+message);
    }

    public void sendCTCPCommand(String recipient, String message){
        sendRawLine("PRIVMSG " + recipient + " :\u0001" + message + "\u0001");
    }

    public void sendMessage(String recipient, String message,ChatPriority priority){
        sendRawLine("PRIVMSG "+recipient+" :"+message,priority);
    }

    public void sendCTCPCommand(String recipient, String message,ChatPriority priority){
        sendRawLine("PRIVMSG " + recipient + " :\u0001" + message + "\u0001",priority);
    }

    public void log(String message){
        if(verbose){
            System.out.println(message);
        }
    }

    public String getDataDir(){
        if(!new File(DATA_DIR).exists()){
            new File(DATA_DIR).mkdir();
        }
        return DATA_DIR;
    }

    public void die(){
        quitServer();
        Iterator itr = commandHandlers.iterator();
        while(itr.hasNext()){
            CommandHandler handler = (CommandHandler)((ArrayList)itr.next()).get(1);
            handler.die(this);
        }
        outThread.stopWorking();
        inThread.stopWorking();
    }

    /*Events*/
    protected void onStart(){}
    protected void onConnect(){}
    protected void onJoinChannel(String channel){}
    protected void onChatIn(String chat){}
    protected void onChatOut(String chat){}
}
