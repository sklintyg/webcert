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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventProcessedRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@Service
@EnableScheduling
public class CertificateEventLoaderServiceImpl implements CertificateEventLoaderService {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateEventLoaderServiceImpl.class);
    private static final String JOB_NAME = "CertificateEventLoader.run";
    private static final int NR_OF_BATCHES = 32;

    @Autowired
    MonitoringLogService monitoringLogService;

    @Autowired
    UtkastRepository utkastRepository;

    @Autowired
    CertificateEventProcessedRepository processedRepository;

    @Autowired
    @Qualifier("jmsCertificateEventLoaderTemplate")
    private JmsTemplate jmsTemplate;

    @Value("${certificateeventloader.batchsize:10000}")
    private Integer batchSize;

    @Value("${certificateevent.loader.queueName}")
    private String internalCertificateEventsLoaderQueue;


    @Override
    @Scheduled(cron = "${certificateeventloader.cron:-}")
    @SchedulerLock(name = JOB_NAME)
    public void loadIds() {
        if (countPendingMessages() == 0) {
            LOG.info("No IDs on queue: Starting certificate event loader run with batch size: " + batchSize + " and splitting into "
                + NR_OF_BATCHES + " batches.");
            var certificateIdList = getIdsForCertificatesWithoutEvents();
            LOG.debug("Putting ids on queue: " + String.join(",", certificateIdList));
            if (certificateIdList.size() > 0) {
                var size = certificateIdList.size() < NR_OF_BATCHES ? NR_OF_BATCHES : (certificateIdList.size() / NR_OF_BATCHES);
                chunked(certificateIdList.stream(), size).forEach(this::putIdsOnQueue);
            }
        }
    }

    private static <T> Stream<List<T>> chunked(Stream<T> stream, int chunkSize) {
        AtomicInteger index = new AtomicInteger(0);

        return stream.collect(Collectors.groupingBy(x -> index.getAndIncrement() / chunkSize))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue);
    }

    private int countPendingMessages() {
        Integer totalPendingMessages = this.jmsTemplate
            .browse(internalCertificateEventsLoaderQueue, (session, browser) -> Collections.list(browser.getEnumeration()).size());

        return totalPendingMessages == null ? 0 : totalPendingMessages;
    }

    @Transactional
    public void putIdsOnQueue(List<String> idList) {
        processedRepository.saveBatch(idList);
        var success = send(session -> session.createObjectMessage((ArrayList<String>) idList));
        if (!success) {
            throw new RuntimeException("Could not send message to queue");
        }
        LOG.debug("Put ids on queue: " + String.join(",", idList) + " : " + success);
    }

    private boolean send(final MessageCreator messageCreator) {
        try {
            jmsTemplate.send(internalCertificateEventsLoaderQueue, messageCreator);
            return true;
        } catch (JmsException e) {
            LOG.error("Failure sending ids to certificate event loader queue.", e);
            return false;
        }
    }

    private List<String> getIdsForCertificatesWithoutEvents() {
        return utkastRepository.findCertificatesWithoutEvents(batchSize);
    }
}
