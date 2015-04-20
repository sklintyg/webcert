package se.inera.webcert.loggtjanststub;

import org.springframework.beans.factory.annotation.Autowired;
import se.riv.ehr.log.store.storelog.rivtabp21.v1.StoreLogResponderInterface;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogResponseType;
import se.riv.ehr.log.store.v1.ResultType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResultCodeType;

import java.util.concurrent.CopyOnWriteArrayList;


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
        result.setResultText("Done");
        response.setResultType(result);
        return response;
    }
}
