package se.inera.webcert.persistence;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public class FragaSvar {
    private String frageStallare;
    private String internReferens;
    private String externReferens;
    private Amne amne;
    private String frageText;
    private LocalDateTime frageSigneringsDatum;
    private LocalDateTime frageSkickadDatum;
    private String svarsText;
    private LocalDateTime svarSigneringsDatum;
    private LocalDateTime svarSkickadDatum;
    private List<String> externaKontakter;
    private String meddelandeRubrik;
    private LocalDate sistaDatumForSvar;

    // Composites
    private IntygsReferens intygsReferens;
    private List<Komplettering> kompletteringar;
    private Vardperson vardperson;
    
    public String getFrageStallare() {
        return frageStallare;
    }
    public void setFrageStallare(String frageStallare) {
        this.frageStallare = frageStallare;
    }
    public String getInternReferens() {
        return internReferens;
    }
    public void setInternReferens(String internReferens) {
        this.internReferens = internReferens;
    }

    public String getExternReferens() {
        return externReferens;
    }

    public void setExternReferens(String externReferens) {
        this.externReferens = externReferens;
    }

    public Amne getAmne() {
        return amne;
    }
    public void setAmne(Amne amne) {
        this.amne = amne;
    }
    public String getFrageText() {
        return frageText;
    }
    public void setFrageText(String frageText) {
        this.frageText = frageText;
    }
    public LocalDateTime getFrageSigneringsDatum() {
        return frageSigneringsDatum;
    }
    public void setFrageSigneringsDatum(LocalDateTime frageSigneringsDatum) {
        this.frageSigneringsDatum = frageSigneringsDatum;
    }
    public LocalDateTime getFrageSkickadDatum() {
        return frageSkickadDatum;
    }
    public void setFrageSkickadDatum(LocalDateTime frageSkickadDatum) {
        this.frageSkickadDatum = frageSkickadDatum;
    }
    public String getSvarsText() {
        return svarsText;
    }
    public void setSvarsText(String svarsText) {
        this.svarsText = svarsText;
    }
    public LocalDateTime getSvarSigneringsDatum() {
        return svarSigneringsDatum;
    }
    public void setSvarSigneringsDatum(LocalDateTime svarSigneringsDatum) {
        this.svarSigneringsDatum = svarSigneringsDatum;
    }
    public LocalDateTime getSvarSkickadDatum() {
        return svarSkickadDatum;
    }
    public void setSvarSkickadDatum(LocalDateTime svarSkickadDatum) {
        this.svarSkickadDatum = svarSkickadDatum;
    }
    public List<String> getExternaKontakter() {
        return externaKontakter;
    }
    public void setExternaKontakter(List<String> externaKontakter) {
        this.externaKontakter = externaKontakter;
    }
    public String getMeddelandeRubrik() {
        return meddelandeRubrik;
    }
    public void setMeddelandeRubrik(String meddelandeRubrik) {
        this.meddelandeRubrik = meddelandeRubrik;
    }
    public LocalDate getSistaDatumForSvar() {
        return sistaDatumForSvar;
    }
    public void setSistaDatumForSvar(LocalDate sistaDatumForSvar) {
        this.sistaDatumForSvar = sistaDatumForSvar;
    }
    public IntygsReferens getIntygsReferens() {
        return intygsReferens;
    }
    public void setIntygsReferens(IntygsReferens intygsReferens) {
        this.intygsReferens = intygsReferens;
    }

    public List<Komplettering> getKompletteringar() {
        return kompletteringar;
    }

    public void setKompletteringar(List<Komplettering> kompletteringar) {
        this.kompletteringar = kompletteringar;
    }

    public Vardperson getVardperson() {
        return vardperson;
    }
    public void setVardperson(Vardperson vardperson) {
        this.vardperson = vardperson;
    }

}
