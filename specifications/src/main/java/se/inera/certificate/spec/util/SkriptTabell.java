package se.inera.certificate.spec.util;

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
