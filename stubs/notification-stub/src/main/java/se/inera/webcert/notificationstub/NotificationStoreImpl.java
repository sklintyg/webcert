package se.inera.webcert.notificationstub;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDateTime;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

public class NotificationStoreImpl implements NotificationStore {

    private static final double LOAD = 0.8;

    private final int maxSize;

    private final int minSize;

    public NotificationStoreImpl(int maxSize) {
        this.maxSize = maxSize;
        this.minSize = Double.valueOf(maxSize * LOAD).intValue();
    }

    private Multimap<String, CertificateStatusUpdateForCareType> notificationsMap = ArrayListMultimap.create();

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.notificationstub.NotificationStore#put(java.lang.String,
     * se.inera.certificate.clinicalprocess
     * .healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType)
     */
    @Override
    public void put(String utlatandeId, CertificateStatusUpdateForCareType request) {
        notificationsMap.put(utlatandeId, request);
        if (notificationsMap.size() >= maxSize) {
            purge(notificationsMap);
        }
    }

    public int size() {
        return this.notificationsMap.size();
    }

    public void purge(Multimap<String, CertificateStatusUpdateForCareType> notificationsMap) {

        // find the oldest ones
        Ordering<CertificateStatusUpdateForCareType> order = new Ordering<CertificateStatusUpdateForCareType>() {

            @Override
            public int compare(CertificateStatusUpdateForCareType left, CertificateStatusUpdateForCareType right) {
                LocalDateTime lDate = left.getUtlatande().getHandelse().getHandelsetidpunkt();
                LocalDateTime rDate = right.getUtlatande().getHandelse().getHandelsetidpunkt();

                return lDate.compareTo(rDate);
            }
        };

        // sort the list of entries in the map so that the oldest entries are first
        List<CertificateStatusUpdateForCareType> sortedEntries = order.sortedCopy(notificationsMap.values());

        Iterator<CertificateStatusUpdateForCareType> iter = sortedEntries.iterator();

        // trim map down to minSize
        while (notificationsMap.size() > minSize) {
            CertificateStatusUpdateForCareType objToRemove = iter.next();
            String intygsId = objToRemove.getUtlatande().getUtlatandeId().getExtension();
            notificationsMap.remove(intygsId, objToRemove);
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getMinSize() {
        return minSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.notificationstub.NotificationStore#getNotifications()
     */
    @Override
    public Collection<CertificateStatusUpdateForCareType> getNotifications() {
        return notificationsMap.values();
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.notificationstub.NotificationStore#clear()
     */
    @Override
    public void clear() {
        notificationsMap.clear();
    }
}
