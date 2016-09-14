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

package se.inera.intyg.webcert.specifications.spec

import se.inera.intyg.webcert.mailstub.OutgoingMail
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

class MailHarSkickats extends RestClientFixture {

    def avsändare
    def mottagare
    def ämne
    def text

    boolean resultat

    boolean resultat() {
        resultat
    }

    def reset() {
        avsändare = ".*"
        mottagare = ".*"
        ämne = ".*"
        text = ".*"
    }

    def execute() {
        sleep(1000)
        def restClient = createRestClient("${baseUrl}services/")
        List<OutgoingMail> sentMails = restClient.get(path: 'mail-stub/mails').data
        if (sentMails == null || sentMails.isEmpty()) {
            throw new RuntimeException("Mail - tom")
        }
        resultat = sentMails.any {
            it.from.matches(avsändare) && it.recipients.any { it.matches(mottagare) } && it.subject.matches(ämne) && it.body.matches(text)
        }
    }

}
