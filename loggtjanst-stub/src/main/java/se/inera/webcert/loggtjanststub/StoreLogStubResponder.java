package se.inera.webcert.loggtjanststub;

import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import se.riv.ehr.log.store.storelog.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponderInterface;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponseType;
import se.riv.ehr.log.store.v1.ResultType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResultCodeType;

/**
 * @author andreaskaltenbach
 */
public class StoreLogStubResponder implements StoreLogResponderInterface {

    @Autowired
    private CopyOnWriteArrayList<LogType> logEntries;

    @Override
    public StoreLogResponseType storeLog(String logicalAddress, StoreLogRequestType request) {


        logEntries.addAll(request.getLog());

        StoreLogResponseType response = new StoreLogResponseType();

        ResultType result = new ResultType();
        result.setResultCode(ResultCodeType.OK);
        response.setResultType(result);
        return response;
    }
}
