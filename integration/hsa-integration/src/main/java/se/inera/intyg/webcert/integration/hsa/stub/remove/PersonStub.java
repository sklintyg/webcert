/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.integration.hsa.stub.remove;

import java.util.ArrayList;
import java.util.List;

/**
 * @author johannesc
 */
public class PersonStub {

    private String hsaId;
    private String name;

    private List<String> medarbetaruppdrag = new ArrayList<>();

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMedarbetaruppdrag() {
        return medarbetaruppdrag;
    }

    public void setMedarbetaruppdrag(List<String> medarbetaruppdrag) {
        this.medarbetaruppdrag = medarbetaruppdrag;
    }

}
