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

package se.inera.intyg.webcert.integration.hsa.model;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andreaskaltenbach
 */
public class Mottagning extends AbstractVardenhet {

    private static final long serialVersionUID = 6427228467181041893L;

    private LocalDateTime start;
    private LocalDateTime end;
    private String parentHsaId;

    public Mottagning() {
        super();
    }

    public Mottagning(String id, String namn) {
        super(id, namn);
    }

    public Mottagning(String id, String namn, LocalDateTime start, LocalDateTime end) {
        super(id, namn);
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public String getParentHsaId() {
        return parentHsaId;
    }

    public void setParentHsaId(String parentHsaId) {
        this.parentHsaId = parentHsaId;
    }

    @Override
    public List<String> getHsaIds() {
        List<String> ids = new ArrayList<>();
        ids.add(getId());
        return ids;
    }
}
