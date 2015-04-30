package se.inera.certificate.page

import geb.Browser
import geb.Page

abstract class AbstractPage extends Page {

    static boolean doneLoading() {
        boolean result
        Browser.drive {
            result = js.doneLoading && js.dialogDoneLoading && js.rendered
        }
        result
    }

    static void scrollIntoView(elementId){
        def jqScrollToVisible = "jQuery(\'#" + elementId + "\')[0].scrollIntoView();var current=jQuery('body').scrollTop(); jQuery('body').scrollTop(current-400);"
        Browser.drive {
            js.exec(jqScrollToVisible)
        }
    }
}
