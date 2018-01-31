package edu.lsu.cct.distgc;

import java.util.List;

public interface MessageQueue extends Iterable<Message> {

    public void sendMessage(Message m);

    public Message nextToRun();

    public List<Message> nextRoundToRun();

    public Message getMessage(int nodeId, int msgId);

    public int size();
}
