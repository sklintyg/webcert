/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.mail.stubs;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CareUnitStub extends AbstractUnitStub {
    private static final long serialVersionUID = 460424685142490104L;
    private LocalDateTime start;
    private LocalDateTime end;
    private List<SubUnitStub> subUnits;

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof CareUnitStub)) {
            return false;
        } else {
            CareUnitStub other = (CareUnitStub)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (!super.equals(o)) {
                return false;
            } else {
                label49: {
                    Object this$start = this.getStart();
                    Object other$start = other.getStart();
                    if (this$start == null) {
                        if (other$start == null) {
                            break label49;
                        }
                    } else if (this$start.equals(other$start)) {
                        break label49;
                    }

                    return false;
                }

                Object this$end = this.getEnd();
                Object other$end = other.getEnd();
                if (this$end == null) {
                    if (other$end != null) {
                        return false;
                    }
                } else if (!this$end.equals(other$end)) {
                    return false;
                }

                Object this$subUnits = this.getSubUnits();
                Object other$subUnits = other.getSubUnits();
                if (this$subUnits == null) {
                    return other$subUnits == null;
                } else {
                    return this$subUnits.equals(other$subUnits);
                }
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof CareUnitStub;
    }

    public int hashCode() {
        int result = super.hashCode();
        Object $start = this.getStart();
        result = result * 59 + ($start == null ? 43 : $start.hashCode());
        Object $end = this.getEnd();
        result = result * 59 + ($end == null ? 43 : $end.hashCode());
        Object $subUnits = this.getSubUnits();
        result = result * 59 + ($subUnits == null ? 43 : $subUnits.hashCode());
        return result;
    }

    public CareUnitStub() {
    }

    public String toString() {
        LocalDateTime var10000 = this.getStart();
        return "CareUnitStub(start=" + var10000 + ", end=" + this.getEnd() + ", subUnits=" + this.getSubUnits() + ")";
    }
}
