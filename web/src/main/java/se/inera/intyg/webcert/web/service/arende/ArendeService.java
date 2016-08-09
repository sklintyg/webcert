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

package se.inera.intyg.webcert.web.service.arende;

import java.util.*;

import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.intyg.webcert.web.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeConversationView;

public interface ArendeService {

    /**
     * Validates and decorates incoming arende with additional information.
     */
    Arende processIncomingMessage(Arende arende) throws WebCertServiceException;

    ArendeConversationView createMessage(String intygId, ArendeAmne amne, String rubrik, String meddelande) throws WebCertServiceException;

    ArendeConversationView answer(String svarPaMeddelandeId, String meddelande) throws WebCertServiceException;

    ArendeConversationView setForwarded(String meddelandeId, boolean vidarebefordrad);

    ArendeConversationView openArendeAsUnhandled(String meddelandeId);

    /**
     * List names of signing doctors for units where the webcert user is logged in.
     */
    List<Lakare> listSignedByForUnits(String enhetsId) throws WebCertServiceException;

    List<ArendeConversationView> getArenden(String intygsId);

    QueryFragaSvarResponse filterArende(QueryFragaSvarParameter filterParameters);

    /**
     * Close Arende and set status 'CLOSED'.
     */
    ArendeConversationView closeArendeAsHandled(String meddelandeId);

    Arende getArende(String meddelandeId);

    Map<String, Long> getNbrOfUnhandledArendenForCareUnits(List<String> allUnitIds, Set<String> intygsTyper);
}
