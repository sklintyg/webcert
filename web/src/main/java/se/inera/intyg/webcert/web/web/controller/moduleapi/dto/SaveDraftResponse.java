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

package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;

import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessageType;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;

import java.util.ArrayList;
import java.util.List;

public class SaveDraftResponse {

    private long version;

    private UtkastStatus status;

    private List<SaveDraftValidationMessage> messages = new ArrayList<>();

    public SaveDraftResponse(long version, UtkastStatus status) {
        this.version = version;
        this.status = status;
    }

    public UtkastStatus getStatus() {
        return status;
    }

    public void setStatus(UtkastStatus status) {
        this.status = status;
    }

    public List<SaveDraftValidationMessage> getMessages() {
        return messages;
    }

    public void getMessage(List<SaveDraftValidationMessage> messages) {
        this.messages = messages;
    }

    public void addMessage(String field, ValidationMessageType type, String message) {
        messages.add(new SaveDraftValidationMessage(field, type, message));
    }

    public long getVersion() {
        return version;
    }
}
