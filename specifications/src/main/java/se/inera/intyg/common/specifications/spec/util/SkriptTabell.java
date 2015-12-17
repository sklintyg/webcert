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

package se.inera.intyg.common.specifications.spec.util;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.tables.ScriptTable;

public class SkriptTabell extends ScriptTable {

    public SkriptTabell(Table table, String tableId, SlimTestContext context) {
        super(table, tableId, context);
    }

    @Override
    protected String getTableType() {
        return "skriptTabell";
    }

    @Override
    protected String getTableKeyword() {
        return "skript";
    }

    @Override
    protected String getStartKeyword() {
        return "starta";
    }

    @Override
    protected String getCheckKeyword() {
        return "kontrollera att";
    }

    @Override
    protected String getCheckNotKeyword() {
        return "kontrollera att inte";
    }

    @Override
    protected String getEnsureKeyword() {
        return "säkerställ att";
    }

    @Override
    protected String getRejectKeyword() {
        return "säkerställ att inte";
    }

    @Override
    protected String getNoteKeyword() {
        return "notering";
    }

    @Override
    protected String getShowKeyword() {
        return "visa";
    }

}
