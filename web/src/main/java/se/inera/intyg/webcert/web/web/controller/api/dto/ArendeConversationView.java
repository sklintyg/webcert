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
package se.inera.intyg.webcert.web.web.controller.api.dto;

import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.LocalDateTime;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
public abstract class ArendeConversationView {

    public abstract ArendeView getFraga();

    @Nullable
    public abstract ArendeView getSvar();

    @Nullable
    public abstract LocalDateTime getSenasteHandelse();

    public abstract ImmutableList<ArendeView> getPaminnelser();

    public static ArendeConversationView create(ArendeView fraga,
            ArendeView svar,
            LocalDateTime senasteHandelse,
            List<ArendeView> paminnelser) {
        return new AutoValue_ArendeConversationView(fraga, svar, senasteHandelse, ImmutableList.copyOf(paminnelser));
    }

}
