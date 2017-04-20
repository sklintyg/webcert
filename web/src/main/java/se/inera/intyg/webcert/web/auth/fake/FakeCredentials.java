/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.fake;

import java.io.Serializable;
import java.util.List;

import se.inera.intyg.webcert.web.security.WebCertUserOriginType;

/**
 * @author andreaskaltenbach
 */
public class FakeCredentials implements Serializable {

    private static final long serialVersionUID = -7620199916206349045L;

    private static final String LAKARE = "Läkare";
    private static final String TANDLAKARE = "Tandläkare";

    private String hsaId;
    private String forNamn;
    private String efterNamn;
    private String enhetId;
    private String befattningsKod;
    private String forskrivarKod;
    private String origin = WebCertUserOriginType.NORMAL.name();

    private List<String> legitimeradeYrkesgrupper;

    public FakeCredentials() {
        // Needed for deserialization
    }

    public FakeCredentials(FakeCredentialsBuilder builder) {
        this.hsaId = builder.hsaId;
        this.forNamn = builder.forNamn;
        this.efterNamn = builder.efterNamn;
        this.enhetId = builder.enhetId;
        this.befattningsKod = builder.befattningsKod;
        this.forskrivarKod = builder.forskrivarKod;
        this.origin = builder.origin;
        this.legitimeradeYrkesgrupper = builder.legitimeradeYrkesgrupper;
    }

    public String getBefattningsKod() {
        return befattningsKod;
    }

    public String getEfterNamn() {
        return efterNamn;
    }

    public String getEnhetId() {
        return enhetId;
    }

    public String getForNamn() {
        return forNamn;
    }

    public String getForskrivarKod() {
        return forskrivarKod;
    }

    public String getHsaId() {
        return hsaId;
    }

    public boolean isLakare() {
        if (legitimeradeYrkesgrupper == null) {
            return false;
        }
        return legitimeradeYrkesgrupper.contains(LAKARE);
    }

    public boolean isTandlakare() {
        if (legitimeradeYrkesgrupper == null) {
            return false;
        }
        return legitimeradeYrkesgrupper.contains(TANDLAKARE);
    }

    public String getOrigin() {
        return origin;
    }

    public List<String> getLegitimeradeYrkesgrupper() {
        return legitimeradeYrkesgrupper;
    }

    @Override
    public String toString() {
        return "FakeCredentials{"
                + "hsaId='" + hsaId + '\''
                + ", fornamn='" + forNamn + '\''
                + ", efternamn='" + efterNamn + '\''
                + ", enhetId='" + enhetId + '\''
                + '}';
    }

    public static class FakeCredentialsBuilder {
        private String hsaId;
        private String forNamn;
        private String efterNamn;
        private String enhetId;
        private String befattningsKod;
        private String forskrivarKod;
        private String origin;
        private List<String> legitimeradeYrkesgrupper;

        public FakeCredentialsBuilder(String hsaId, String enhetId) {
            this.hsaId = hsaId;
            this.enhetId = enhetId;
        }

        public FakeCredentialsBuilder forNamn(String forNamn) {
            this.forNamn = forNamn;
            return this;
        }

        public FakeCredentialsBuilder efterNamn(String efterNamn) {
            this.efterNamn = efterNamn;
            return this;
        }

        public FakeCredentialsBuilder forskrivarKod(String forskrivarKod) {
            this.forskrivarKod = forskrivarKod;
            return this;
        }

        public FakeCredentialsBuilder befattningsKod(String befattningsKod) {
            this.befattningsKod = befattningsKod;
            return this;
        }

        public FakeCredentialsBuilder origin(String origin) {
            this.origin = origin;
            return this;
        }

        public FakeCredentialsBuilder legitimeradeYrkesgrupper(List<String> legitimeradeYrkesgrupper) {
            this.legitimeradeYrkesgrupper = legitimeradeYrkesgrupper;
            return this;
        }

        public FakeCredentials build() {
            return new FakeCredentials(this);
        }

    }

}
