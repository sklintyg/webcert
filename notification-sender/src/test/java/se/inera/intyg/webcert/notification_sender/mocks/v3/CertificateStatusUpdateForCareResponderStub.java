/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.mocks.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.intyg.webcert.notification_sender.mocks.NotificationStubEntry;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

public class CertificateStatusUpdateForCareResponderStub implements CertificateStatusUpdateForCareResponderInterface {

    public static final String FALLERAT_MEDDELANDE = "fallerat-meddelande-";
    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateForCareResponderStub.class);
    private ConcurrentHashMap<String, AtomicInteger> attemptsPerMessage = new ConcurrentHashMap<>();
    private List<CertificateStatusUpdateForCareType> store = new CopyOnWriteArrayList<>();

    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public CertificateStatusUpdateForCareResponseType certificateStatusUpdateForCare(String logicalAddress,
            CertificateStatusUpdateForCareType request) {

        counter.incrementAndGet();

        String utlatandeId = getUtlatandeId(request);

        LOG.debug("utlatandeId: " + utlatandeId);
        LOG.debug("numberOfReceivedMessages: " + getNumberOfReceivedMessages());

        if (utlatandeId.startsWith(FALLERAT_MEDDELANDE)) {
            int attempts = increaseAttempts(utlatandeId);
            int numberOfRequestedFailedAttempts = Integer.parseInt(utlatandeId.substring(utlatandeId.length() - 1));
            LOG.debug("attempts: " + attempts);
            LOG.debug("numberOfRequestedFailedAttempts: " + numberOfRequestedFailedAttempts);
            if (attempts < numberOfRequestedFailedAttempts + 1) {
                throw new RuntimeException("Something went wrong");
            }
        }

        store.add(request);

        CertificateStatusUpdateForCareResponseType response = new CertificateStatusUpdateForCareResponseType();

        ResultType result = new ResultType();
        result.setResultCode(ResultCodeType.OK);
        response.setResult(result);
        LOG.debug("Request set to 'OK'");
        return response;
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

    private String getUtlatandeId(CertificateStatusUpdateForCareType request) {
        return request.getIntyg().getIntygsId().getExtension();
    }

    public int getNumberOfReceivedMessages() {
        return counter.get();
    }

    public int getNumberOfSentMessages() {
        return store.size();
    }

    public List<String> getIntygsIdsInOrder() {
        List<String> returnList = new ArrayList<>();
        for (CertificateStatusUpdateForCareType request : store) {
            returnList.add(getUtlatandeId(request));
        }
        return returnList;
    }

    public void reset() {
        counter = new AtomicInteger(0);
        attemptsPerMessage = new ConcurrentHashMap<>();
        store = new CopyOnWriteArrayList<>();
    }

    public List<NotificationStubEntry> getNotificationMessages() {
        List<NotificationStubEntry> returnList = new ArrayList<>();
        for (CertificateStatusUpdateForCareType request : store) {
            returnList.add(new NotificationStubEntry(request.getIntyg().getIntygsId().getExtension(),
                    request.getHandelse().getHandelsekod().getCode(), request.getHandelse().getTidpunkt()));
        }
        return returnList;
    }
}
