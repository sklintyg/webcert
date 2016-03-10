package se.inera.intyg.webcert.web.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.fragasvar.model.*;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeMetaData;

public class ArendeMetaDataConverterTest {

    @Test
    public void testConvert() {
        final String fragestallare = "fragestallare";
        final String intygId = "intygId";
        final String intygTyp = "intygTyp";
        final String patientId = "patientId";
        final long internReferens = 13;
        final LocalDateTime senasteHandelse = LocalDateTime.now();
        final String signeratAv = "signeratAv";
        final String enhetsnamn = "enhetsnamn";
        final String vardgivarnamn = "vardgivarnamn";
        final Amne amne = Amne.ARBETSTIDSFORLAGGNING;
        final boolean vidarebefordrad = false;
        final Status status = Status.PENDING_INTERNAL_ACTION;

        FragaSvar fs = createFragaSvar(fragestallare, intygId, intygTyp, patientId, internReferens, senasteHandelse, signeratAv, enhetsnamn,
                vardgivarnamn, amne, vidarebefordrad, status);
        ArendeMetaData arende = ArendeMetaDataConverter.convert(fs);

        assertNotNull(arende);
        assertEquals(fragestallare, arende.getFragestallare());
        assertEquals(intygId, arende.getIntygId());
        assertEquals(intygTyp, arende.getIntygTyp());
        assertEquals(patientId, arende.getPatientId());
        assertEquals(Long.toString(internReferens), arende.getMeddelandeId());
        assertEquals(senasteHandelse, arende.getReceivedDate());
        assertEquals(signeratAv, arende.getSigneratAv());
        assertEquals(enhetsnamn, arende.getEnhetsnamn());
        assertEquals(vardgivarnamn, arende.getVardgivarnamn());
        assertEquals(amne.name(), arende.getAmne());
        assertEquals(vidarebefordrad, arende.isVidarebefordrad());
        assertEquals(status, arende.getStatus());
    }

    @Test
    public void testConvertEmptyIntygReferens() {
        FragaSvar fs = new FragaSvar();
        fs.setVardperson(new Vardperson());
        ArendeMetaData arende = ArendeMetaDataConverter.convert(fs);
        assertNull(arende);
    }

    @Test
    public void testConvertEmptyVardperson() {
        FragaSvar fs = new FragaSvar();
        fs.setIntygsReferens(new IntygsReferens());
        ArendeMetaData arende = ArendeMetaDataConverter.convert(fs);
        assertNull(arende);
    }

    private FragaSvar createFragaSvar(String fragestallare, String intygsId, String intygsTyp, String patientId, Long internReferens,
            LocalDateTime senasteHandelse, String signeratAv, String enhetsnamn, String vardgivarnamn, Amne amne, Boolean vidarebefordrad,
            Status status) {
        FragaSvar res = new FragaSvar();
        res.setFrageStallare(fragestallare);
        res.setIntygsReferens(new IntygsReferens(intygsId, intygsTyp, new Personnummer(patientId), null, null));
        res.setInternReferens(internReferens);
        res.setFrageSkickadDatum(senasteHandelse);
        Vardperson vp = new Vardperson();
        vp.setNamn(signeratAv);
        vp.setEnhetsnamn(enhetsnamn);
        vp.setVardgivarnamn(vardgivarnamn);
        res.setVardperson(vp);
        res.setAmne(amne);
        res.setVidarebefordrad(vidarebefordrad);
        res.setStatus(status);
        return res;
    }
}
