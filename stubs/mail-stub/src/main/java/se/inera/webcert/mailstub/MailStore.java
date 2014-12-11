package se.inera.webcert.mailstub;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

/**
 * @author andreaskaltenbach
 */
@Component
public class MailStore {

    private static final long MAX_TIMEOUT = 5000;
    public static final long POLL_INTERVAL = 10L;
    public static final int MAX_POLLS = 100;
    private List<OutgoingMail> mails = new CopyOnWriteArrayList<>();
    private boolean doWait;

    public List<OutgoingMail> getMails() {
        return mails;
    }

    public void waitForMails(int count) {
        int loops = MAX_POLLS;
        while (mails.size() < count) {
            try {
                Thread.sleep(POLL_INTERVAL);
            } catch (InterruptedException e) {
                if (--loops == 0) {
                    break;
                }
            }
        }
    }

    public void waitToContinue() {
        synchronized (this) {
            if (doWait) {
                try {
                    this.wait(MAX_TIMEOUT);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void setWait(boolean doWait) {
        synchronized (this) {
            this.doWait = doWait;
            this.notifyAll();
        }
    }
}
