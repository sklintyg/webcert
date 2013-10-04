package se.inera.webcert.web.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.webcert.security.Vardenhet;
import se.inera.webcert.security.Vardgivare;
import se.inera.webcert.security.WebCertUser;
import se.inera.webcert.service.FragaSvarServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class FragaSvarServiceImplTest {

    @Mock
    private FragaSvarRepository fragasvarRepository;

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private FragaSvarServiceImpl service;

    private LocalDateTime JANUARY = new LocalDateTime("2013-01-12T11:22:11");
    private LocalDateTime MAY = new LocalDateTime("2013-05-01T11:11:11");
    private LocalDateTime AUGUST = new LocalDateTime("2013-08-02T11:11:11");
    private LocalDateTime DECEMBER = new LocalDateTime("2014-12-11T10:22:00");

    @SuppressWarnings("unchecked")
    @Test
    public void testFindByEnhetsIdSorting() {
        List<FragaSvar> unsortedList = new ArrayList<FragaSvar>();
        unsortedList.add(buildFragaSvarFraga(1L, MAY, null));
        unsortedList.add(buildFragaSvarFraga(2L, DECEMBER, null));
        unsortedList.add(buildFragaSvarFraga(3L, null, JANUARY));
        unsortedList.add(buildFragaSvarFraga(4L, null, AUGUST));
        when(fragasvarRepository.findByEnhetsId(Mockito.any(List.class))).thenReturn(unsortedList);

        List<FragaSvar> result = service.getFragaSvar(Arrays.asList("123"));

        assertEquals(4, result.size());

        assertEquals(2, (long) result.get(0).getInternReferens());
        assertEquals(4, (long) result.get(1).getInternReferens());
        assertEquals(1, (long) result.get(2).getInternReferens());
        assertEquals(3, (long) result.get(3).getInternReferens());

    }

    @Test
    public void testFindByIntygSorting() {
        List<FragaSvar> unsortedList = new ArrayList<FragaSvar>();
        unsortedList.add(buildFragaSvarFraga(1L, MAY, null));
        unsortedList.add(buildFragaSvarFraga(2L, DECEMBER, null));
        unsortedList.add(buildFragaSvarFraga(3L, null, JANUARY));
        unsortedList.add(buildFragaSvarFraga(4L, null, AUGUST));
        when(fragasvarRepository.findByIntygsReferensIntygsId((Mockito.any(String.class)))).thenReturn(unsortedList);
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        List<FragaSvar> result = service.getFragaSvar("intygsid");

        assertEquals(4, result.size());

        assertEquals(2, (long) result.get(0).getInternReferens());
        assertEquals(4, (long) result.get(1).getInternReferens());
        assertEquals(1, (long) result.get(2).getInternReferens());
        assertEquals(3, (long) result.get(3).getInternReferens());

    }

    private FragaSvar buildFragaSvarFraga(Long id, LocalDateTime fragaSkickadDatum, LocalDateTime svarSkickadDatum) {
        FragaSvar f = new FragaSvar();
        f.setInternReferens(id);
        f.setFrageSkickadDatum(fragaSkickadDatum);
        f.setSvarSkickadDatum(svarSkickadDatum);

        f.setVardperson(new Vardperson());
        f.getVardperson().setEnhetsId("enhet");
        return f;
    }

    @Test
    public void testGetFragaSvarForIntyg() {
        List<FragaSvar> fragaSvarList = new ArrayList<>();
        fragaSvarList.add(buildFragaSvarFraga(1L, new LocalDateTime(), new LocalDateTime()));
        fragaSvarList.add(buildFragaSvarFraga(2L, new LocalDateTime(), new LocalDateTime()));
        fragaSvarList.add(buildFragaSvarFraga(3L, new LocalDateTime(), new LocalDateTime()));

        when(fragasvarRepository.findByIntygsReferensIntygsId("intyg-1")).thenReturn(new ArrayList<>(fragaSvarList));
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        List<FragaSvar> result = service.getFragaSvar("intyg-1");

        verify(fragasvarRepository).findByIntygsReferensIntygsId("intyg-1");
        verify(webCertUserService).getWebCertUser();

        assertEquals(3, result.size());
        assertEquals(fragaSvarList, result);
    }

    @Test
    public void testGetFragaSvarFilteringForIntyg() {
        List<FragaSvar> fragaSvarList = new ArrayList<>();
        fragaSvarList.add(buildFragaSvarFraga(1L, new LocalDateTime(), new LocalDateTime()));
        fragaSvarList.add(buildFragaSvarFraga(2L, new LocalDateTime(), new LocalDateTime()));
        fragaSvarList.add(buildFragaSvarFraga(3L, new LocalDateTime(), new LocalDateTime()));

        // the second question/answer pair was sent to a unit out of the current user's range -> has to be filtered
        fragaSvarList.get(1).getVardperson().setEnhetsId("another unit without my range");

        when(fragasvarRepository.findByIntygsReferensIntygsId("intyg-1")).thenReturn(new ArrayList<>(fragaSvarList));
        when(webCertUserService.getWebCertUser()).thenReturn(webCertUser());

        List<FragaSvar> result = service.getFragaSvar("intyg-1");

        verify(fragasvarRepository).findByIntygsReferensIntygsId("intyg-1");
        verify(webCertUserService).getWebCertUser();

        assertEquals(2, result.size());
        assertEquals(fragaSvarList.get(0), result.get(0));
        assertEquals(fragaSvarList.get(2), result.get(1));
    }

    private WebCertUser webCertUser() {
        WebCertUser user = new WebCertUser();
        user.setVardgivare(new Vardgivare());
        user.getVardgivare().getVardenheter().add(new Vardenhet("enhet", "Enhet"));
        return user;
    }

}
