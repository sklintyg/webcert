package se.inera.intyg.webcert.persistence.fragasvar.repository.util;

import org.joda.time.LocalDateTime;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Status;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;

import java.util.Arrays;
import java.util.HashSet;


public final class FragaSvarTestUtil {

    private FragaSvarTestUtil() {
    }

    public static final String FRAGA_TEXT = "To be, or not to be: that is the question:";
    public static final String SVAR_TEXT = "This are not the droids you are looking for";

    private static final LocalDateTime FRAGE_SENT_DATE = new LocalDateTime("2012-03-01T12:00:00");
    private static final LocalDateTime SVAR_SIGN_DATE = new LocalDateTime("2014-10-21T11:11:11");
    private static final LocalDateTime SVAR_SENT_DATE = new LocalDateTime("2014-10-21T12:00:00");

    private static final IntygsReferens INTYGS_REFERENS = new IntygsReferens("abc123", "fk", new Personnummer("19121212-1212"),
            "Sven Persson", FRAGE_SENT_DATE);

    public static String ENHET_1_ID = "ENHET_TEST_1_ID";
    public static String ENHET_2_ID = "ENHET_TEST_2_ID";

    public static FragaSvar buildFraga(long fragaSvarId, String enhetsId, Status status, Amne amne, String fragestallare, String hsaId,  String fragaSkickad, boolean vidarebefordrad) {
        return buildFraga(fragaSvarId, enhetsId, status, amne, fragestallare, hsaId, LocalDateTime.parse(fragaSkickad), vidarebefordrad);
    }


    public static FragaSvar buildFraga(long fragaSvarId, String enhetsId, Status status, Amne amne, String fragestallare, String hsaId, LocalDateTime fragaSkickad, boolean vidarebefordrad) {

        FragaSvar f = new FragaSvar();
        f.setInternReferens(fragaSvarId);

        f.setExternaKontakter(new HashSet<String>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));

        if (fragestallare.equalsIgnoreCase("FK")) {
            f.setExternReferens("externReferens-" + fragaSvarId);
        }

        f.setFrageSigneringsDatum(fragaSkickad);
        f.setFrageSkickadDatum(fragaSkickad);
        f.setAmne((amne != null) ? amne : Amne.OVRIGT);

        f.setVidarebefordrad(vidarebefordrad);

        f.setFrageStallare(fragestallare);
        Vardperson vardperson = new Vardperson();
        vardperson.setHsaId(hsaId);
        vardperson.setEnhetsId(enhetsId);
        vardperson.setEnhetsnamn(enhetsId + "-namnet");

        f.setVardperson(vardperson);

        f.setFrageText(FRAGA_TEXT);

        f.setIntygsReferens(INTYGS_REFERENS);

        f.setStatus(status);

        return f;
    }

    /**
     * Builds a FragaSvara, a question with reply, from the supplied params.
     * @param enhetsId
     * @param status
     * @param fragestallare
     * @param hsaId
     * @param fragaSkickad
     * @param svarSkickad
     * @param vidarebefordrad
     * @return
     */
    public static FragaSvar buildFragaWithSvar(String enhetsId, Status status, Amne amne, String fragestallare, String hsaId, String fragaSkickad, String svarSkickad, boolean vidarebefordrad) {

        FragaSvar f = buildFraga(1L, enhetsId, status, amne, fragestallare, hsaId, fragaSkickad, vidarebefordrad);

        f.setSvarSigneringsDatum((svarSkickad != null ? LocalDateTime.parse(svarSkickad) : SVAR_SIGN_DATE));
        f.setSvarSkickadDatum((svarSkickad != null ? LocalDateTime.parse(svarSkickad) : SVAR_SENT_DATE));

        f.setSvarsText(SVAR_TEXT);

        return f;
    }
}
