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

package se.inera.intyg.webcert.web.integration;

import java.util.List;
import se.inera.intyg.infra.message.dto.MessageFromIT;

/**
 * Service to use when calling Intygstjanstens internal Rest APIs.
 */
public interface ITIntegrationService {

    /**
     * Get all messages for a certificate.
     * @param certificateId Id of certificate
     * @return  List of messages. If no messages exists for the certificate, then list will be empty.
     */
    List<MessageFromIT> findMessagesByCertificateId(String certificateId);
}
