package se.inera.intyg.webcert.logsender.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.webcert.logsender.exception.LoggtjanstExecutionException;
import se.riv.ehr.log.store.storelog.rivtabp21.v1.StoreLogResponderInterface;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogResponseType;
import se.riv.ehr.log.store.v1.ResultType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResultCodeType;

import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2016-03-08.
 */
@RunWith(MockitoJUnitRunner.class)
public class LogSenderClientImplTest {

    @Mock
    StoreLogResponderInterface storeLogResponderInterface;

    @InjectMocks
    private LogSenderClientImpl testee;

    @Test
    public void testSendOk() {
        when(storeLogResponderInterface.storeLog(anyString(), any(StoreLogRequestType.class))).thenReturn(buildOkResponse());
        StoreLogResponseType response = testee.sendLogMessage(buildLogEntries());
        assertNotNull(response);
        assertEquals(ResultCodeType.OK, response.getResultType().getResultCode());
    }

    @Test
    public void testSendWithEmptyLogEntriesListCausesNoSend() {
        StoreLogResponseType response = testee.sendLogMessage(new ArrayList<>());
        assertNotNull(response);
        assertEquals(ResultCodeType.INFO, response.getResultType().getResultCode());
        assertNotNull(response.getResultType().getResultText());
        verify(storeLogResponderInterface, times(0)).storeLog(anyString(), any(StoreLogRequestType.class));
    }

    @Test(expected = LoggtjanstExecutionException.class)
    public void testWebServiceExceptionCausesLoggtjanstExecutionException() {
        when(storeLogResponderInterface.storeLog(anyString(), any(StoreLogRequestType.class))).thenThrow(new WebServiceException("error"));
        testee.sendLogMessage(buildLogEntries());
    }

    private StoreLogResponseType buildOkResponse() {
        StoreLogResponseType resp = new StoreLogResponseType();
        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeType.OK);
        resp.setResultType(resultType);
        return resp;
    }

    private List<LogType> buildLogEntries() {
        List<LogType> logEntries = new ArrayList<>();
        logEntries.add(buildLogEntry());
        return logEntries;
    }

    private LogType buildLogEntry() {
        LogType logType = new LogType();
        return logType;
    }

}
