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
    private static LocalDateTime FRAGE_SIGN_DATE = new LocalDateTime("2012-03-01T11:11:11");
    private static LocalDateTime FRAGE_SENT_DATE = new LocalDateTime("2012-03-01T12:00:00");
    private static LocalDateTime SVAR_SIGN_DATE = new LocalDateTime("2014-10-21T11:11:11");
    private static LocalDateTime SVAR_SENT_DATE = new LocalDateTime("2014-10-21T12:00:00");
    private static IntygsReferens INTYGS_REFERENS = new IntygsReferens("abc123", "fk", "Sven Persson",
            FRAGE_SENT_DATE);
    public static String ENHET_1_ID = "ENHET_1_ID";
    private static String ENHET_2_ID = "ENHET_2_ID";
    private static String ENHET_3_ID = "ENHET_3_ID";
    private static String ENHET_4_ID = "ENHET_4_ID";

    /**
     * Populates the database with "antal" FragaSvar that matches and "antal" that doesn't match the filter.
     * @param filter the filter the FragaSvar should be built from.
     * @param antal the number of items that should match, and not match the filter
     * @param fragasvarRepository
     */
    public static void populateFragaSvar (FragaSvarFilter filter,int antal, final FragaSvarRepository fragasvarRepository){
        for (int count = 0; count < antal; count++) {
            FragaSvar[] fsArray = buildFragaSvarFromFilter(filter);

            fragasvarRepository.save(fsArray[0]);
            fragasvarRepository.save(fsArray[1]);
        }
    }

    /**
     * Creates a FragaSvar that matches the criteria in the supplied filter.
     * It also creates a FragaSvar that doesn't match the filter.
     * @param filter
     * @return  an array of 2 FragaSvar, where the first item matches the filter and the second doesn't.
     */
    public static FragaSvar[] buildFragaSvarFromFilter(FragaSvarFilter filter){
        FragaSvar[] fs = new FragaSvar[2];

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
            //Make the date 1 month earlier
            antichangedFrom=new LocalDateTime(changedFrom.getYear(),changedFrom.minusMonths(1).getMonthOfYear(), changedFrom.getDayOfMonth(),0,0) ;

        }
        if(filter.getChangedTo()!=null){
            changedTo=filter.getChangedTo();
            //Make the date 1 month later
            antichangedTo=new LocalDateTime(changedTo.getYear(),changedTo.plusMonths(1).getMonthOfYear(), changedTo.getDayOfMonth(),0,0) ;
        }

        if(filter.getVidarebefordrad()!=null){
             vidarebefordrad=filter.getVidarebefordrad().booleanValue();
        }


        if(filter.getChangedTo()!=null){
            fs[0] = buildFragaSvarSvar("ENHET_1_ID",status,fragestallare,hsaid,changedFrom,changedTo,vidarebefordrad);
            fs[1] = buildFragaSvarSvar("ENHET_1_ID",antistatus,antifragestallare,"ingen-vardperson-hsaid",antichangedFrom,antichangedTo,!vidarebefordrad);
        }else {
            fs[0] = buildFragaSvarFraga("ENHET_1_ID",status,fragestallare,hsaid,changedFrom,vidarebefordrad);
            fs[1] = buildFragaSvarFraga("ENHET_1_ID",antistatus,antifragestallare,"ingen-vardperson-hsaid",antichangedFrom,!vidarebefordrad);

        }

        return fs;
    }

    /**
     * Builds a FragaSvara, a question without reply, from the supplied params.
     *
     * @param enhetsId
     * @param status
     * @param fragestallare
     * @param hsaId
     * @param fragaSkickad
     * @param vidarebefordrad
     * @return
     */
    public static FragaSvar buildFragaSvarFraga(String enhetsId, Status status, String fragestallare,String hsaId, LocalDateTime fragaSkickad,  boolean vidarebefordrad) {
        FragaSvar f = new FragaSvar();
        f.setExternaKontakter(new HashSet<String>(Arrays.asList("KONTAKT1", "KONTAKT2", "KONTAKT3")));
        f.setAmne(Amne.OVRIGT);
        if (fragestallare.equalsIgnoreCase("FK")) {
            f.setExternReferens("externReferens-"+ref_count);
        } else{
            f.setInternReferens(ref_count);
        }



        if (fragaSkickad!=null){
            f.setFrageSkickadDatum(fragaSkickad);
            f.setFrageSigneringsDatum(fragaSkickad);

        }else{
            f.setFrageSkickadDatum(FRAGE_SENT_DATE);
            f.setFrageSigneringsDatum(FRAGE_SIGN_DATE);
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

    /**
     * Builds a FragaSvara, a question with reply, from the supplied params.
     * @param enhetsId
     * @param status
     * @param fragestallare
     * @param hsaId
     * @param fragaSkickad
     * @param svarSkickad
     * @param vidarebefordrad
     * @return
     */
    public static FragaSvar buildFragaSvarSvar(String enhetsId, Status status, String fragestallare,String hsaId, LocalDateTime fragaSkickad, LocalDateTime svarSkickad, boolean vidarebefordrad) {

        FragaSvar f = buildFragaSvarFraga(enhetsId,status,fragestallare,hsaId,fragaSkickad, vidarebefordrad);

        f.setSvarSigneringsDatum(svarSkickad);
        f.setSvarSkickadDatum(svarSkickad);
        f.setSvarsText("Ett svar");

        if (svarSkickad!=null){
            f.setSvarSkickadDatum(fragaSkickad);
            f.setSvarSigneringsDatum(fragaSkickad);

        }else{
            f.setSvarSkickadDatum(SVAR_SENT_DATE);
            f.setSvarSigneringsDatum(SVAR_SIGN_DATE);
        }
        return f;
    }
}
