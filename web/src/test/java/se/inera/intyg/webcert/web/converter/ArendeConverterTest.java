package se.inera.intyg.webcert.web.converter;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType.Komplettering;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v1.SendMessageToCareType.SkickatAv;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.v2.MeddelandeReferens;

public class ArendeConverterTest {

    @Test
    public void testConvertArende() {
        final ArendeAmne amneskod = ArendeAmne.ARBTID;
        final String intygId = "intygId";
        final String kontaktInfo = "kontaktInfo";
        final String skickatAv = "FKASSA";
        final String frageId = "frageId";
        final Integer instans = 1;
        final String kompletteringsText = "kompletteringsText";
        final String meddelande = "meddelande";
        final String meddelandeId = "meddelandeId";
        final String paminnelseMeddelandeId = "paminnelseMeddelandeId";
        final String personId = "personId";
        final String referensId = "referensId";
        final String rubrik = "rubrik";
        final LocalDate sistaDatum = LocalDate.now();
        final LocalDateTime skickatTidpunkt = LocalDateTime.now();
        final String svarPa = "svarPa";
        final String svarReferensId = "svarReferensId";
        SendMessageToCareType input = createSendMessageToCare(amneskod.name(), intygId, kontaktInfo, skickatAv, frageId, instans, kompletteringsText,
                meddelande, meddelandeId, paminnelseMeddelandeId, personId, referensId, rubrik, sistaDatum, skickatTidpunkt, svarPa, svarReferensId);
        Arende res = ArendeConverter.convert(input);
        assertEquals(amneskod, res.getAmne());
        assertEquals(intygId, res.getIntygsId());
        assertEquals(kontaktInfo, res.getKontaktInfo().get(0));
        assertEquals("FK", res.getSkickatAv());
        assertEquals(frageId, res.getKomplettering().get(0).getFrageId());
        assertEquals(instans, res.getKomplettering().get(0).getInstans());
        assertEquals(kompletteringsText, res.getKomplettering().get(0).getText());
        assertEquals(meddelande, res.getMeddelande());
        assertEquals(meddelandeId, res.getMeddelandeId());
        assertEquals(paminnelseMeddelandeId, res.getPaminnelseMeddelandeId());
        assertEquals(personId, res.getPatientPersonId());
        assertEquals(referensId, res.getReferensId());
        assertEquals(rubrik, res.getRubrik());
        assertEquals(sistaDatum, res.getSistaDatumForSvar());
        assertEquals(skickatTidpunkt, res.getSkickatTidpunkt());
        assertEquals(svarPa, res.getSvarPaId());
        assertEquals(svarReferensId, res.getSvarPaReferens());
    }

    private SendMessageToCareType createSendMessageToCare(String amneskod, String intygId, String kontaktInfo, String skickatAv, String frageId,
            Integer instans, String kompletteringsText, String meddelande, String meddelandeId, String paminnelseMeddelandeId, String personId,
            String referensId, String rubrik, LocalDate sistaDatum, LocalDateTime skickatTidpunkt, String svarPa, String svarReferensId) {
        SendMessageToCareType res = new SendMessageToCareType();

        Amneskod amne = new Amneskod();
        amne.setCode(amneskod);
        res.setAmne(amne);

        SkickatAv sa = new SkickatAv();
        sa.getKontaktInfo().add(kontaktInfo);
        Part part = new Part();
        part.setCode(skickatAv);
        sa.setPart(part);
        res.setSkickatAv(sa);

        Komplettering komplettering = new Komplettering();
        komplettering.setFrageId(frageId);
        komplettering.setInstans(instans);
        komplettering.setText(kompletteringsText);
        res.getKomplettering().add(komplettering);

        PersonId pid = new PersonId();
        pid.setExtension(personId);
        res.setPatientPersonId(pid);

        MeddelandeReferens mr = new MeddelandeReferens();
        mr.setMeddelandeId(svarPa);
        mr.getReferensId().add(svarReferensId);
        res.setSvarPa(mr);

        IntygId ii = new IntygId();
        ii.setExtension(intygId);
        res.setIntygsId(ii);

        res.setMeddelande(meddelande);
        res.setMeddelandeId(meddelandeId);
        res.setPaminnelseMeddelandeId(paminnelseMeddelandeId);
        res.setReferensId(referensId);
        res.setRubrik(rubrik);
        res.setSistaDatumForSvar(sistaDatum);
        res.setSkickatTidpunkt(skickatTidpunkt);

        return res;
    }
    /*
     * if (request.getSvarPa() != null) {
     * res.setSvarPaId(request.getSvarPa().getMeddelandeId());
     * res.setSvarPaReferens(extractReferensId(request.getSvarPa()));
     * }
     * return res;
     * }
     *
     * // There are between 0 and 1 referensid in the MeddelandeReferens according to specification 2.0.RC3
     * // Because of this we get the first item if there exists one
     * private static String extractReferensId(MeddelandeReferens meddelandeReferens) {
     * return meddelandeReferens.getReferensId() != null && !meddelandeReferens.getReferensId().isEmpty()
     * ? meddelandeReferens.getReferensId().get(0)
     * : null;
     * }
     *
     * private static MedicinsktArende convert(Komplettering k) {
     * MedicinsktArende res = new MedicinsktArende();
     * res.setFrageId(k.getFrageId());
     * res.setText(k.getText());
     * res.setInstans(k.getInstans());
     * return res;
     * }
     */
}
