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
package se.inera.intyg.webcert.web.service.facade.question.util;

import java.util.List;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.question.Complement;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;

public interface QuestionConverter {

    Question convert(ArendeDraft arendeDraft);

    Question convert(Arende arende);

    Question convert(Arende arende, Complement[] complements, CertificateRelation answeredByCertificate);

    Question convert(Arende arende, Complement[] complements, CertificateRelation answeredByCertificate, List<Arende> reminders);

    Question convert(Arende arende, Complement[] complements, CertificateRelation answeredByCertificate, Arende answer,
        List<Arende> reminders);

    Question convert(Arende arende, Complement[] complements, CertificateRelation answeredByCertificate, ArendeDraft answerDraft,
        List<Arende> reminders);
}
