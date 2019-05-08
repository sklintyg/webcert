/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.access;

import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.schemas.contract.Personnummer;

public interface CertificateAccessService {
    AccessResult allowToRead(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer);

    AccessResult allowToReplace(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer);

    AccessResult allowToRenew(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer);

    AccessResult allowToRenew(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer, boolean complement);

    AccessResult allowToPrint(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer, boolean isEmployer);

    AccessResult allowToInvalidate(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer);

    AccessResult allowToSend(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer);

    AccessResult allowToCreateQuestion(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer);

    AccessResult allowToAnswerComplementQuestion(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer, boolean newCertificate);

    AccessResult allowToAnswerAdminQuestion(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer);

    AccessResult allowToReadQuestions(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer);

    AccessResult allowToForwardQuestions(String intygsTyp, Vardenhet vardenhet, Personnummer personnummer);
}
