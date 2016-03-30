package se.inera.intyg.webcert.web.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.*;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeListItem;

public class ArendeListItemConverterTest {

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
        ArendeListItem arende = ArendeListItemConverter.convert(fs);

        assertNotNull(arende);
        assertEquals(fragestallare, arende.getFragestallare());
        assertEquals(intygId, arende.getIntygId());
        assertEquals(intygTyp, arende.getIntygTyp());
        assertEquals(patientId, arende.getPatientId());
        assertEquals(Long.toString(internReferens), arende.getMeddelandeId());
        assertEquals(senasteHandelse, arende.getReceivedDate());
        assertEquals(signeratAv, arende.getSigneratAvNamn());
        assertEquals(enhetsnamn, arende.getEnhetsnamn());
        assertEquals(vardgivarnamn, arende.getVardgivarnamn());
        assertEquals(ArendeAmne.fromAmne(amne).get().name(), arende.getAmne());
        assertEquals(vidarebefordrad, arende.isVidarebefordrad());
        assertEquals(status, arende.getStatus());
    }

    @Test
    public void testConvertEmptyIntygReferens() {
        FragaSvar fs = new FragaSvar();
        fs.setVardperson(new Vardperson());
        ArendeListItem arende = ArendeListItemConverter.convert(fs);
        assertNull(arende);
    }

    @Test
    public void testConvertEmptyVardperson() {
        FragaSvar fs = new FragaSvar();
        fs.setIntygsReferens(new IntygsReferens());
        ArendeListItem arende = ArendeListItemConverter.convert(fs);
        assertNull(arende);
    }

    @Test
    public void testConvertArende() {
        final ArendeAmne amne = ArendeAmne.KONTKT;
        final String intygsId = "intygsId";
        final String intygTyp = "luse";
        final String meddelandeId = "meddelandeId";
        final String patientPersonId = "patientPersonId";
        final String signeratAvName = "signeratAvName";
        final String skickatAv = "skickatAv";
        final LocalDateTime skickatTidpunkt = LocalDateTime.now();
        final Status status = Status.ANSWERED;
        final Boolean vidarebefordrad = Boolean.TRUE;

        Arende arende = createArende(amne, intygsId, intygTyp, meddelandeId, patientPersonId, signeratAvName, skickatAv, skickatTidpunkt, status,
                vidarebefordrad);
        ArendeListItem result = ArendeListItemConverter.convert(arende);

        assertEquals(amne.name(), result.getAmne());
        assertEquals(intygsId, result.getIntygId());
        assertEquals(intygTyp, result.getIntygTyp());
        assertEquals(meddelandeId, result.getMeddelandeId());
        assertEquals(patientPersonId, result.getPatientId());
        assertEquals(signeratAvName, result.getSigneratAvNamn());
        assertEquals(skickatAv, result.getFragestallare());
        assertEquals(skickatTidpunkt, result.getReceivedDate());
        assertEquals(status, result.getStatus());
        assertEquals(true, result.isVidarebefordrad());
    }

    @Test
    public void testConvertArendeVidarebefordradNull() {
        Arende arende = createArende(ArendeAmne.KONTKT, "intygsId", "intygTyp", "meddelandeId", "patientPersonId", "signeratAvName", "skickatAv",
                LocalDateTime.now(), Status.ANSWERED,
                null);
        ArendeListItem result = ArendeListItemConverter.convert(arende);

        assertEquals(false, result.isVidarebefordrad());
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

    private Arende createArende(ArendeAmne amne, String intygsId, String intygTyp, String meddelandeId, String patientPersonId,
            String signeratAvName, String skickatAv, LocalDateTime skickatTidpunkt, Status status, Boolean vidarebefordrad) {
        Arende arende = new Arende();
        arende.setAmne(amne);
        arende.setIntygsId(intygsId);
        arende.setIntygTyp(intygTyp);
        arende.setMeddelandeId(meddelandeId);
        arende.setPatientPersonId(patientPersonId);
        arende.setSigneratAvName(signeratAvName);
        arende.setSkickatAv(skickatAv);
        arende.setSkickatTidpunkt(skickatTidpunkt);
        arende.setStatus(status);
        arende.setVidarebefordrad(vidarebefordrad);
        return arende;
    }
}
