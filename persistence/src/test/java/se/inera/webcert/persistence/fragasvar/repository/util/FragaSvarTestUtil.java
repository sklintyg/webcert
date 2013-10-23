package se.inera.webcert.persistence.fragasvar.repository.util;

import org.joda.time.LocalDateTime;
import se.inera.webcert.persistence.fragasvar.model.*;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarFilter;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by pehr on 10/21/13.
 */
public class FragaSvarTestUtil {

    private static long ref_count=1;
    private static LocalDateTime FRAGE_SIGN_DATE = new LocalDateTime("2013-03-01T11:11:11");
    private static LocalDateTime FRAGE_SENT_DATE = new LocalDateTime("2013-03-01T12:00:00");
    private static LocalDateTime SVAR_SIGN_DATE = new LocalDateTime("2013-10-21T11:11:11");
    private static LocalDateTime SVAR_SENT_DATE = new LocalDateTime("2013-10-21T12:00:00");
    private static IntygsReferens INTYGS_REFERENS = new IntygsReferens("abc123", "fk", "Sven Persson",
            FRAGE_SENT_DATE);
    public static String ENHET_1_ID = "ENHET_1_ID";
    private static String ENHET_2_ID = "ENHET_2_ID";
    private static String ENHET_3_ID = "ENHET_3_ID";
    private static String ENHET_4_ID = "ENHET_4_ID";



    public static void buildFragaSvar(FragaSvarFilter filter,int antal, final FragaSvarRepository fragasvarRepository){
        String fragestallare;
        String antifragestallare;
        Status status;
        Status antistatus=Status.CLOSED;
        String hsaid="ingen-vardperson-hsaid";
        LocalDateTime changedFrom=null;
        LocalDateTime antichangedFrom=null;
        LocalDateTime changedTo=null;
        LocalDateTime antichangedTo=null;

        boolean vidarebefordrad = false;

        if (filter.isQuestionFromWC()&&!filter.isQuestionFromFK()) {
            fragestallare="WC";
            antifragestallare="FK";
            status = Status.PENDING_EXTERNAL_ACTION;
        }else{
            fragestallare="FK";
            antifragestallare="WC";
            status=Status.PENDING_INTERNAL_ACTION;
        }

        if(filter.getHsaId()!=null&&!filter.getHsaId().isEmpty()){
            hsaid = filter.getHsaId();
        }

        if(filter.getChangedFrom()!=null){
            changedFrom=filter.getChangedFrom();
            antichangedFrom=new LocalDateTime(changedFrom.getYear(),changedFrom.minusMonths(1).getMonthOfYear(), changedFrom.getDayOfMonth(),0,0) ;

        }
        if(filter.getChangedTo()!=null){
            changedTo=filter.getChangedTo();
            antichangedTo=new LocalDateTime(changedTo.getYear(),changedTo.plusMonths(1).getMonthOfYear(), changedTo.getDayOfMonth(),0,0) ;
        }

        if(filter.getVidarebefordrad()!=null){
             vidarebefordrad=filter.getVidarebefordrad().booleanValue();
        }

        for (int count = 0; count < antal; count++) {
            fragasvarRepository.save(buildFragaSvarFraga("ENHET_1_ID",status,fragestallare,hsaid,changedTo,vidarebefordrad));
        }
        for (int count = 0; count < antal; count++) {
            fragasvarRepository.save(buildFragaSvarFraga("ENHET_1_ID",antistatus,antifragestallare,"ingen-vardperson-hsaid",antichangedTo,!vidarebefordrad));
        }
    }

    public static FragaSvar buildFragaSvarFraga(String enhetsId, Status status, String fragestallare,String hsaId, LocalDateTime fragaSkickad, boolean vidarebefordrad) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<String>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.OVRIGT);
        if (fragestallare.equalsIgnoreCase("FK")) {
            f.setExternReferens("externReferens-"+ref_count);
        } else{
            f.setInternReferens(ref_count);
        }

        f.setFrageSigneringsDatum(FRAGE_SIGN_DATE);

        if (fragaSkickad!=null){
            f.setFrageSkickadDatum(fragaSkickad);

        }else{
            f.setFrageSkickadDatum(FRAGE_SENT_DATE);
        }
        f.setVidarebefordrad(vidarebefordrad);

        f.setFrageStallare(fragestallare);
        Vardperson vardperson = new Vardperson();
        vardperson.setHsaId(hsaId);
        vardperson.setEnhetsId(enhetsId);
        vardperson.setEnhetsnamn(enhetsId + "-namnet");
        f.setVardperson(vardperson);
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setIntygsReferens(INTYGS_REFERENS);
        f.setStatus(status);

        ref_count++;
        return f;
    }

    public static FragaSvar buildFragaSvarSvar(String enhetsId, Status status, String fragestallare,String hsaId) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<String>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.OVRIGT);
        f.setExternReferens("externReferens-"+ref_count);
        f.setInternReferens(ref_count);
        f.setFrageSigneringsDatum(FRAGE_SIGN_DATE);
        f.setFrageSkickadDatum(FRAGE_SENT_DATE);
        f.setSvarSigneringsDatum(SVAR_SIGN_DATE);
        f.setSvarSkickadDatum(SVAR_SENT_DATE);
        f.setFrageStallare(fragestallare);
        Vardperson vardperson = new Vardperson();
        vardperson.setHsaId(hsaId);
        vardperson.setEnhetsId(enhetsId);
        vardperson.setEnhetsnamn(enhetsId + "-namnet");
        f.setVardperson(vardperson);
        f.setFrageText("Detta var ju otydligt formulerat!");
        f.setSvarsText("Då skall vi bättra oss!");
        f.setIntygsReferens(INTYGS_REFERENS);
        f.setStatus(status);

        ref_count++;
        return f;
    }
}
