/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.logsender.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

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
    public void testSendError() {
        when(storeLogResponderInterface.storeLog(anyString(), any(StoreLogRequestType.class))).thenReturn(buildErrorResponse());
        StoreLogResponseType response = testee.sendLogMessage(buildLogEntries());
        assertNotNull(response);
        assertEquals(ResultCodeType.ERROR, response.getResultType().getResultCode());
    }

    @Test
    public void testSendWithNullListCausesNoSend() {
        StoreLogResponseType response = testee.sendLogMessage(null);
        assertNotNull(response);
        assertEquals(ResultCodeType.INFO, response.getResultType().getResultCode());
        assertNotNull(response.getResultType().getResultText());
        verify(storeLogResponderInterface, times(0)).storeLog(anyString(), any(StoreLogRequestType.class));
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

    private StoreLogResponseType buildErrorResponse() {
        StoreLogResponseType resp = new StoreLogResponseType();
        ResultType resultType = new ResultType();
        resultType.setResultCode(ResultCodeType.ERROR);
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
