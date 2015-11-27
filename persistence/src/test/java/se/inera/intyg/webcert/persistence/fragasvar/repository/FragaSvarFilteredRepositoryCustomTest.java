package se.inera.intyg.webcert.persistence.fragasvar.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.persistence.fragasvar.model.Amne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvarStatus;
import se.inera.intyg.webcert.persistence.fragasvar.model.Status;
import se.inera.intyg.webcert.persistence.fragasvar.repository.util.FragaSvarTestUtil;

/**
 * Test for filtering FragaSvar.
 *
 * @author nikpet
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class FragaSvarFilteredRepositoryCustomTest {

    private static final String WEBCERT = "WC";
    private static final String FK = "FK";

    private static final String HSA_ID_1 = "hsaId-1";
    private static final String HSA_ID_2 = "hsaId-2";

    @Autowired
    private FragaSvarRepository fragasvarRepository;

    @Test
    public void testFilterFragaFromWC() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setQuestionFromWC(true);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertEquals(5, fsList.size());
    }

    /**
     * Should filter all FS that belongs to ENHET_2 and is not CLOSED.
     */
    @Test
    public void testCountFilterFraga() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_2_ID);

        int res = fragasvarRepository.filterCountFragaSvar(filter);
        Assert.assertEquals(3, res);
    }

    /**
     * Should filter all FS that belongs to ENHET_1 and is not CLOSED.
     */
    @Test
    public void testFilterFragaWithPaging() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setPageSize(10);
        filter.setStartFrom(0);

        int res = fragasvarRepository.filterCountFragaSvar(filter);
        Assert.assertEquals(14, res);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        Assert.assertEquals(10, fsList.size());
    }

    @Test
    public void testFilterHsaId() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setHsaId(HSA_ID_2);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertEquals(3, fsList.size());
    }

    @Test
    public void testFilterChangedFrom() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setChangedFrom(LocalDateTime.parse("2013-10-01T15:10:00"));

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertEquals(6, fsList.size());
    }

    @Test
    public void testFilterChangedTo() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setChangedTo(LocalDateTime.parse("2013-10-01T15:10:00"));

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertEquals(8, fsList.size());

    }

    /**
     * Should filter out all FS that is not CLOSED and has <= changedFrom
     * and >= changedTo.
     */
    @Test
    public void testFilterChangedBetween() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setChangedFrom(LocalDateTime.parse("2013-10-01T15:00:00"));
        filter.setChangedTo(LocalDateTime.parse("2013-10-01T15:10:00"));

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertEquals(8, fsList.size());
    }

    @Test
    public void testFilterVidarebefordrad() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVidarebefordrad(true);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        Assert.assertEquals(3, fsList.size());
    }

    /**
     * Should return FS with status PENDING_INTERNAL_ACTION and a subject like OVRIGT, ARBETSTIDSFORLAGGNING,
     * AVSTAMNINGSMOTE, KONTAKT.
     */
    @Test
    public void testFilterWaitingForReplyFromCare() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVantarPa(VantarPa.SVAR_FRAN_VARDEN);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        Assert.assertEquals(6, fsList.size());

    }

    /**
     * Should return FS with status PENDING_EXTERNAL_ACTION
     */
    @Test
    public void testFilterWaitingForReplyFromFK() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVantarPa(VantarPa.SVAR_FRAN_FK);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        Assert.assertEquals(4, fsList.size());
    }

    /**
     * Should return FS with status ANSWERED or (status PENDING_INTERNAL_ACTION and subject MAKULERING_AV_LAKARINTYG )
     * or (status PENDING_INTERNAL_ACTION and subject PAMINNELSE).
     */
    @Test
    public void testFilterMarkAsHandled() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVantarPa(VantarPa.MARKERA_SOM_HANTERAD);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertEquals(3, fsList.size());
    }

    /**
     * Should return FS with status PENDING_INTERNAL_ACTION and subject KOMPLETTERING_AV_LAKARINTYG.
     */
    @Test
    public void testFilterVantaPaKomplettering() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVantarPa(VantarPa.KOMPLETTERING_FRAN_VARDEN);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertEquals(1, fsList.size());
    }

    /**
     * Should return all FS that is not CLOSED.
     */
    @Test
    public void testFilterAllNotHandled() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVantarPa(VantarPa.ALLA_OHANTERADE);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertEquals(14, fsList.size());
    }

    /**
     * Should filter out all FS with status PENDING_EXTERNAL_ACTION and is <= changedFrom
     * and >= changedTo.
     */
    @Test
    public void testFilterChangedBetweenAndAwaitingReplyFromFK() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setChangedFrom(LocalDateTime.parse("2013-10-01T15:03:00"));
        filter.setChangedTo(LocalDateTime.parse("2013-10-01T15:10:00"));
        filter.setVantarPa(VantarPa.SVAR_FRAN_FK);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertEquals(4, fsList.size());
    }

    /**
     * Should filter out all FS with status PENDING_EXTERNAL_ACTION that belongs to
     * either ENHET_1_ID or ENHET_2_ID.
     */
    @Test
    public void testFilterWithTwoEnheterAndAwaitingReplyFromFK() {

        FragaSvarFilter filter = new FragaSvarFilter();
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_2_ID);
        filter.getEnhetsIds().add(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVantarPa(VantarPa.SVAR_FRAN_FK);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertEquals(7, fsList.size());
    }

    @Test
    public void testFindFragaSvarStatusesForIntyg() {

        List<FragaSvarStatus> res = fragasvarRepository.findFragaSvarStatusesForIntyg("abc123");

        assertNotNull(res);
        assertEquals(20, res.size());
    }

    @Before
    public void setupTestData() {

        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(1L, FragaSvarTestUtil.ENHET_1_ID, Status.CLOSED, Amne.OVRIGT, WEBCERT, HSA_ID_1,
                "2013-10-01T15:01:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(2L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, Amne.OVRIGT, FK,
                HSA_ID_1, "2013-10-01T15:02:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(3L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_EXTERNAL_ACTION, Amne.OVRIGT, WEBCERT,
                HSA_ID_2, "2013-10-01T15:03:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(4L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_EXTERNAL_ACTION, Amne.AVSTAMNINGSMOTE,
                WEBCERT, HSA_ID_1, "2013-10-01T15:04:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(5L, FragaSvarTestUtil.ENHET_2_ID, Status.PENDING_EXTERNAL_ACTION, Amne.OVRIGT, WEBCERT,
                HSA_ID_1, "2013-10-01T15:04:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(6L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_INTERNAL_ACTION,
                Amne.ARBETSTIDSFORLAGGNING, FK, HSA_ID_1, "2013-10-01T15:05:00", true));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(7L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, Amne.AVSTAMNINGSMOTE,
                FK, HSA_ID_2, "2013-10-01T15:06:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(8L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_EXTERNAL_ACTION, Amne.OVRIGT, WEBCERT,
                HSA_ID_1, "2013-10-01T15:07:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(9L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_EXTERNAL_ACTION, Amne.AVSTAMNINGSMOTE,
                WEBCERT, HSA_ID_1, "2013-10-01T15:08:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(10L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, Amne.KONTAKT, FK,
                HSA_ID_1, "2013-10-01T15:09:00", true));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(11L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, Amne.KONTAKT, FK,
                HSA_ID_2, "2013-10-01T15:10:00", true));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(12L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_INTERNAL_ACTION,
                Amne.AVSTAMNINGSMOTE, FK, HSA_ID_1, "2013-10-01T15:11:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(13L, FragaSvarTestUtil.ENHET_2_ID, Status.PENDING_EXTERNAL_ACTION, Amne.OVRIGT,
                WEBCERT, HSA_ID_1, "2013-10-01T15:11:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(14L, FragaSvarTestUtil.ENHET_1_ID, Status.CLOSED, Amne.OVRIGT, WEBCERT, HSA_ID_1,
                "2013-10-01T15:12:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(15L, FragaSvarTestUtil.ENHET_1_ID, Status.CLOSED, Amne.OVRIGT, FK, HSA_ID_1,
                "2013-10-01T15:13:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(16L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_INTERNAL_ACTION,
                Amne.KOMPLETTERING_AV_LAKARINTYG, FK, HSA_ID_1, "2013-10-01T15:14:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(17L, FragaSvarTestUtil.ENHET_1_ID, Status.ANSWERED, Amne.OVRIGT, WEBCERT, HSA_ID_1,
                "2013-10-01T15:15:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(18L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_INTERNAL_ACTION,
                Amne.MAKULERING_AV_LAKARINTYG, FK, HSA_ID_1, "2013-10-01T15:16:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(19L, FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_INTERNAL_ACTION, Amne.PAMINNELSE, FK,
                HSA_ID_1, "2013-10-01T15:17:00", false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFraga(20L, FragaSvarTestUtil.ENHET_2_ID, Status.PENDING_EXTERNAL_ACTION, Amne.OVRIGT,
                WEBCERT, HSA_ID_2, "2013-10-01T15:18:00", false));
    }

}
