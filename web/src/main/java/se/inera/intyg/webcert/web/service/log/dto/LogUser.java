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

package se.inera.intyg.webcert.web.service.log.dto;


public class LogUser {

    private String userId;
    private String userName;
    private String enhetsId;
    private String enhetsNamn;
    private String vardgivareId;
    private String vardgivareNamn;

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getEnhetsId() {
        return enhetsId;
    }
    public void setEnhetsId(String enhetsId) {
        this.enhetsId = enhetsId;
    }
    public String getEnhetsNamn() {
        return enhetsNamn;
    }
    public void setEnhetsNamn(String enhetsNamn) {
        this.enhetsNamn = enhetsNamn;
    }
    public String getVardgivareId() {
        return vardgivareId;
    }
    public void setVardgivareId(String vardgivareId) {
        this.vardgivareId = vardgivareId;
    }
    public String getVardgivareNamn() {
        return vardgivareNamn;
    }
    public void setVardgivareNamn(String vardgivareNamn) {
        this.vardgivareNamn = vardgivareNamn;
    }

}
