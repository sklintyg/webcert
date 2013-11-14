package se.inera.logsender;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.riv.ehr.log.store.storelog.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponderInterface;
import se.riv.ehr.log.v1.LogType;

/**
 * @author andreaskaltenbach
 */
@Component
public class BlockingLogSender {

    private static final Logger LOG = LoggerFactory.getLogger(BlockingLogSender.class);

    @Value("${logsender.deliveryIntervalInSeconds}")
    private int deliveryIntervalInSeconds;

    @Value("${logsender.bulkSize}")
    private int bulkSize;

    @Autowired
    private StoreLogResponderInterface loggTjanstResponder;

    private CountDownLatch countDownLatch;

    private Collection<LogType> bucket = new HashSet<>();

    @PostConstruct
    public void checkConfiguration() {
        if (deliveryIntervalInSeconds == 0) {
            throw new IllegalStateException("'deliveryIntervalInSeconds' has to be greater than zero");
        }

        if (bulkSize == 0) {
            throw new IllegalStateException("'bulkSize' has to be greater than zero");
        }
    }

    @PostConstruct
    public void setupCountDownLatch() {

        countDownLatch = new CountDownLatch(bulkSize);
    }

    public void sendLogEntry(LogType logEntry) {



        boolean senderThread = false;

        synchronized (bucket) {

            bucket.add(logEntry);
            LOG.debug("Putting log entry {} in the bulk bucket. Bucket size: {}", logEntry, bucket.size());

            // if bucket is filled, we send it to the loggtjänst
            if (bucket.size() == bulkSize) {
                senderThread = true;
                sendLogEntriesToLoggtjanst();
                LOG.debug("Log entry {} has been sent to loggtjanst successfully", logEntry);
                countDownLatch.countDown();
            }

        }

        if (!senderThread) {
            countDownLatch.countDown();
            try {
                countDownLatch.await();
                LOG.debug("Log entry {} has been sent to loggtjanst successfully", logEntry);
            } catch (InterruptedException e) {
                LOG.error("Thread got interrupted", e);
                throw new RuntimeException("Failed to trigger bulk sending to loggtjanst", e);
            }
        } else {
            setupCountDownLatch();
        }
    }

    private void decreaseCountDownLatch() {

    }

    public void sendLogEntriesToLoggtjanst() {
        // TODO - call WS

        StoreLogRequestType request = new StoreLogRequestType();
        request.getLog().addAll(bucket);
        loggTjanstResponder.storeLog(null, request);

        bucket.clear();

    }


}
