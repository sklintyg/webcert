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
package se.inera.intyg.webcert.specifications.spec.util.screenshot;

import com.google.common.html.HtmlEscapers;

/**
 * Exception in Slim fixture.
 */
public class SlimFixtureException extends RuntimeException {

    /**
     * Creates new.
     * @param message message for exception.
     */
    public SlimFixtureException(String message) {
        this(true, message);
    }

    /**
     * Creates new.
     * @param stackTraceInWiki whether wiki should include the stack trace of this exception, or just the message
     * @param message message for exception.
     */
    public SlimFixtureException(boolean stackTraceInWiki, String message) {
        super(createMessage(stackTraceInWiki, message));
    }

    /**
     * Creates new.
     * @param message message for exception.
     * @param cause underlying exception.
     */
    public SlimFixtureException(String message, Throwable cause) {
        this(true, message, cause);
    }

    /**
     * Creates new.
     * @param stackTraceInWiki whether wiki should include the stack trace of this exception, or just the message
     * @param message message for exception.
     * @param cause underlying exception.
     */
    public SlimFixtureException(boolean stackTraceInWiki, String message, Throwable cause) {
        super(createMessage(stackTraceInWiki, message), cause);
    }

    /**
     * Creates new.
     * @param cause underlying exception.
     */
    public SlimFixtureException(Throwable cause) {
        super(cause);
    }

    private static String createMessage(boolean stackTraceInWiki, String message) {
        String result = message;
        if (!stackTraceInWiki) {
            // Until https://github.com/unclebob/fitnesse/issues/731 is fixed
            if (result.contains("\n")) {
                if (!result.startsWith("<") || !result.endsWith(">")) {
                    // it is not yet HTML, make it HTML so we can use <br/>
                    result = String.format("<div>%s</div>", HtmlEscapers.htmlEscaper().escape(result));
                }
                result = result.replaceAll("(\\r)?\\n", "<br/>");
            }
            return String.format("message:<<%s>>", result);
        }
        return result;
    }

}
