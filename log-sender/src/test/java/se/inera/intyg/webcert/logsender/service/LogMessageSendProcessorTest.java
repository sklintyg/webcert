package se.inera.intyg.webcert.logsender.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.logmessages.ActivityType;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.logsender.client.LogSenderClient;
import se.inera.intyg.webcert.logsender.converter.LogTypeFactoryImpl;
import se.inera.intyg.webcert.logsender.exception.BatchValidationException;
import se.inera.intyg.webcert.logsender.exception.LoggtjanstExecutionException;
import se.inera.intyg.webcert.logsender.helper.TestDataHelper;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogResponseType;
import se.riv.ehr.log.store.v1.ResultType;
import se.riv.ehr.log.v1.ResultCodeType;

import javax.xml.ws.WebServiceException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2016-03-08.
 */
@RunWith(MockitoJUnitRunner.class)
public class LogMessageSendProcessorTest {

    @Mock
    private LogSenderClient logSenderClient;

    @Spy
    private LogTypeFactoryImpl logTypeFactory;

    @InjectMocks
    private LogMessageSendProcessor testee;

    private ObjectMapper objectMapper = new CustomObjectMapper();

    @Test
    public void testSendLogMessagesWhenAllOk() throws Exception {
        when(logSenderClient.sendLogMessage(anyList())).thenReturn(buildResponse(ResultCodeType.OK));
        testee.process(objectMapper.writeValueAsString(buildGroupedMessages()));
        verify(logSenderClient, times(1)).sendLogMessage(anyList());
    }

    @Test(expected = BatchValidationException.class)
    public void testSendLogMessagesThrowsPermanentExceptionWhenInvalidJsonIsSupplied() throws Exception {
        when(logSenderClient.sendLogMessage(anyList())).thenReturn(buildResponse(ResultCodeType.ERROR));
        testee.process(objectMapper.writeValueAsString(buildInvalidGroupedMessages()));
        verify(logSenderClient, times(1)).sendLogMessage(anyList());
    }


    @Test(expected = BatchValidationException.class)
    public void testSendLogMessagesThrowsBatchValidationExceptionWhenErrorOccured() throws Exception {
        when(logSenderClient.sendLogMessage(anyList())).thenReturn(buildResponse(ResultCodeType.ERROR));
        testee.process(objectMapper.writeValueAsString(buildGroupedMessages()));
        verify(logSenderClient, times(1)).sendLogMessage(anyList());
    }

    @Test(expected = BatchValidationException.class)
    public void testSendLogMessagesThrowsBatchValidationExceptionWhenValidationErrorOccured() throws Exception {
        when(logSenderClient.sendLogMessage(anyList())).thenReturn(buildResponse(ResultCodeType.VALIDATION_ERROR));
        testee.process(objectMapper.writeValueAsString(buildGroupedMessages()));
        verify(logSenderClient, times(1)).sendLogMessage(anyList());
    }

    @Test
    public void testSendLogMessagesDoesNothingWhenInfoIsReturned() throws Exception {
        when(logSenderClient.sendLogMessage(anyList())).thenReturn(buildResponse(ResultCodeType.INFO));
        testee.process(objectMapper.writeValueAsString(buildGroupedMessages()));
        verify(logSenderClient, times(1)).sendLogMessage(anyList());
    }

    @Test(expected = BatchValidationException.class)
    public void testSendLogMessagesThrowsBatchValidationExceptionWhenIllegalArgumentExceptionIsThrown() throws Exception {
        when(logSenderClient.sendLogMessage(anyList())).thenThrow(new IllegalArgumentException("illegal"));
        testee.process(objectMapper.writeValueAsString(buildGroupedMessages()));
        verify(logSenderClient, times(1)).sendLogMessage(anyList());
    }

    @Test(expected = TemporaryException.class)
    public void testSendLogMessagesThrowsTemporaryExceptionWhenLoggtjanstExecutionExceptionIsThrwn() throws Exception {
        when(logSenderClient.sendLogMessage(anyList())).thenThrow(new LoggtjanstExecutionException());
        testee.process(objectMapper.writeValueAsString(buildGroupedMessages()));
        verify(logSenderClient, times(1)).sendLogMessage(anyList());
    }

    @Test(expected = TemporaryException.class)
    public void testSendLogMessagesThrowsTemporaryExceptionWhenWebServiceExceptionIsThrwn() throws Exception {
        when(logSenderClient.sendLogMessage(anyList())).thenThrow(new WebServiceException());
        testee.process(objectMapper.writeValueAsString(buildGroupedMessages()));
        verify(logSenderClient, times(1)).sendLogMessage(anyList());
    }


    private StoreLogResponseType buildResponse(ResultCodeType resultCodeType) {
        StoreLogResponseType responseType = new StoreLogResponseType();
        ResultType resultType = new ResultType();
        resultType.setResultCode(resultCodeType);
        responseType.setResultType(resultType);
        return responseType;
    }

    private List<String> buildGroupedMessages() {
        String pdlLogMessage1 = TestDataHelper.buildBasePdlLogMessageAsJson(ActivityType.READ);
        String pdlLogMessage2 = TestDataHelper.buildBasePdlLogMessageAsJson(ActivityType.PRINT);
        return Arrays.asList(pdlLogMessage1, pdlLogMessage2);

    }

    private List<String> buildInvalidGroupedMessages() {
        String pdlLogMessage1 = TestDataHelper.buildBasePdlLogMessageAsJson(ActivityType.READ);
        String pdlLogMessage2 = TestDataHelper.buildBasePdlLogMessageAsJson(ActivityType.PRINT);
        return Arrays.asList(pdlLogMessage1, pdlLogMessage2, "this-is-not-json");

    }
}
