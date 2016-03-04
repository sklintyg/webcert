/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import se.inera.intyg.webcert.logsender.exception.LoggtjanstExecutionException;
import se.riv.ehr.log.store.storelog.rivtabp21.v1.StoreLogResponderInterface;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelogresponder.v1.StoreLogResponseType;
import se.riv.ehr.log.v1.LogType;

import javax.xml.ws.WebServiceException;
import java.util.List;

/**
 * Responsible for sending a list of {@link LogType} over the {@link StoreLogResponderInterface}.
 *
 * Typically, StoreLogResponderInterface is stubbed for dev and test, NTjP connected service used for demo, qa and prod.
 *
 * Created by eriklupander on 2016-02-29.
 */
public class LogSenderClientImpl implements LogSenderClient {

    @Value("${loggtjanst.logicalAddress}")
    private String logicalAddress;

    @Autowired
    private StoreLogResponderInterface storeLogClient;

    @Override
    public StoreLogResponseType sendLogMessage(List<LogType> logEntries) {

        StoreLogRequestType request = new StoreLogRequestType();
        request.getLog().addAll(logEntries);

        try {
            StoreLogResponseType response = storeLogClient.storeLog(logicalAddress, request);
            return response;
        } catch (WebServiceException e) {
            throw new LoggtjanstExecutionException(e);
        }
    }
}
