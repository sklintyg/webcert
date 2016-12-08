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

package se.inera.intyg.webcert.web.service.dto;

import java.util.*;

public class Lakare {

    private String hsaId;

    private String name;

    public Lakare() {
        // Needed for deserialization
    }

    public Lakare(String hsaId, String name) {
        this.hsaId = hsaId;
        this.name = name;
    }

    public String getHsaId() {
        return hsaId;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hsaId == null) ? 0 : hsaId.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Lakare other = (Lakare) obj;
        if (hsaId == null) {
            if (other.hsaId != null) {
                return false;
            }
        } else if (!hsaId.equals(other.hsaId)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /**
     * Merges two lists of lakare to one.
     *
     * @return the sorted by name list of lakare
     */
    public static List<Lakare> merge(List<Lakare> a, List<Lakare> b) {
        if (a == null && b == null) {
            return new ArrayList<>();
        } else if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        Set<Lakare> lakarSet = new HashSet<>(a);
        lakarSet.addAll(b);
        List<Lakare> res = new ArrayList<>(lakarSet);
        res.sort(Comparator.comparing(Lakare::getName));
        return res;
    }
}
