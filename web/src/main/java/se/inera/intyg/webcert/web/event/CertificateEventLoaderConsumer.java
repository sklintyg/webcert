/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.event;

import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.event.model.CertificateEventFailedLoad;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventFailedLoadRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventProcessedRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

@Service
public class CertificateEventLoaderConsumer implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateEventLoaderConsumer.class);

    @Autowired
    CertificateEventService service;

    @Autowired
    UtkastRepository utkastRepository;

    @Autowired
    CertificateEventFailedLoadRepository failedLoadRepository;

    @Autowired
    CertificateEventProcessedRepository processedRepository;


    @Override
    @JmsListener(destination = "${certificateevent.loader.queueName}")
    public void onMessage(Message message) {
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage objMessage = (ObjectMessage) message;
                var list = (ArrayList<String>) objMessage.getObject();
                processIds(list);
            }
        } catch (Exception e) {
            LOG.error("Could not process certificate event loader message: {}", e.getMessage());
        }
    }

    public void processIds(List<String> certificateIdList) {
        List<String> failedCertificates = new ArrayList<>();

        certificateIdList.forEach(id -> {
            try {
                LOG.debug("Processing " + id);
                var byCertificateId = processedRepository.findById(id);
                if (byCertificateId.isPresent()) {
                    processId(id);
                }
            } catch (Exception e) {
                processFailedId(id, e, failedCertificates);
            }
        });

        if (failedCertificates.size() < certificateIdList.size()) {
            LOG.info("Certificate Event Loader successfully finished processing {} certificates out of batch with size {}",
                (certificateIdList.size() - failedCertificates.size()), certificateIdList.size());
        }

        if (failedCertificates.size() > 0) {
            LOG.warn("Certificate Event Loader failed during processing of {} certificates out of batch with size {}. These failed: {}",
                failedCertificates.size(), certificateIdList.size(), String.join(", ", failedCertificates));
        }
    }

    @Transactional
    public void processFailedId(String id, Exception e, List<String> failedCertificates) {
        addToFailedCertificatesTable(id, e);
        processedRepository.deleteById(id);
        failedCertificates.add(id);
    }

    @Transactional
    public void processId(String id) {
        service.getCertificateEvents(id);
        processedRepository.deleteById(id);
        LOG.debug("Finished processing " + id);
    }

    private void addToFailedCertificatesTable(String id, Exception e) {
        var certificateEventFailedLoad = new CertificateEventFailedLoad();
        certificateEventFailedLoad.setCertificateId(id);
        certificateEventFailedLoad.setException(e.toString());
        certificateEventFailedLoad.setTimestamp(LocalDateTime.now());
        failedLoadRepository.save(certificateEventFailedLoad);
    }
}
