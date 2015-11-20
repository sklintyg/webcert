package se.inera.intyg.webcert.web.service.util;

import java.util.Comparator;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * Compare senasteHandelseDatum (latest first) for two FragaSvar entities in a null safe manner.
 *
 * @author marced
 */
public class FragaSvarSenasteHandelseDatumComparator implements Comparator<FragaSvar> {

    @Override
    public int compare(FragaSvar f1, FragaSvar f2) {
        if (f1.getSenasteHandelseDatum() == null && f2.getSenasteHandelseDatum() == null) {
            return 0;
        } else if (f1.getSenasteHandelseDatum() == null) {
            return -1;
        } else if (f2.getSenasteHandelseDatum() == null) {
            return 1;
        } else {
            return f2.getSenasteHandelseDatum().compareTo(f1.getSenasteHandelseDatum());
        }
    }
}
