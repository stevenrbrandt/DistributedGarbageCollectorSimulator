package edu.lsu.cct.distgc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

public class MessagesOvertake implements MessageQueue {

    List<Message> msgs = new ArrayList<>();
    HashMap<Integer, List<Message>> masterMailbox = new HashMap<>();

    public MessagesOvertake(boolean CONGEST_mode) {}

    @Override
    public Iterator<Message> iterator() {
        return msgs.iterator();
    }

    @Override
    public void sendMessage(Message m) {
        msgs.add(m);
        List<Message> mailbox = masterMailbox.get(m.recipient);
        if (mailbox == null) {
            mailbox = new ArrayList<>();
            mailbox.add(m);
            masterMailbox.put(m.recipient, mailbox);
        } else {
            mailbox.add(m);
        }
    }

    void removeMsg(Message m) {
        msgs.remove(m);
        masterMailbox.get(m.recipient).remove(m);
    }

    @Override
    public Message nextToRun() {
        if (msgs.isEmpty()) {
            return null;
        }
        int n = Message.RAND.nextInt(msgs.size());
        Message m = msgs.get(n);
        removeMsg(m);
        return m;
    }

    @Override
    public List<Message> nextRoundToRun() {
        List<Message> mails = new ArrayList<>();
        for (Integer nodeId : masterMailbox.keySet()) {
            List<Message> mailbox = masterMailbox.get(nodeId);
            assert mailbox != null : "Mailbox cannot be null";
            int numMails = mailbox.size();
            while (mailbox.size() > 0) {
                if (numMails > 0) {
                    int randomNum = Message.RAND.nextInt(0, numMails);
                    Message toBeAdded = mailbox.remove(randomNum);
                    assert !toBeAdded.isDone();
                    mails.add(toBeAdded);
                    break;
                }
            }
        }
        return mails;
    }

    @Override
    public Message getMessage(int nodeId, int msgId) {
    	List<Message> mailbox = masterMailbox.get(nodeId);
        assert mailbox != null : "Mailbox cannot be null";
        for (Message msg:mailbox) {
        	if (msg.msg_id == msgId) {
                removeMsg(msg);
        		return msg;
        	}
        }
        return null;
    }


    @Override
    public int size() {
        return msgs.size();
    }

}
