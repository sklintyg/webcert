package se.inera.intyg.webcert.notification_sender.notifications.services;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.schemas.contract.Personnummer;

public class NotificationResultMessageCreatorTest {

    private static final Object INTYGS_ID = "";
    private static final Object LUSE = "";

    private Utlatande getUtlatandeMock() {
        final var utlatande = mock(Utlatande.class);

        doReturn(INTYGS_ID).when(utlatande).getId();
        doReturn(LUSE).when(utlatande).getTyp();
        doReturn("1.0").when(utlatande).getTextVersion();

        final var grundData = mock(GrundData.class);
        doReturn(grundData).when(utlatande).getGrundData();

        final var skapadAv = mock(HoSPersonal.class);
        doReturn(skapadAv).when(grundData).getSkapadAv();

        final var vardenhet = mock(Vardenhet.class);
        doReturn(vardenhet).when(skapadAv).getVardenhet();

        final var vardgivare = mock(se.inera.intyg.common.support.model.common.internal.Vardgivare.class);
        doReturn(vardgivare).when(vardenhet).getVardgivare();

        final var patient = mock(Patient.class);
        doReturn(patient).when(grundData).getPatient();

        doReturn(Personnummer.createPersonnummer("191212121212").get()).when(patient).getPersonId();

        return utlatande;
    }
}