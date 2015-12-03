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
