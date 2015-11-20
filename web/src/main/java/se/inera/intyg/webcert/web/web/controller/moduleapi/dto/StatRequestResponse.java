package se.inera.intyg.webcert.web.web.controller.moduleapi.dto;


/**
 * Simple DTO for holding statEntries for units and user.
 * {@see StatModuleApiController}
 *
 * @author marced
 */
public class StatRequestResponse {

    private StatEntry unitStat;
    private StatEntry userStat;

    public StatRequestResponse(StatEntry unitStat, StatEntry userStat) {
        super();
        this.unitStat = unitStat;
        this.userStat = userStat;
    }

    public StatRequestResponse() {
    }

    public StatEntry getUnitStat() {
        return unitStat;
    }

    public void setUnitStat(StatEntry unitStat) {
        this.unitStat = unitStat;
    }

    public StatEntry getUserStat() {
        return userStat;
    }

    public void setUserStat(StatEntry userStat) {
        this.userStat = userStat;
    }

}
