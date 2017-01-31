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
package se.inera.intyg.webcert.web.service.log.dto;

/**
 * Immutable representation of a Webcert user for PDL logging purposes.
 *
 * @author mekstrand
 */
public final class LogUser {

    private final String userId;
    private final String userName;
    private final String userAssignment;
    private final String userTitle;
    private final String enhetsId;
    private final String enhetsNamn;
    private final String vardgivareId;
    private final String vardgivareNamn;

    private LogUser(Builder builder) {
        this.userId = builder.userId;
        this.userName = builder.userName;
        this.userAssignment = builder.userAssignment;
        this.userTitle = builder.userTitle;
        this.enhetsId = builder.enhetsId;
        this.enhetsNamn = builder.enhetsNamn;
        this.vardgivareId = builder.vardgivareId;
        this.vardgivareNamn = builder.vardgivareNamn;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserAssignment() {
        return userAssignment;
    }

    public String getUserTitle() {
        return userTitle;
    }

    public String getEnhetsId() {
        return enhetsId;
    }

    public String getEnhetsNamn() {
        return enhetsNamn;
    }

    public String getVardgivareId() {
        return vardgivareId;
    }

    public String getVardgivareNamn() {
        return vardgivareNamn;
    }


    public static class Builder {

        private String userId;
        private String userName;
        private String userAssignment;
        private String userTitle;
        private String enhetsId;
        private String enhetsNamn;
        private String vardgivareId;
        private String vardgivareNamn;

        /**
         * Enligt tjänstekontraktsbeskrivningen ska det i anrop till tjänsten "StoreLog"
         * komma information om användaren som är upphov till loggposten. De fält som är
         * obligatoriska är användarens id, vårdenhetens id och vårdgivarens id.
         *
         * Se https://bitbucket.org/rivta-domains/riv.ehr.log/raw/master/docs/TKB_ehr_log.docx
         *
         * @param userId
         *          HsaId of the logged in user.
         * @param enhetsId
         *          HsaId of the enhet.
         * @param vardgivareId
         *          HsaId of the vardgivare.
         */
        public Builder(String userId, String enhetsId, String vardgivareId) {
            if (userId == null || enhetsId == null || vardgivareId == null) {
                throw new IllegalArgumentException("LogUser builder requires all constructor arguments to be non-null");
            }
            this.userId = userId;
            this.enhetsId = enhetsId;
            this.vardgivareId = vardgivareId;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder userAssignment(String userAssignment) {
            this.userAssignment = userAssignment;
            return this;
        }

        public Builder userTitle(String userTitle) {
            this.userTitle = userTitle;
            return this;
        }

        public Builder enhetsNamn(String enhetsNamn) {
            this.enhetsNamn = enhetsNamn;
            return this;
        }

        public Builder vardgivareNamn(String vardgivareNamn) {
            this.vardgivareNamn = vardgivareNamn;
            return this;
        }

        public LogUser build() {
            return new LogUser(this);
        }
    }

}
