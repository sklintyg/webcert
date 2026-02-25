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
package se.inera.intyg.webcert.web.service.erase;

import jakarta.transaction.Transactional;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeDraftRepository;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.event.repository.CertificateEventRepository;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.integreradenhet.repository.IntegreradEnhetRepository;
import se.inera.intyg.webcert.persistence.legacy.repository.MigreratMedcertIntygRepository;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;
import se.inera.intyg.webcert.persistence.privatlakaravtal.repository.GodkantAvtalRepository;
import se.inera.intyg.webcert.persistence.referens.repository.ReferensRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.PagaendeSigneringRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;

@Service
public class EraseServiceImpl implements EraseService {

    private static final Logger LOG = LoggerFactory.getLogger(EraseServiceImpl.class);

    private final UtkastRepository utkastRepository;
    private final FragaSvarRepository fragaSvarRepository;
    private final ArendeRepository arendeRepository;
    private final ArendeDraftRepository arendeDraftRepository;
    private final HandelseRepository handelseRepository;
    private final NotificationRedeliveryRepository notificationRedeliveryRepository;
    private final PagaendeSigneringRepository pagaendeSigneringRepository;
    private final CertificateEventRepository certificateEventRepository;
    private final ReferensRepository referensRepository;
    private final IntegreradEnhetRepository integreradEnhetRepository;
    private final GodkantAvtalRepository godkantAvtalRepository;
    private final MigreratMedcertIntygRepository migreratMedcertIntygRepository;


    //CHECKSTYLE:OFF ParameterNumber
    public EraseServiceImpl(UtkastRepository utkastRepository, FragaSvarRepository fragaSvarRepository, ArendeRepository arendeRepository,
        IntegreradEnhetRepository integreradEnhetRepository, ArendeDraftRepository arendeDraftRepository,
        HandelseRepository handelseRepository, NotificationRedeliveryRepository notificationRedeliveryRepository,
        PagaendeSigneringRepository pagaendeSigneringRepository,
        MigreratMedcertIntygRepository migreratMedcertIntygRepository,
        CertificateEventRepository certificateEventRepository,
        ReferensRepository referensRepository, GodkantAvtalRepository godkantAvtalRepository) {
        this.utkastRepository = utkastRepository;
        this.fragaSvarRepository = fragaSvarRepository;
        this.arendeRepository = arendeRepository;
        this.integreradEnhetRepository = integreradEnhetRepository;
        this.arendeDraftRepository = arendeDraftRepository;
        this.handelseRepository = handelseRepository;
        this.notificationRedeliveryRepository = notificationRedeliveryRepository;
        this.pagaendeSigneringRepository = pagaendeSigneringRepository;
        this.migreratMedcertIntygRepository = migreratMedcertIntygRepository;
        this.certificateEventRepository = certificateEventRepository;
        this.referensRepository = referensRepository;
        this.godkantAvtalRepository = godkantAvtalRepository;
    } //CHECKSTYLE:ON ParameterNumber

    @Override
    @Transactional
    public void eraseCertificates(String careProviderId, int erasePageSize) {
        Page<String> certificateIdPage = Page.empty();
        final var erasePageable = PageRequest.of(0, erasePageSize, Sort.by(Direction.ASC, "skapad", "intygsId"));
        int erasedArendeTotal = 0;
        int erasedFragaSvarTotal = 0;
        int erasedHandelseTotal = 0;
        int erasedCertificateEventsTotal = 0;
        int erasedCertificatesTotal = 0;
        int erasedCertificates = 0;

        try {
            eraseIntegreradeEnheter(careProviderId);
            eraseGodkantAvtal(careProviderId);

            do {
                erasedCertificates = 0;
                certificateIdPage = utkastRepository.findCertificateIdsForCareProvider(careProviderId, erasePageable);
                final var certificateIds = certificateIdPage.getContent();

                if (certificateIds.isEmpty()) {
                    break;
                }

                LOG.info("Starting batch erasure of {} certificates for care provider {}.", certificateIds.size(), careProviderId);

                erasePagaendeSignering(certificateIds, careProviderId);
                eraseNotificationRedeliveries(certificateIds, careProviderId);
                eraseReferens(certificateIds, careProviderId);
                eraseMigratedMedcertCertificates(certificateIds, careProviderId);
                erasedFragaSvarTotal += eraseFragaSvar(certificateIds, careProviderId);
                erasedArendeTotal += eraseArenden(certificateIds, careProviderId);
                erasedHandelseTotal += eraseHandelser(certificateIds, careProviderId);
                erasedCertificateEventsTotal += eraseCertificateEvents(certificateIds, careProviderId);
                erasedCertificates = eraseCertificates(certificateIds, careProviderId);
                erasedCertificatesTotal += erasedCertificates;

                LOG.info("Completed batch erasure of {} certificates for care provider {}. Certificates remaining: {}.",
                    certificateIds.size(), careProviderId, certificateIdPage.getTotalElements() - erasedCertificates);

            } while (certificateIdPage.hasNext());

            final var eventIds = handelseRepository.findByVardgivarId(careProviderId).stream()
                .map(Handelse::getId)
                .toList();

            notificationRedeliveryRepository.eraseRedeliveriesForEventIds(eventIds);
            erasedHandelseTotal += handelseRepository.deleteHandelseByVardgivarId(careProviderId);

            LOG.info("Successfully completed erasure of certificates for care provider {}. Total number of erased Utkast: {}, "
                    + "Arende: {}, FragaSvar: {}, Handelse: {}, CertificateEvent: {}.", careProviderId, erasedCertificatesTotal,
                erasedArendeTotal, erasedFragaSvarTotal, erasedHandelseTotal, erasedCertificateEventsTotal);

        } catch (Exception e) {
            LOG.error("Error erasing certificates for care provider {}. Number of erased Utkast: {}, Arende: {}, FragaSvar: {}, "
                    + "Handelse: {}, CertificateEvent: {}. Utkast remaining: {}.", careProviderId, erasedCertificatesTotal,
                erasedArendeTotal, erasedFragaSvarTotal, erasedHandelseTotal, erasedCertificateEventsTotal,
                certificateIdPage.getTotalElements() - erasedCertificates, e);
            throw e;
        }
    }

    private void eraseIntegreradeEnheter(String careProviderId) {
        final var erasedIntegratedUnitsCount = integreradEnhetRepository.eraseIntegratedUnitsByCareProviderId(careProviderId);
        LOG.debug("Erased {} IntegreradEnhet for care provider {}.", erasedIntegratedUnitsCount, careProviderId);
    }

    private void eraseGodkantAvtal(String careProviderId) {
        final var erasedGodkantAvtalCount = godkantAvtalRepository.eraseGodkantAvtalByCareProviderId(careProviderId);
        LOG.debug("Erased {} GodkantAvtal for care provider {}.", erasedGodkantAvtalCount, careProviderId);
    }

    private void erasePagaendeSignering(List<String> certificateIds, String careProviderId) {
        final var erasedOngouingSignCount = pagaendeSigneringRepository.erasePagaendeSigneringByCertificateIds(certificateIds);
        LOG.debug("Erased {} PagaendeSignering for care provider {}.", erasedOngouingSignCount, careProviderId);
    }

    private void eraseNotificationRedeliveries(List<String> certificateIds, String careProviderId) {
        int erasedRedeliveryCount = 0;
        final var eventIds = handelseRepository.findHandelseIdsByCertificateIds(certificateIds);
        if (!eventIds.isEmpty()) {
            erasedRedeliveryCount = notificationRedeliveryRepository.eraseRedeliveriesForEventIds(eventIds);
        }
        LOG.debug("Erased {} NotificationRedeliveries for care provider {}.", erasedRedeliveryCount, careProviderId);
    }

    private void eraseReferens(List<String> certificateIds, String careProviderId) {
        final var erasedReferensCount = referensRepository.eraseReferenserByCertificateIds(certificateIds);
        LOG.debug("Erased {} Referens for care provider {}.", erasedReferensCount, careProviderId);
    }

    private void eraseMigratedMedcertCertificates(List<String> certificateIds, String careProviderId) {
        final var erasedMigratedMedcertCount = migreratMedcertIntygRepository.eraseMedcertCertificatesByCertificateIds(certificateIds);
        LOG.debug("Erased {} MigreratMedcertIntyg for care provider {}.", erasedMigratedMedcertCount, careProviderId);
    }

    private int eraseFragaSvar(List<String> certificateIds, String careProviderId) {
        final var erasedFragaSvarCount = fragaSvarRepository.eraseFragaSvarByCertificateIds(certificateIds);
        LOG.debug("Erased {} FragaSvar for care provider {}.", erasedFragaSvarCount, careProviderId);
        return erasedFragaSvarCount;
    }

    private int eraseArenden(List<String> certificateIds, String careProviderId) {
        final var erasedArendeDraftsCount = arendeDraftRepository.eraseArendeDraftsByCertificateIds(certificateIds);
        LOG.debug("Erased {} ArendeDrafts for care provider {}.", erasedArendeDraftsCount, careProviderId);

        final var erasedArendenCount = arendeRepository.eraseArendenByCertificateIds(certificateIds);
        LOG.debug("Erased {} Arende for care provider {}.", erasedArendenCount, careProviderId);
        return erasedArendenCount;
    }

    private int eraseHandelser(List<String> certificateIds, String careProviderId) {
        final var erasedHandelseCount = handelseRepository.eraseHandelseByCertificateIds(certificateIds);
        LOG.debug("Erased {} Handelse for care provider {}.", erasedHandelseCount, careProviderId);
        return erasedHandelseCount;
    }

    private int eraseCertificateEvents(List<String> certificateIds, String careProviderId) {
        final var erasedCertificateEventCount = certificateEventRepository.eraseCertificateEventsByCertificateIds(certificateIds);
        LOG.debug("Erased {} CertificateEvents for care provider {}.", erasedCertificateEventCount, careProviderId);
        return erasedCertificateEventCount;
    }

    private int eraseCertificates(List<String> certificateIds, String careProviderId) {
        final var erasedCertificatesCount = utkastRepository.eraseCertificatesByCertificateIds(certificateIds);
        LOG.debug("Erased {} Utkast for care provider {}.", erasedCertificatesCount, careProviderId);
        return erasedCertificatesCount;
    }
}