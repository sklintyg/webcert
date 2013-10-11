package se.inera.webcert.spec.web

import geb.Browser
import se.inera.webcert.spec.web.pages.UnhandledQAPage
import se.inera.webcert.spec.web.pages.WelcomePage
import se.inera.webcert.spec.web.pages.fk7264.ViewCertQAPage
//import se.inera.certificate.web.pages.ArchivedPage

public class SvaraOchFraga {

    public void loggaPåSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor{
                at WelcomePage
            }
            page.userSelect=id

            page.startLogin()
        }
    }


    public boolean obehandladeFragarSidanVisas(){
        Browser.drive {
           waitFor {
               at UnhandledQAPage
           }
        }
    }

    public boolean listaMedObehandladeFragarVisas() {
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                result = page.unhandledQATable.isDisplayed()
            }
        }
        result
    }


    public void visaFraga(String externid) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.showQA(externid)
        }
    }

    public void intygMedFragaSvarSidanVisas() {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
        }
    }

    public boolean svaraPaFragaMedSvar(String internid, String svar) {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.unhandledQAList.isDisplayed();
            }
            page.addAnswerText(internid, svar)

            waitFor {
                page.sendAnswer(internid)
            }

        }
    }

    public boolean stallFraga(String fraga){
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.unhandledQAList.isDisplayed();
            }

            page.initQuestion();

            waitFor {

                page.newQuestionForm.isDisplayed()
            }
            page.addQuestionText(fraga)

            waitFor {
                result = page.sendQuestion()
            }

        }
        result
    }
}
