package edu.lsu.cct.distgc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessagesOvertake implements MessageQueue {

    List<Message> msgs = new ArrayList<>();
    HashMap<Integer, List<Message>> masterMailbox = new HashMap<>();

    @Override
    public void sendMessage(Message m) {
        msgs.add(m);
        if (m.isDone()) {
            return;
        }
        List<Message> mailbox = masterMailbox.get(m.recipient);
        if (mailbox == null) {
            mailbox = new ArrayList<>();
            mailbox.add(m);
            masterMailbox.put(m.recipient, mailbox);
        } else {
            mailbox.add(m);
        }
    }

    @Override
    public Message nextToRun() {
        if (msgs.isEmpty()) {
            return null;
        }
        int n = Message.RAND.nextInt(msgs.size());
        Message m = msgs.remove(n);
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
                    if (!toBeAdded.isDone()) {
                        mails.add(toBeAdded);
                        break;
                    }
                }
                numMails = mailbox.size();
            }
        }
        return mails;
    }

    @Override
    public int size() {
        return msgs.size();
    }

}
