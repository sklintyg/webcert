package se.inera.webcert.spec.fk7263

import org.codehaus.groovy.runtime.StackTraceUtils

import se.inera.webcert.spec.Browser
import se.inera.webcert.pages.fk7263.EditCertPage

class VerifieraUtkastFk7263 {
    public VerifieraUtkastFk7263() {
    }

    String getCurrentMethodName(){
        def marker = new Throwable()
        return StackTraceUtils.sanitize(marker).stackTrace[1].methodName
    }

    String getStringResult(module, field) {
        def result = ''
        if (module != null) {
            Browser.drive {
                if (!page."$module"."$field".isDisplayed()) {
                    result = "notshown"
                } else {
                    result = page."$module"."$field".text()
                }
            }
        } else {
            Browser.drive {
                if (!page."$field".isDisplayed()) {
                    result = "notshown"
                } else {
                    result = page."$field".text()
                }
            }
        }
        result
    }
    
    String getValueResult(module, field) {
        def result = ''
        if (module != null) {
            Browser.drive {
                if (!page."$module"."$field".isDisplayed()) {
                    result = "notshown"
                } else {
                    result = page."$module"."$field".value()
                }
            }
        } else {
            Browser.drive {
                if (!page."$field".isDisplayed()) {
                    result = "notshown"
                } else {
                    result = page."$field".value()
                }
            }
        }
        result
    }


    boolean getBooleanResult(field) {
        def result = false
        Browser.drive {
            result = page."$field".isDisplayed()
        }
        result
    }

    String diagnos1() {
        getValueResult("diagnos", getCurrentMethodName())
    }
    
    String diagnos1Text() {
        getValueResult("diagnos", getCurrentMethodName())
    }
    String diagnos2() {
        getValueResult("diagnos", getCurrentMethodName())
    }
    String diagnos2Text() {
        getValueResult("diagnos", getCurrentMethodName())
    }
    String diagnos3() {
        getValueResult("diagnos", getCurrentMethodName())
    }
    String diagnos3Text() {
        getValueResult("diagnos", getCurrentMethodName())
    }
    String fortydligande() {
        getValueResult("diagnos", getCurrentMethodName())
    }
    String beskrivning() {
        getValueResult("prognos", getCurrentMethodName())
    }
    String annanReferensBeskrivning() {
        getValueResult("baserasPa", "otherText")
    }

    String sjukdomsforlopp() {
        getValueResult(null, getCurrentMethodName())
    }
    

}
