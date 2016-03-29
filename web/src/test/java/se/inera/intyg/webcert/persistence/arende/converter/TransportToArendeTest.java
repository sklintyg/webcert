package se.inera.intyg.webcert.persistence.arende.converter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.modules.fkparent.model.converter.RespConstants;
import se.inera.certificate.modules.sjukersattning.model.internal.SjukersattningUtlatande;
import se.inera.certificate.modules.sjukersattning.model.internal.SjukersattningUtlatande.Builder;
import se.inera.certificate.modules.sjukersattning.rest.SjukersattningModuleApi;
import se.inera.certificate.modules.sjukpenning_utokad.model.internal.SjukpenningUtokadUtlatande;
import se.inera.certificate.modules.sjukpenning_utokad.rest.SjukpenningUtokadModuleApi;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.InternalDate;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.model.MedicinsktArende;
import se.inera.intyg.webcert.web.converter.util.TransportToArende;
import se.inera.intyg.webcert.web.service.intyg.IntygServiceImpl;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView;

@RunWith(MockitoJUnitRunner.class)
public class TransportToArendeTest {
    private static final String PATIENT_PERSON_ID = "19121212-1212";
    private static final String SKAPADAV_PERSON_ID = "19121212-1212";
    private static final String ENHETS_ID = "enhetsId";
    private static final String ENHETS_NAMN = "enhetsNamn";
    private static final String intygsId = "intyg-1";

    @InjectMocks
    private TransportToArende converter;

    @Mock
    private IntygModuleRegistryImpl moduleRegistry;

    @Mock
    private IntygServiceImpl intygService;
    private Status status;
    private se.inera.intyg.webcert.persistence.model.Status webcertStatus = se.inera.intyg.webcert.persistence.model.Status.PENDING_INTERNAL_ACTION;;

    @Test
    public void testConvertToArendeForSjukersattning() throws ModuleNotFoundException {
        Arende arende = buildArende();

        SjukersattningModuleApi moduleApi = new SjukersattningModuleApi();
        LocalDateTime timeStamp = LocalDateTime.now().minusDays(2);
        String skapadAvNamn = "Test Testsson";
        SjukersattningUtlatande utlatande = buildSjukersattningsUtlatande(intygsId, ENHETS_ID, ENHETS_NAMN, PATIENT_PERSON_ID, skapadAvNamn,
                SKAPADAV_PERSON_ID,
                timeStamp);
        ArendeView result = setupMocks(arende, moduleApi, timeStamp, utlatande);

        assertEquals(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_ANHORIGS_BESKRIVNING_SVAR_JSON_ID_1,
                result.getKompletteringar().get(0).getJsonPropertyHandle());
        assertEquals(RespConstants.KANNEDOM_SVAR_JSON_ID_2,
                result.getKompletteringar().get(1).getJsonPropertyHandle());
        assertEquals(RespConstants.UNDERLAG_SVAR_JSON_ID_4,
                result.getKompletteringar().get(2).getJsonPropertyHandle());
        assertEquals(new Integer(0), result.getKompletteringar().get(0).getPosition());
        assertEquals(new Integer(0), result.getKompletteringar().get(1).getPosition());
        assertEquals(new Integer(2), result.getKompletteringar().get(2).getPosition());
    }

    @Test
    public void testConvertToArendeForSjukpenning() throws ModuleNotFoundException {
        Arende arende = buildArende();

        SjukpenningUtokadModuleApi moduleApi = new SjukpenningUtokadModuleApi();
        LocalDateTime timeStamp = LocalDateTime.now().minusDays(2);
        String skapadAvNamn = "Test Testsson";
        SjukpenningUtokadUtlatande utlatande = buildSjukpenningUtlatande(intygsId, ENHETS_ID, ENHETS_NAMN, PATIENT_PERSON_ID, skapadAvNamn,
                SKAPADAV_PERSON_ID,
                timeStamp);
        ArendeView result = setupMocks(arende, moduleApi, timeStamp, utlatande);

        assertEquals(RespConstants.GRUNDFORMEDICINSKTUNDERLAG_TELEFONKONTAKT_PATIENT_SVAR_JSON_ID_1,
                result.getKompletteringar().get(0).getJsonPropertyHandle());
        assertEquals(RespConstants.KANNEDOM_SVAR_JSON_ID_2,
                result.getKompletteringar().get(1).getJsonPropertyHandle());
        assertEquals(RespConstants.UNDERLAG_SVAR_JSON_ID_4,
                result.getKompletteringar().get(2).getJsonPropertyHandle());
        assertEquals(new Integer(0), result.getKompletteringar().get(0).getPosition());
        assertEquals(new Integer(0), result.getKompletteringar().get(1).getPosition());
        assertEquals(new Integer(2), result.getKompletteringar().get(2).getPosition());
    }

    private ArendeView setupMocks(Arende arende, ModuleApi moduleApi, LocalDateTime timeStamp, Utlatande utlatande)
            throws ModuleNotFoundException {
        status = new Status(CertificateState.RECEIVED, intygsId, timeStamp);
        IntygContentHolder content = new IntygContentHolder("", utlatande, Arrays.asList(status), false, Optional.empty());

        when(moduleRegistry.getModuleApi(any(String.class))).thenReturn(moduleApi);
        when(intygService.fetchIntygData(any(String.class), any(String.class))).thenReturn(content);

        return converter.convert(arende);
    }

    private SjukpenningUtokadUtlatande buildSjukpenningUtlatande(String intygsid2, String enhetsId, String enhetsNamn, String patientPersonId,
            String skapadAvNamn, String skapadavPersonId, LocalDateTime timeStamp) {

        se.inera.certificate.modules.sjukpenning_utokad.model.internal.SjukpenningUtokadUtlatande.Builder template = SjukpenningUtokadUtlatande
                .builder();

        template.setId(intygsId);
        GrundData grundData = buildGrundData(enhetsId, enhetsNamn, patientPersonId, skapadavPersonId, timeStamp);
        template.setGrundData(grundData);
        template.setTextVersion("");
        template.setTelefonkontaktMedPatienten(new InternalDate(timeStamp.toLocalDate()));

        return template.build();
    }

    private SjukersattningUtlatande buildSjukersattningsUtlatande(String intygsId, String enhetsId, String enhetsnamn, String patientPersonId,
            String string, String skapadAvId, LocalDateTime timeStamp) {
        Builder template = SjukersattningUtlatande.builder();
        template.setId(intygsId);
        GrundData grundData = buildGrundData(enhetsId, enhetsnamn, patientPersonId, skapadAvId, timeStamp);
        template.setGrundData(grundData);
        template.setTextVersion("");
        template.setAnhorigsBeskrivningAvPatienten(new InternalDate(timeStamp.toLocalDate()));

        return template.build();
    }

    private GrundData buildGrundData(String enhetsId, String enhetsNamn, String patientPersonId, String skapadavPersonId, LocalDateTime timeStamp) {
        GrundData grundData = new GrundData();
        HoSPersonal skapadAv = new HoSPersonal();
        Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(enhetsId);
        vardenhet.setEnhetsnamn(enhetsNamn);
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid("vardgivarid");
        vardgivare.setVardgivarnamn("vardgivarnamn");
        vardenhet.setVardgivare(vardgivare);
        skapadAv.setVardenhet(vardenhet);
        skapadAv.setFullstandigtNamn(skapadavPersonId);
        skapadAv.setPersonId(skapadavPersonId);
        grundData.setSkapadAv(skapadAv);
        Patient patient = new Patient();
        Personnummer personId = new Personnummer(patientPersonId);
        patient.setPersonId(personId);
        grundData.setPatient(patient);
        grundData.setSigneringsdatum(timeStamp);
        return grundData;
    }

    private Arende buildArende() {
        Arende arende = new Arende();
        arende.setAmne(ArendeAmne.OVRIGT);
        arende.setIntygsId(intygsId);
        arende.setStatus(webcertStatus);
        arende.setMeddelandeId("meddelandeId");
        arende.setTimestamp(LocalDateTime.now());
        MedicinsktArende medArende1 = buildMedicinsktArende("1", 1, "arende1");
        MedicinsktArende medArende2 = buildMedicinsktArende("2", 1, "arende1");
        MedicinsktArende medArende4 = buildMedicinsktArende("4", 3, "arende1");
        arende.setKomplettering(Arrays.asList(medArende1, medArende2, medArende4));

        return arende;
    }

    private MedicinsktArende buildMedicinsktArende(String frageId, int instansId, String text) {
        MedicinsktArende med1 = new MedicinsktArende();
        med1.setFrageId(frageId);
        med1.setInstans(instansId);
        med1.setText(text);
        return med1;
    }

}
