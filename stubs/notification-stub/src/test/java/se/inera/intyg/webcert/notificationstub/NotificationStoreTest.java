/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notificationstub;

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
