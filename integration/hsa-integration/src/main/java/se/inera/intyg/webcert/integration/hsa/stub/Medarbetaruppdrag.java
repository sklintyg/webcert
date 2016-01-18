/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.integration.hsa.stub;

import static java.util.Arrays.asList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Medarbetaruppdrag {

    public static final String VARD_OCH_BEHANDLING = "Vård och behandling";

    private String hsaId;
    private List<Uppdrag> uppdrag;

    public Medarbetaruppdrag() {
    }

    public Medarbetaruppdrag(String hsaId, List<Uppdrag> uppdrag) {
        this.hsaId = hsaId;
        this.uppdrag = uppdrag;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public List<Uppdrag> getUppdrag() {
        return uppdrag;
    }

    public void setUppdrag(List<Uppdrag> uppdrag) {
        this.uppdrag = uppdrag;
    }

    public static class Uppdrag {
        private String vardgivare;
        private String enhet;
        private List<String> andamal;
        public Uppdrag() {
            enhet = "";
            andamal = asList(VARD_OCH_BEHANDLING);
        }
        public Uppdrag(String vardgivare, String enhet) {
            this(vardgivare, enhet, VARD_OCH_BEHANDLING);
        }
        public Uppdrag(String vardgivare, String enhet, String andamal) {
            this.vardgivare = vardgivare;
            this.enhet = enhet;
            this.andamal = asList(andamal);
        }
        public Uppdrag(String vardgivare, String enhet, List<String> andamal) {
            this.vardgivare = vardgivare;
            this.enhet = enhet;
            this.andamal = andamal;
        }
        public String getVardgivare() {
            return vardgivare;
        }
        public void setVardgivare(String vardgivare) {
            this.vardgivare = vardgivare;
        }
        public String getEnhet() {
            return enhet;
        }
        public void setEnhet(String enhet) {
            this.enhet = enhet;
        }
        public List<String> getAndamal() {
            return andamal;
        }
        public void setAndamal(List<String> andamal) {
            this.andamal = andamal;
        }
    }
}
