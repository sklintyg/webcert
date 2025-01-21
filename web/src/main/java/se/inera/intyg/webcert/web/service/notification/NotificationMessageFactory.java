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
package se.inera.intyg.webcert.web.service.notification;

import java.io.IOException;
import java.time.LocalDate;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;

public interface NotificationMessageFactory {

    /**
     * Creates a NotificationMessage instance from the supplied {@link Utkast} and supplied parameters.
     */
    NotificationMessage createNotificationMessage(Utkast utkast, HandelsekodEnum handelse, SchemaVersion version,
        String reference, Amneskod amne, LocalDate sistaSvarsDatum);

    /**
     * Creates a NotificationMessage instance where the fields from Utkast has been extracted into separate parameters.
     */
    // CHECKSTYLE:OFF ParameterNumber
    NotificationMessage createNotificationMessage(String intygsId, String intygsTyp, String logiskAdress, String utkastJson,
        HandelsekodEnum handelse, SchemaVersion version,
        String reference, Amneskod amne, LocalDate sistaSvarsDatum);

    NotificationMessage createNotificationMessage(Handelse event, String draftJson)
        throws ModuleNotFoundException, IOException, ModuleException;
    // CHECKSTYLE:ON ParameterNumber
}
