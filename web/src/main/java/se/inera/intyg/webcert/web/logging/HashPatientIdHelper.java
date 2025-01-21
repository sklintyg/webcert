/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.logging;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class HashPatientIdHelper {

    private HashPatientIdHelper() {

    }

    public static String fromUrl(String url) {
        final var pattern = Pattern.compile("\\b\\d{12}\\b");
        final var matcher = pattern.matcher(url);

        if (matcher.find()) {
            final var patientId = matcher.group();
            final var hashedPatientId = hashString(patientId);
            return url.replaceFirst(patientId, hashedPatientId);
        }

        return url;
    }

    private static String hashString(String input) {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

        final var hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        final var hexString = new StringBuilder();
        for (final var b : hashBytes) {
            final var hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
