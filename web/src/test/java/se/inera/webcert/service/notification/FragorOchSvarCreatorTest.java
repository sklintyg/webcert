package se.inera.webcert.service.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.inera.certificate.modules.support.api.notification.FragorOchSvar;
import se.inera.webcert.persistence.fragasvar.model.FragaSvarStatus;
import se.inera.webcert.persistence.fragasvar.model.Status;

public class FragorOchSvarCreatorTest {

    
    private FragorOchSvarCreatorImpl fsCreator = new FragorOchSvarCreatorImpl();
    
    @Test
    public void testPerformCountHan8() {
        
        List<FragaSvarStatus> fsStatuses = Arrays.asList(new FragaSvarStatus(1L, null, Status.PENDING_EXTERNAL_ACTION));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
    }
    
    @Test
    public void testPerformCountHan7() {
        
        List<FragaSvarStatus> fsStatuses = Arrays.asList(new FragaSvarStatus(1L, "Ett svar från FK", Status.ANSWERED));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(1, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
    }
    
    @Test
    public void testPerformCountHan10() {
        
        List<FragaSvarStatus> fsStatuses = Arrays.asList(new FragaSvarStatus(1L, "Ett svar från FK", Status.CLOSED));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(1, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(1, fos.getAntalHanteradeSvar());
    }
    
    @Test
    public void testPerformCountHan6() {
        
        List<FragaSvarStatus> fsStatuses = Arrays.asList(new FragaSvarStatus(1L, null, Status.PENDING_INTERNAL_ACTION));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(0, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
    }
    
    @Test
    public void testPerformCountHan9_Answered() {
        
        List<FragaSvarStatus> fsStatuses = Arrays.asList(new FragaSvarStatus(1L, "Ett svar från WC", Status.CLOSED));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(1, fos.getAntalHanteradeFragor());
        assertEquals(1, fos.getAntalSvar());
        assertEquals(1, fos.getAntalHanteradeSvar());
    }
    
    @Test
    public void testPerformCountHan9_NotAnswered() {
        
        List<FragaSvarStatus> fsStatuses = Arrays.asList(new FragaSvarStatus(1L, null, Status.CLOSED));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(1, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
    }
    
    @Test
    public void testPerformCountFragaToFKNotAnswered() {
        
        List<FragaSvarStatus> fsStatuses = Arrays.asList(new FragaSvarStatus(1L, null, Status.CLOSED));
        FragorOchSvar fos = fsCreator.performCount(fsStatuses);
        assertNotNull(fos);
        assertEquals(1, fos.getAntalFragor());
        assertEquals(1, fos.getAntalHanteradeFragor());
        assertEquals(0, fos.getAntalSvar());
        assertEquals(0, fos.getAntalHanteradeSvar());
    }

}
