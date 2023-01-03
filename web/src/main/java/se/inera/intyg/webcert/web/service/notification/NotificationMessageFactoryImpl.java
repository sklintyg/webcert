/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.notification;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;

@Component
public class NotificationMessageFactoryImpl implements NotificationMessageFactory {

    private static final List<HandelsekodEnum> USES_FRAGOR_OCH_SVAR = Arrays.asList(HandelsekodEnum.NYFRFM,
        HandelsekodEnum.NYSVFM, HandelsekodEnum.NYFRFV, HandelsekodEnum.HANFRFM,
        HandelsekodEnum.HANFRFV, HandelsekodEnum.MAKULE);

    @Autowired
    private FragorOchSvarCreator fragorOchSvarCreator;
    @Autowired
    private SendNotificationStrategy sendNotificationStrategy;
    @Autowired
    private ReferensService referenceService;
    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Override
    public NotificationMessage createNotificationMessage(Utkast utkast, HandelsekodEnum handelse, SchemaVersion version,
        String reference, Amneskod amne, LocalDate sistaSvarsDatum) {
        return createNotificationMessage(utkast.getIntygsId(), utkast.getIntygsTyp(), utkast.getEnhetsId(), utkast.getModel(),
            handelse, version, reference, amne, sistaSvarsDatum);
    }

    // CHECKSTYLE:OFF ParameterNumber
    @Override
    public NotificationMessage createNotificationMessage(String intygsId, String intygsTyp, String logiskAdress, String utkastJson,
        HandelsekodEnum handelse, SchemaVersion version, String reference, Amneskod amne, LocalDate sistaSvarsDatum) {

        LocalDateTime handelseTid = LocalDateTime.now();

        FragorOchSvar fragaSvar = null;
        ArendeCount skickadeFragor = null;
        ArendeCount mottagnaFragor = null;

        if (SchemaVersion.VERSION_3 == version) {
            Pair<ArendeCount, ArendeCount> arenden = Pair.of(ArendeCount.getEmpty(), ArendeCount.getEmpty());

            // Add a count of questions to the message
            if (USES_FRAGOR_OCH_SVAR.contains(handelse)) {
                arenden = fragorOchSvarCreator.createArenden(intygsId, intygsTyp);
            }

            skickadeFragor = arenden.getLeft();
            mottagnaFragor = arenden.getRight();

        } else {
            fragaSvar = FragorOchSvar.getEmpty();

            // Add a count of questions to the message
            if (USES_FRAGOR_OCH_SVAR.contains(handelse)) {
                fragaSvar = fragorOchSvarCreator.createFragorOchSvar(intygsId);
            }
        }

        return new NotificationMessage(intygsId, intygsTyp, handelseTid, handelse, logiskAdress, utkastJson,
            fragaSvar, skickadeFragor, mottagnaFragor, version, reference, amne, sistaSvarsDatum);
    }
    // CHECKSTYLE:ON ParameterNumber
    
    @Override
    public NotificationMessage createNotificationMessage(Handelse event, String draftJson)
        throws ModuleNotFoundException, IOException, ModuleException {
        final var moduleApi = moduleRegistry.getModuleApi(moduleRegistry.getModuleIdFromExternalId(event.getCertificateType()),
            event.getCertificateVersion());
        final var utlatande = moduleApi.getUtlatandeFromJson(draftJson);
        final var schemaVersion = sendNotificationStrategy.decideNotificationForIntyg(utlatande).orElse(SchemaVersion.VERSION_3);
        final var reference = referenceService.getReferensForIntygsId(event.getIntygsId());
        final var topicCode = event.getAmne() != null ? AmneskodCreator.create(event.getAmne().name(), event.getAmne().getDescription())
            : null;

        final var notificationMessage = createNotificationMessage(event.getIntygsId(), utlatande.getTyp(),
            event.getEnhetsId(), draftJson, event.getCode(), schemaVersion, reference, topicCode, event.getSistaDatumForSvar());

        notificationMessage.setHandelseTid(event.getTimestamp());
        return notificationMessage;
    }

}
