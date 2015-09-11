package se.inera.certificate.page

import se.inera.certificate.spec.Browser
import geb.Page

abstract class AbstractPage extends Page {

    static content = {
        modalBackdrop(required:false) {$('.modal-backdrop')}
    }

    static boolean doneLoading() {
        boolean result
        Browser.drive {
            println('js.doneLoading:'+js.doneLoading+', js.dialogDoneLoading:' + js.dialogDoneLoading)
            result = js.doneLoading && js.dialogDoneLoading
        }
        result
    }

    static void scrollIntoView(elementId){
        def jqScrollToVisible = "jQuery(\'#" + elementId + "\')[0].scrollIntoView();var current=jQuery('body').scrollTop(); jQuery('body').scrollTop(current-400);"
        Browser.drive {
            js.exec(jqScrollToVisible)
        }
    }

    def waitForModalBackdropToHide(){
        waitFor() {
            return !modalBackdrop.isDisplayed();
        }
    }

    static boolean isButtonDisabled(button){
        return button.@disabled == 'true';
    }

    def elementForId(elementId){
        return $("#" + elementId);
    }

    def elementForClass(classId){
        return $("." + classId);
    }

    // use inside content definitions to prevent wait success until the element is displayed
    // with the option element(wait:true){ displayed($('#element-id')) }
    static displayed(elem) {
        (elem?.displayed) ? elem : null
    }

}
