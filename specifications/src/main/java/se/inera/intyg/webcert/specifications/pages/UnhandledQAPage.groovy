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

package se.inera.intyg.webcert.specifications.pages

class UnhandledQAPage extends AbstractLoggedInPage {

    static url = "/web/dashboard#/unhandled-qa"
    static at = { doneLoading() && $("#unhandled-qa").isDisplayed() }

    static content = {
        unitstatUnhandledQuestionsBadgde(required: false, wait: true) { $("#stat-unitstat-unhandled-question-count") }
        careUnitSelector(required: false) { $("a#wc-care-unit-clinic-selector") }
        careUnitSelectorLink(required: false) { $("a#wc-care-unit-clinic-selector-link") }
        careUnitSelectorNoWait(required: false) { $("div#wc-care-unit-clinic-selector")}
        careUnitModal(required: false, wait: true) { $("a#wc-care-unit-clinic-selector-link") }
        careUnitModalBody(required: false, wait: true) { $(".modal-body") }
        unhandledQATable(required: false) { $("#qaTable") }

        noResultsOnUnitInfo { $("#current-list-noResults-unit") }
        noResultsForQueryInfo { $("#current-list-noResults-query") }

        advancedFilterBtn { $("#show-advanced-filter-btn") }
        advancedFilterForm { $("#advanced-filter-form") }
        advandecFilterFormFragestallare { $("input", name: "frageStallare") }
        advancedFilterSelectDoctor(required: false) { $("#qp-lakareSelector") }
        advancedFilterVidarebefordrad { $("input", name: "vidarebefordrad") }
        advancedFilterChangeDateFrom(required: false) { $("#filter-changedate-from") }
        advancedFilterChangeDateTo(required: false) { $("#filter-changedate-to") }
        advancedFilterStatus { $("#qp-showStatus") }
        advancedFilterSearchBtn { $("#filter-qa-btn") }
        advancedFilterResetBtn  { $("#reset-search-form") }

        filterVidarebefordrad(required: false) { $("#filterFormVidarebefordrad") }
        filterSparatAv(required: false) { $("#filterFormSparatAv") }
        filterSigneratAv(required: false) { $("#filterFormSigneratAv") }

        visaAllaFragaBtn(required: false) { $("#select-active-unit-wc-all") }
        vcCentrumVastBtn(required: false) { $("select-active-unit-centrum-vast") }
        fetchMoreBtn { $("#hamtaFler") }

        visaFragaBtn(required: false) {internReferens -> $("#showqaBtn-${internReferens}")}

        patientId() {internReferens -> $("#patientId-${internReferens}")}
    }

    void visaAllaFragor() {
        visaAllaFragaBtn.click();
        waitFor {
            doneLoading()
        }
    }

    void showQA(String internReferens) {
        visaFragaBtn(internReferens).click()
        waitFor {
            doneLoading()
        }
    }

    boolean isQAVisible(String internid) {
        visaFragaBtn(internid)?.isDisplayed()
    }

    void showAdvancedFilter() {
        advancedFilterBtn.click()
        waitFor {
            doneLoading()
        }
    }

    void resetAdvancedFilter() {
        advancedFilterResetBtn.click()
        waitFor {
            doneLoading()
        }
    }

    boolean patientIdSyns(String internReferens) {
        patientId(internReferens)?.text() != ""
    }

    void hamtaFler() {
        if (fetchMoreBtn.isDisplayed()) {
            fetchMoreBtn.click()
        }
        waitFor {
            doneLoading()
        }
    }

}
