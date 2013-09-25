package se.inera.webcert.hsa.stub;

import java.util.ArrayList;
import java.util.List;

/**
 * @author johannesc
 */
public class VardgivarStub {

    private List<HsaUnitStub> enheter = new ArrayList();

    public List<HsaUnitStub> getEnheter() {
        return enheter;
    }

    public void setEnheter(List<HsaUnitStub> enheter) {
        this.enheter = enheter;
    }
}
