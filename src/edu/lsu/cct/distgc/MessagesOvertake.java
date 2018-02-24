package edu.lsu.cct.distgc;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class MessagesOvertake implements MessageQueue {

    List<Message> msgs;
    Map<Integer, List<Message>> masterMailbox;

    public MessagesOvertake(boolean congest) {
        if(congest)
            masterMailbox = new HashMap<>();
        else
            msgs = new ArrayList<>();
    }

    @Override
    public Iterator<Message> iterator() {
        if(msgs == null) {
            List<Message> list = new ArrayList<>();
            for(List<Message> mailbox : masterMailbox.values()) {
                list.addAll(mailbox);
            }
            return list.iterator();
        } else {
            return msgs.iterator();
        }
    }

    final static int ADV_PRIORITY = Integer.parseInt(Props.get("adv-priority"));

    static void addRandom(List<Message> msgs,Message m) {
        msgs.add(m);
        int n = msgs.size();
        if(ADV_PRIORITY > 0 && m instanceof HasAdversary) {
           if(n > ADV_PRIORITY) n = ADV_PRIORITY;
        }
        int n1 = msgs.size() - Message.RAND.nextInt(n) - 1;
        int n2 = msgs.size() - 1;
        Message m1 = msgs.get(n1);
        Message m2 = msgs.get(n2);
        msgs.set(n1,m2);
        msgs.set(n2,m1);
    }

    @Override
    public void sendMessage(Message m) {
        if(msgs != null) {
            addRandom(msgs,m);
        } else {
            List<Message> mailbox = masterMailbox.get(m.recipient);
            if (mailbox == null) {
                mailbox = new ArrayList<>();
                masterMailbox.put(m.recipient, mailbox);
            }
            addRandom(mailbox,m);
        }
    }

    @Override
    public Message nextToRun() {
        if (msgs.isEmpty()) {
            return null;
        }
        return msgs.remove(msgs.size()-1);
    }

    @Override
    public List<Message> nextRoundToRun() {
        List<Message> mails = new ArrayList<>();
        Here.log();
        for (Integer nodeId : masterMailbox.keySet()) {
            List<Message> mailbox = masterMailbox.get(nodeId);
            assert mailbox != null : "Mailbox cannot be null";
            int numMails = mailbox.size();
            while (mailbox.size() > 0) {
                if (numMails > 0) {
                    Message toBeAdded = mailbox.remove(numMails-1);
                    assert !toBeAdded.isDone();
                    mails.add(toBeAdded);
                }
                numMails = mailbox.size();
            }
        }
        Here.log(mails);
        return mails;
    }

    @Override
    public Message getMessage(int nodeId, int msgId) {
    	List<Message> mailbox = masterMailbox.get(nodeId);
        assert mailbox != null : "Mailbox cannot be null";
        Iterator<Message> miter = mailbox.iterator();
        while(miter.hasNext()) {
            Message msg = miter.next();
        	if (msg.msg_id == msgId) {
                miter.remove();
        		return msg;
        	}
        }
        return null;
    }


    @Override
    public int size() {
        if(msgs != null) {
            return msgs.size();
        } else {
            int size = 0;
            for(List<Message> mailbox : masterMailbox.values()) {
                size += mailbox.size();
            }
            return size;
        }
    }

}
