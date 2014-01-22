package com.coreymatyas.irc.AlphaForceFramework;

public abstract interface CommandHandler {
    void executeCommand(IrcBot bot, String[] args, String rawMessage, String sender, String hostname, String channel);
    void die(IrcBot bot);
}
