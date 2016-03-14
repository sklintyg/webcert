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

package se.inera.intyg.webcert.notification_sender.certificatesender.services.mock;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.ws.WebServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.intyg.webcert.common.client.SendCertificateServiceClient;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v1.SendCertificateToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.v2.*;

/**
 * Created by eriklupander on 2015-06-03.
 */
public class MockSendCertificateServiceClientImpl implements SendCertificateServiceClient {

    private static final Logger LOG = LoggerFactory.getLogger(MockSendCertificateServiceClientImpl.class);

    public static final String FALLERAT_MEDDELANDE = "fallerat-meddelande";

    private AtomicInteger count = new AtomicInteger(0);

    private ConcurrentHashMap<String, AtomicInteger> attemptsPerMessage = new ConcurrentHashMap<>();

    private List<String> store = new CopyOnWriteArrayList<>();

    @Override
    public SendCertificateToRecipientResponseType sendCertificate(String intygsId, String personId, String recipient, String logicalAddress) {
        count.incrementAndGet();

        if (intygsId.startsWith(FALLERAT_MEDDELANDE)) {
            int attempts = increaseAttempts(intygsId);
            int numberOfRequestedFailedAttempts = Integer.parseInt(intygsId.substring(intygsId.length() - 1));
            LOG.debug("attempts: " + attempts);
            LOG.debug("numberOfRequestedFailedAttempts: " + numberOfRequestedFailedAttempts);
            if (attempts < numberOfRequestedFailedAttempts + 1) {
                throw new WebServiceException("Something went wrong");
            }
        }

        store.add(intygsId);

        return createResponse(ResultCodeType.OK, null);
    }

    private int increaseAttempts(String key) {
        AtomicInteger value = attemptsPerMessage.get(key);
        if (value == null) {
            value = attemptsPerMessage.putIfAbsent(key, new AtomicInteger(1));
        }
        if (value != null) {
            value.incrementAndGet();
        }
        return attemptsPerMessage.get(key).intValue();
    }


    public int getNumberOfReceivedMessages() {
        return count.get();
    }

    public int getNumberOfSentMessages() {
        return store.size();
    }

    private SendCertificateToRecipientResponseType createResponse(ResultCodeType resultCodeType, ErrorIdType errorType) {
        ResultType resultType = new ResultType();
        resultType.setResultCode(resultCodeType);
        if (errorType != null) {
            resultType.setErrorId(errorType);
        }
        SendCertificateToRecipientResponseType responseType = new SendCertificateToRecipientResponseType();

        responseType.setResult(resultType);
        return responseType;
    }

    public void reset() {
        count = new AtomicInteger(0);
        attemptsPerMessage = new ConcurrentHashMap<>();
        store = new CopyOnWriteArrayList<>();
    }

}
