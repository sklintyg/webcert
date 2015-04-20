package se.inera.webcert.notificationstub;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.UtlatandeType;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class NotificationStoreTest {

    private static List<String> INTYG_IDS = Arrays.asList("intyg1", "intyg2", "intyg3", "intyg4", "intyg5", "intyg6", "intyg7", "intyg8", "intyg9", "intyg10");

    
    @Test
    public void testPurge() {
        
        Multimap<String, CertificateStatusUpdateForCareType> notificationsMap = initNotificationsMap(110);
        assertEquals(110, notificationsMap.size());
        
        NotificationStoreImpl notificationStore = new NotificationStoreImpl(100);
        notificationStore.purge(notificationsMap);
        
        assertEquals(80, notificationsMap.size());
    }

    private Multimap<String, CertificateStatusUpdateForCareType> initNotificationsMap(int nbr) {
        
        Iterator<String> intygsIdIter = Iterables.cycle(INTYG_IDS).iterator();
        
        Multimap<String, CertificateStatusUpdateForCareType> notificationsMap = ArrayListMultimap.create();

        LocalDateTime baseTime = LocalDateTime.now();

        for (int i = 0; i < nbr; i++) {

            String intygsId = intygsIdIter.next();

            Handelse handelse = new Handelse();
            baseTime = baseTime.minusMinutes(1);
            handelse.setHandelsetidpunkt(baseTime);

            UtlatandeType utl = new UtlatandeType();
            utl.setHandelse(handelse);
            utl.setUtlatandeId(new UtlatandeId());
            utl.getUtlatandeId().setExtension(intygsId);

            CertificateStatusUpdateForCareType type = new CertificateStatusUpdateForCareType();
            type.setUtlatande(utl);

            notificationsMap.put(intygsId, type);
        }

        return notificationsMap;
    }

}
