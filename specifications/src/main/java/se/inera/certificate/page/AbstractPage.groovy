package se.inera.certificate.page

import geb.Browser
import geb.Page

abstract class AbstractPage extends Page {

    def doneLoading() {
        js.doneLoading && js.dialogDoneLoading
    }

    def static scrollIntoView(elementId){
        def jqScrollToVisible = "jQuery(\'#" + elementId + "\')[0].scrollIntoView();var current=jQuery('body').scrollTop(); jQuery('body').scrollTop(current-400);"
        println("------------ scrollintoview")
        println(jqScrollToVisible)
        Browser.drive {
            js.exec(jqScrollToVisible)
        }
    }
}
