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
package se.inera.intyg.webcert.persistence.fragasvar.repository.util;

import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.intyg.webcert.persistence.model.Status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

public final class FragaSvarTestUtil {

    private FragaSvarTestUtil() {
    }

    public static final String FRAGA_TEXT = "To be, or not to be: that is the question:";
    public static final String SVAR_TEXT = "This are not the droids you are looking for";

    private static final LocalDateTime FRAGE_SENT_DATE = LocalDateTime.parse("2012-03-01T12:00:00");
    private static final LocalDateTime SVAR_SIGN_DATE = LocalDateTime.parse("2014-10-21T11:11:11");
    private static final LocalDateTime SVAR_SENT_DATE = LocalDateTime.parse("2014-10-21T12:00:00");

    private static final IntygsReferens INTYGS_REFERENS = new IntygsReferens("abc123", "fk7263",
            Personnummer.createPersonnummer("19121212-1212").get(), "Sven Persson", FRAGE_SENT_DATE);

    public static String ENHET_1_ID = "ENHET_TEST_1_ID";
    public static String ENHET_2_ID = "ENHET_TEST_2_ID";

    public static FragaSvar buildFraga(long fragaSvarId, String enhetsId, Status status, Amne amne, String fragestallare, String hsaId,
            String fragaSkickad, boolean vidarebefordrad) {
        return buildFraga(fragaSvarId, enhetsId, status, amne, fragestallare, hsaId, LocalDateTime.parse(fragaSkickad), vidarebefordrad);
    }

    public static FragaSvar buildFraga(long fragaSvarId, String enhetsId, Status status, Amne amne, String fragestallare, String hsaId,
            LocalDateTime fragaSkickad, boolean vidarebefordrad) {

        FragaSvar f = new FragaSvar();
        f.setInternReferens(fragaSvarId);

        f.setExternaKontakter(new HashSet<>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));

        if (fragestallare.equalsIgnoreCase("FKASSA")) {
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
     *
     * @param enhetsId
     * @param status
     * @param fragestallare
     * @param hsaId
     * @param fragaSkickad
     * @param svarSkickad
     * @param vidarebefordrad
     * @return
     */
    public static FragaSvar buildFragaWithSvar(String enhetsId, Status status, Amne amne, String fragestallare, String hsaId, String fragaSkickad,
            String svarSkickad, boolean vidarebefordrad) {

        FragaSvar f = buildFraga(1L, enhetsId, status, amne, fragestallare, hsaId, fragaSkickad, vidarebefordrad);

        f.setSvarSigneringsDatum((svarSkickad != null ? LocalDateTime.parse(svarSkickad) : SVAR_SIGN_DATE));
        f.setSvarSkickadDatum((svarSkickad != null ? LocalDateTime.parse(svarSkickad) : SVAR_SENT_DATE));

        f.setSvarsText(SVAR_TEXT);

        return f;
    }
}
