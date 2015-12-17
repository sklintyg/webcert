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

package se.inera.intyg.webcert.web.integration.validator;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultValidator {

    private final List<String> errors = new ArrayList<>();

    public static ResultValidator newInstance() {
        return new ResultValidator();
    }

    public void addError(String msg) {
        errors.add(msg);
    }

    public void addError(String msgTemplate, String... args) {
        String msg = MessageFormat.format(msgTemplate, (Object[]) args);
        addError(msg);
    }

    public void addErrors(List<String> msgs) {
        errors.addAll(msgs);
    }

    public List<String> getErrorMessages() {
        return errors;
    }

    public String getErrorMessagesAsString() {
        return StringUtils.join(errors, ", ");
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void reset() {
        errors.clear();
    }
}
