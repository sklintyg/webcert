/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import se.inera.intyg.webcert.persistence.event.model.CertificateEventFailedLoad;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventFailedLoadRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

@EnableScheduling
public class CertificateEventLoaderServiceImpl implements CertificateEventLoaderService {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateEventLoaderServiceImpl.class);
    private static final String JOB_NAME = "CertificateEventLoader.run";

    @Autowired
    CertificateEventService service;

    @Autowired
    MonitoringLogService monitoringLogService;

    @Autowired
    UtkastRepository utkastRepository;

    @Autowired
    CertificateEventFailedLoadRepository failedLoadRepository;

    @Value("${certificateeventloader.batchsize:10000}")
    private Integer batchSize;

    @Override
    @Scheduled(cron = "${certificateeventloader.cron:-}")
    @SchedulerLock(name = JOB_NAME)
    public void run() {
        LOG.info("Starting certificate event loader run with batch size: " + batchSize);
        var certificateIdList = getIdsForCertificatesWithoutEvents();
        List<String> failedCertificates = new ArrayList<>();

        certificateIdList.forEach(id -> {
            try {
                service.getCertificateEvents(id);
            } catch (Exception e) {
                addToFailedCertificatesTable(id, e);
                failedCertificates.add(id);
            }
        });

        if (failedCertificates.size() < certificateIdList.size()) {
            monitoringLogService.logSuccessfulCertificateEventLoaderBatch(certificateIdList, batchSize);
        }

        if (failedCertificates.size() > 0) {
            monitoringLogService.logFailedCertificateEventLoaderBatch(failedCertificates, batchSize);
        }
    }

    private void addToFailedCertificatesTable(String id, Exception e) {
        var certificateEventFailedLoad = new CertificateEventFailedLoad();
        certificateEventFailedLoad.setCertificateId(id);
        certificateEventFailedLoad.setException(e.toString());
        certificateEventFailedLoad.setTimestamp(LocalDateTime.now());
        failedLoadRepository.save(new CertificateEventFailedLoad());
    }

    private List<String> getIdsForCertificatesWithoutEvents() {
        return utkastRepository.findCertificatesWithoutEvents(PageRequest.of(0, batchSize));
    }
}
