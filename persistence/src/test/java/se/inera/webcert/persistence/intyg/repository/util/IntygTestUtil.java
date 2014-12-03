package se.inera.webcert.persistence.intyg.repository.util;

import org.joda.time.LocalDateTime;

import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.Signatur;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;

import java.util.UUID;

public class IntygTestUtil {

    public static final String ENHET_1_ID = "ENHET_1_ID";
    public static final String ENHET_2_ID = "ENHET_2_ID";
    public static final String ENHET_3_ID = "ENHET_3_ID";

    public static final String HOS_PERSON1_ID = "SE123344332";
    public static final String HOS_PERSON1_NAMN = "Dr. Börje Dengroth";

    public static final String HOS_PERSON2_ID = "SE123493729";
    public static final String HOS_PERSON2_NAMN = "Dr. Henry Jekyl";

    public static final String HOS_PERSON3_ID = "SE123484628";
    public static final String HOS_PERSON3_NAMN = "Dr. Johan Steen";

    public static final String PERSON_NUMMER = "19121212-1212";
    public static final String PERSON_FORNAMN = "Tolvan";
    public static final String PERSON_MELLANNAMN = "Svensson";
    public static final String PERSON_EFTERNAMN = "Tolvansson";

    public static final String INTYGSTYP_FK7263 = "FK7263";

    public static final String MODEL = "This is the JSON model of this Intyg "
            + "with some interesting scandinavian characters like Å, Ä and ö added";

    public static Intyg buildIntyg(String enhetsId) {
        return buildIntyg(enhetsId, IntygsStatus.DRAFT_INCOMPLETE, INTYGSTYP_FK7263, HOS_PERSON1_ID, HOS_PERSON1_NAMN,
                PERSON_NUMMER, PERSON_FORNAMN, PERSON_MELLANNAMN, PERSON_EFTERNAMN, MODEL, null);
    }

    public static Intyg buildIntyg(String enhetsId, IntygsStatus status) {
        return buildIntyg(enhetsId, status, INTYGSTYP_FK7263, HOS_PERSON1_ID, HOS_PERSON1_NAMN, PERSON_NUMMER,
                PERSON_FORNAMN, PERSON_MELLANNAMN, PERSON_EFTERNAMN, MODEL, null);
    }

    public static Intyg buildIntyg(String intygsId, String enhetsId, IntygsStatus status) {
        return buildIntyg(intygsId, enhetsId, status, INTYGSTYP_FK7263, HOS_PERSON1_ID, HOS_PERSON1_NAMN, PERSON_NUMMER,
                PERSON_FORNAMN, PERSON_MELLANNAMN, PERSON_EFTERNAMN, MODEL, null);
    }

    public static Intyg buildIntyg(String enhetsId, String hoSPersonId, String hoSPersonNamn, IntygsStatus status,
            String sparadStr) {
        LocalDateTime sparad = LocalDateTime.parse(sparadStr);
        return buildIntyg(enhetsId, status, INTYGSTYP_FK7263, hoSPersonId, hoSPersonNamn, PERSON_NUMMER,
                PERSON_FORNAMN, PERSON_MELLANNAMN, PERSON_EFTERNAMN, MODEL, sparad);
    }

    public static Intyg buildIntyg(String enhetsId, IntygsStatus status, String type, String hoSPersonId,
            String hoSPersonNamn, String personNummer, String personFornamn, String personMellannamn,
            String personEfternamn, String model, LocalDateTime senastSparadDatum) {
        return buildIntyg(UUID.randomUUID().toString(), enhetsId, status, type, hoSPersonId, hoSPersonNamn,
                personNummer, personFornamn, personMellannamn, personEfternamn, model, senastSparadDatum);
    }

    public static Intyg buildIntyg(String intygsId, String enhetsId, IntygsStatus status, String type,
            String hoSPersonId, String hoSPersonNamn, String personNummer, String personFornamn,
            String personMellannamn, String personEfternamn, String model, LocalDateTime senastSparadDatum) {
        Intyg intyg = new Intyg();
        intyg.setIntygsId(intygsId);
        intyg.setIntygsTyp(type);
        intyg.setEnhetsId(enhetsId);
        intyg.setPatientPersonnummer(personNummer);
        intyg.setPatientFornamn(personFornamn);
        intyg.setPatientMellannamn(personMellannamn);
        intyg.setPatientEfternamn(personEfternamn);
        VardpersonReferens vardpersonReferens = new VardpersonReferens();
        vardpersonReferens.setHsaId(hoSPersonId);
        vardpersonReferens.setNamn(hoSPersonNamn);
        intyg.setSenastSparadAv(vardpersonReferens);
        intyg.setSenastSparadDatum(LocalDateTime.now());
        intyg.setSkapadAv(vardpersonReferens);

        if (senastSparadDatum != null) {
            intyg.setSenastSparadDatum(senastSparadDatum);
        }

        intyg.setStatus(status);

        intyg.setModel(model);

        return intyg;
    }
    
    public static Signatur buildSignatur(String intygId, String signeradAv, LocalDateTime signeringsDatum) {
        return new Signatur(signeringsDatum, signeradAv, intygId, "<intygs-data>", "<intyg-hash>", "<signatur-data>");  
    }
    
}
