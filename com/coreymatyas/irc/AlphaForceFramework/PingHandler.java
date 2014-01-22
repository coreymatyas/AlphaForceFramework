package com.coreymatyas.irc.AlphaForceFramework;

public class PingHandler implements CommandHandler{

    public void executeCommand(IrcBot bot, String[] args, String rawMessage, String sender, String hostname, String channel) {
        bot.sendRawLine("PONG "+rawMessage.split(" ")[1]);
    }


    public void die(IrcBot bot) {}

}
