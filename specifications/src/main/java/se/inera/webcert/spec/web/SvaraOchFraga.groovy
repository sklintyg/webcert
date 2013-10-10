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

    public boolean svaraPaFraga(String internid) {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.unhandledQAList.isDisplayed();
            }
            page.addAnswerText(internid, "Nu svarar FitNesse!!!")

            waitFor {
                page.sendAnswer(internid)
            }

        }
    }


    /*
      public boolean inkorgsidanVisas() {
          Browser.drive {
              waitFor {
                  at InboxPage
              }
          }
      }

      public boolean arkiveradesidanVisas() {
          Browser.drive {
              waitFor {
                  at ArchivedPage
              }
          }
      }

      public boolean listaMedIntygVisas() {
          def result = false
          Browser.drive {
              waitFor {
                  at InboxPage
              }
              result = page.certificateTable.isDisplayed()
          }
          result
      }

      public boolean listaMedArkiveradeIntygVisas() {
          def result = false
          Browser.drive {
              waitFor {
                  at ArchivedPage
              }
              result = page.certificateTable.isDisplayed()
          }
          result
      }

      public boolean finnsIngaIntyg() {
          def result = false
          Browser.drive {
              waitFor {
                  at InboxPage
              }
              result = page.noCertificates.isDisplayed()
          }
          result
      }

      public void arkiveraIntyg(String id) {
          Browser.drive {
              at InboxPage
              page.archiveCertificate(id)
          }
      }

      public void konfirmeraArkiveraIntyg() {
          Browser.drive {
              at InboxPage
              waitFor (message: "no button") {
                  confirmArchiveButton.displayed
              }
              page.confirmArchiveCertificate()
          }
      }

      public void återställIntyg(String id) {
          Browser.drive {
              at ArchivedPage
              page.restoreCertificate(id)
          }
      }

      public void konfirmeraÅterställIntyg() {
          Browser.drive {
              at ArchivedPage
              waitFor {
                  confirmRestoreButton.displayed
              }
              page.confirmRestoreCertificate()
          }
      }

      public void gåTillArkiveradeIntyg() {
          Browser.drive {
              at InboxPage
              page.goToArchivedTab()
          }
      }

      public void gåTillInkorgen() {
          Browser.drive {
              at ArchivedPage
              page.goToInboxPage()
          }
      }

      public boolean arkiveratIntygFinnsIListan(String id) {
          def result = false
          Browser.drive {
              waitFor {
                  at ArchivedPage
              }
              result = page.certificateExists(id)
          }
          result
      }

      public boolean arkiveratIntygFinnsEjIListan(String id) {
          def result = false
          Browser.drive {
              waitFor {
                  at ArchivedPage
                  !page.certificateExists(id)
              }
              result = !page.certificateExists(id)
          }
          result
      }

      public boolean intygFinnsIListan(String id) {
          def result = false
          Browser.drive {
              waitFor {
                  at InboxPage
                  page.certificateExists(id)
              }
              result = page.certificateExists(id)
          }
          result
      }

      public boolean intygFinnsEjIListan(String id) {
          def result = false
          Browser.drive {
              waitFor {
                  at InboxPage
                  !page.certificateExists(id)
              }
              result = !page.certificateExists(id)
          }
          result
      }

      public boolean rättatIntygVisasKorrekt(String id) {
          def result = false
          Browser.drive {
              waitFor {
                  at InboxPage
              }
              result = page.cancelledCertificateDisplayed(id);
          }
          result
      }

      public void visaIntyg(String id) {
          Browser.drive {
              at InboxPage
              page.viewCertificate(id)
          }
      }
      */
}
