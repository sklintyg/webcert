package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage
import se.inera.certificate.spec.Browser

class UnhandledQAPage extends AbstractPage {

    static at = { doneLoading() && $("#unhandled-qa").isDisplayed() }

    static content = {
        unitstatUnhandledQuestionsBadgde(required: false, wait: true) {
            displayed($("#stat-unitstat-unhandled-question-count"))
        }
        careUnitSelector(required: false, wait: true) { $("a#wc-care-unit-clinic-selector") }
        careUnitSelectorLink(required: false, wait: true) { $("a#wc-care-unit-clinic-selector-link") }
        careUnitSelectorNoWait(required: false) { $("div#wc-care-unit-clinic-selector")}
        careUnitModal(required: false, wait: true) { displayed($("a#wc-care-unit-clinic-selector-link")) }
        careUnitModalBody(required: false, wait: true) { displayed($(".modal-body")) }
        unhandledQATable(required: false, wait: true) { displayed($("#qaTable")) }
        unhandledQATableNoWait(required: false) { $("#qaTable") }

        noResultsOnUnitInfo(wait: true) { displayed($("#current-list-noResults-unit")) }
        noResultsOnUnitInfoNoWait{ $("#current-list-noResults-unit") }
        noResultsForQueryInfo(wait: true) { displayed($("#current-list-noResults-query")) }
        noResultsForQueryInfoNoWait{ $("#current-list-noResults-query") }

        advancedFilterBtn(wait: true) { displayed($("#show-advanced-filter-btn")) }
        advancedFilterForm(wait: true) { displayed($("#advanced-filter-form")) }
        advandecFilterFormFragestallare(wait: true) { displayed($("input", name: "frageStallare")) }
        advancedFilterSelectDoctor(required: false, wait: true) { displayed($("#qp-lakareSelector")) }
        advancedFilterVidarebefordrad(wait: true) { displayed($("input", name: "vidarebefordrad")) }
        advancedFilterChangeDateFrom(required: false, wait: true) { displayed($("#filter-changedate-from")) }
        advancedFilterChangeDateTo(required: false, wait: true) { displayed($("#filter-changedate-to")) }
        advancedFilterStatus(wait: true) { displayed($("#qp-showStatus")) }
        advancedFilterSearchBtn(wait: true) { displayed($("#filter-qa-btn")) }
        advancedFilterResetBtn(wait: true) { displayed($("#reset-search-form")) }
        visaAllaFragaBtn(required: false, wait: true) { displayed($("#select-active-unit-wc-all")) }
        vcCentrumVastBtn(required: false) { $("select-active-unit-centrum-vast") }
        fetchMoreBtn { $("#hamtaFler") }

        logoutLink { $("#logoutLink") }
    }

    def visaAllaFragor() {
        visaAllaFragaBtn.click();
    }

    def selectCareUnit(String careUnit) {
        Browser.drive {
            waitFor {
                $("#select-active-unit-${careUnit}").click()
            }
        }
    }

    boolean isCareUnitVisible(String careUnit, boolean expected) {
        Browser.drive {
            def ref = "#select-active-unit-${careUnit}";
            if (expected) {
                waitFor {
                    return $(ref).isDisplayed()
                }
            } else {
                return !$(ref).isDisplayed()
            }
        }
    }

    def clickCareUnitModal() {
        careUnitModal.click();
    }

    def expandEnhetModal(String id) {
        Browser.drive {
            waitFor {
                $("#expand-enhet-${id}").click()
            }
        }
    }

    def modalIsDisplayed() {
        careUnitModalBody.isDisplayed();
    }

    def isCareUnitModalVisible(String careUnit, boolean expected) {
        Browser.drive {
            def ref = "#select-active-unit-${careUnit}-modal"
            if (expected) {
                waitFor {
                    $(ref).isDisplayed()
                }
            } else {
                !$(ref).isDisplayed()
            }
        }
    }

    def selectCareUnitModal(String careUnit) {
        Browser.drive {
            waitFor {
                $("#select-active-unit-${careUnit}-modal").click()
            }
        }
    }

    boolean isNumberPresent(String careUnit, String expected) {
        Browser.drive {
            waitFor {
                expected == $("#select-active-unit-${careUnit}").find(".qa-circle").text()
            }
        }
    }

    boolean isNumberPresentInModal(String careUnit, String expected) {
        Browser.drive {
            waitFor {
                expected == $("#fraga-svar-stat-${careUnit}").text()
            }
        }
    }

    def showQA(String internReferens) {
        Browser.drive {
            waitFor {
                $("#showqaBtn-${internReferens}").click()
            }
        }
    }

    def showQANoWait(String internReferens) {
        Browser.drive {
            $("#showqaBtn-${internReferens}")
        }
    }

    def isQAVisible(String internid, boolean expected) {
        Browser.drive {
            def ref = "#showqaBtn-${internid}"
            if (expected) {
                waitFor {
                    $(ref).isDisplayed()
                }
            } else {
                !$(ref).isDisplayed()
            }
        }
    }

    def showAdvancedFilter() {
        advancedFilterBtn.click()
    }

    def resetAdvancedFilter() {
        advancedFilterResetBtn.click()
    }

    def patientIdSyns(String internReferens) {
        Browser.drive {
            waitFor {
                def patientId = $("#patientId-${internReferens}")
                patientId.text() != ""
            }

        }
    }

    boolean hamtaFler() {
        if (fetchMoreBtn.isDisplayed()) {
            fetchMoreBtn.click()
        }
        return true
    }

    def logout() {
        logoutLink.click()
    }

}
