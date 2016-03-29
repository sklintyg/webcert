package se.inera.intyg.webcert.logsender.client.mock;

import se.inera.intyg.common.logmessages.ActivityType;
import se.riv.ehr.log.store.storelog.rivtabp21.v1.StoreLogResponderInterface;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogResponseType;
import se.riv.ehr.log.store.v1.ResultType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResultCodeType;

import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by eriklupander on 2016-02-29.
 */
public class MockLogSenderClientClientImpl implements StoreLogResponderInterface {

    private AtomicInteger count = new AtomicInteger(0);

    private ConcurrentHashMap<String, AtomicInteger> attemptsPerMessage = new ConcurrentHashMap<>();
    private List<String> store = new CopyOnWriteArrayList<>();

    @Override
    public StoreLogResponseType storeLog(String logicalAddress, StoreLogRequestType storeLogRequestType) {
        count.incrementAndGet();

        StoreLogResponseType resp = new StoreLogResponseType();
        ResultType resultType = new ResultType();
        if (storeLogRequestType.getLog().size() == 0) {
            resultType.setResultCode(ResultCodeType.INFO);
            resultType.setResultText("No log messages to store, doing nothing...");
            resp.setResultType(resultType);
            return resp;
        }

        increaseAttemptsPerMessage(storeLogRequestType);

        // Use the ActivityType.EMERGENCY_ACCESS to fake failures that should trigger a resend.
        if (storeLogRequestType.getLog().get(0).getActivity().getActivityType().equals(ActivityType.EMERGENCY_ACCESS.getType())) {
            throw new WebServiceException("This is an expected error since we got the EMERGENCY_ACCESS type");
        }

        // Use mechanism to trigger VALIDATION_ERROR
        for (LogType logType : storeLogRequestType.getLog()) {
            if (logType.getSystem().getSystemId().equals("invalid")) {
                resultType.setResultCode(ResultCodeType.VALIDATION_ERROR);
                resp.setResultType(resultType);
                return resp;
            }
        }

        resultType.setResultCode(ResultCodeType.OK);
        resp.setResultType(resultType);

        store.add(storeLogRequestType.getLog().get(0).getLogId());

        return resp;
    }

    private void increaseAttemptsPerMessage(StoreLogRequestType storeLogRequestType) {
        String key = storeLogRequestType.getLog().get(0).getLogId();
        if (!attemptsPerMessage.containsKey(key)) {
              attemptsPerMessage.put(key, new AtomicInteger(1));
        } else {
            attemptsPerMessage.get(key).incrementAndGet();
        }
    }

    public int getNumberOfReceivedMessages() {
        return count.get();
    }

    public int getNumberOfSentMessages() {
        return store.size();
    }

    public void reset() {
        count = new AtomicInteger(0);
        attemptsPerMessage = new ConcurrentHashMap<>();
        store = new CopyOnWriteArrayList<>();
    }
}
