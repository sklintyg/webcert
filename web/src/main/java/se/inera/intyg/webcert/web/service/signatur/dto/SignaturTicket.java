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
package se.inera.intyg.webcert.web.service.signatur.dto;

import java.time.LocalDateTime;

public class SignaturTicket {

    /**
     * NetID/Siths uses BEARBETAR, SIGNERAD and OKAND. BEARBETAR from start to the signing either succeeds (SIGNERAD)
     * or fails (OKAND)
     *
     * BankID / Mobil BankID uses BEARBETAR from start until BankID application (mobile or not) has established
     * connection
     * to the BankID server. Then the state is changed to VANTA_SIGN. If no connection between application and bank id
     * server were established in 6 GRP collect requests (according to spec, 18 seconds), the NO_CLIENT state is set.
     * SIGNERAD / OKAND is used similar to NetID after this.
     */
    public enum Status {
        BEARBETAR,
        VANTA_SIGN,
        SIGNERAD,
        NO_CLIENT,
        OKAND
    }

    private final String id;
    private final long pagaendeSigneringId;
    private final Status status;
    private final String intygsId;
    private final long version;
    private final String hash;
    private final LocalDateTime timestamp;
    private final LocalDateTime signeringstid;

    // CHECKSTYLE:OFF ParameterNumberCheck
    @java.lang.SuppressWarnings("squid:S00107") // Parameter number check ignored in Sonar
    public SignaturTicket(String id, long pagaendeSigneringId, Status status, String intygsId, long version, LocalDateTime signeringstid,
            String hash,
            LocalDateTime timestamp) {
        this.id = id;
        this.pagaendeSigneringId = pagaendeSigneringId;
        this.status = status;
        this.intygsId = intygsId;
        this.version = version;
        this.hash = hash;
        this.timestamp = timestamp;
        this.signeringstid = signeringstid;
    }
    // CHECKSTYLE:ON ParameterNumberCheck

    public String getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public String getIntygsId() {
        return intygsId;
    }

    public long getVersion() {
        return version;
    }

    public LocalDateTime getSigneringstid() {
        return signeringstid;
    }

    public String getHash() {
        return hash;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public long getPagaendeSigneringId() {
        return pagaendeSigneringId;
    }

    public SignaturTicket withStatus(Status status) {
        return new SignaturTicket(id, pagaendeSigneringId, status, intygsId, version, signeringstid, hash, LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "SignatureTicket [ id:" + id + " intyg:" + intygsId + " status: " + status + " ]";
    }

}
