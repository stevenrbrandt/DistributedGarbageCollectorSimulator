package edu.lsu.cct.distgc;

import java.util.List;

public interface MessageQueue {

    public void sendMessage(Message m);

    public Message nextToRun();

    public List<Message> nextRoundToRun();

    public int size();
}
