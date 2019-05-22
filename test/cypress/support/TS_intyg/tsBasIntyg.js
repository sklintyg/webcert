// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall

// -------------------- 'Intyget avser' --------------------
export function sektionIntygetAvser(intygetAvser) {

    if (intygetAvser) {
        // TODO: Lägger väldigt lång timeout vid första elementet i intyget eftersom
        // sidan ibland inte har hunnit ladda innan den får timeout.
        // Initial analys är att Jenkins är överbelastad då det verkar fungera bra när
        // man kör lokalt

        var körkortsTyper = {
            "C1": '#intygAvser\\.korkortstyp-C1',
            "C1E": '#intygAvser\\.korkortstyp-C1E',
            "C": '#intygAvser\\.korkortstyp-C',
            "CE": '#intygAvser\\.korkortstyp-CE',
            "D1": '#intygAvser\\.korkortstyp-D1',
            "D1E": '#intygAvser\\.korkortstyp-D1E',
            "D": '#intygAvser\\.korkortstyp-D',
            "DE": '#intygAvser\\.korkortstyp-DE',
            "Taxi": '#intygAvser\\.korkortstyp-TAXI',
            "Annat": '#intygAvser\\.korkortstyp-ANNAT'
        };

        for (var key in intygetAvser) {
            if (intygetAvser[key]) {
                cy.get(körkortsTyper[key], {timeout: 60000}).check();
            }
        }
    }
}

// -------------------- 'Identitet' --------------------
export function sektionIdentitet(identitet) {
    if (identitet) {

        var indentifieringsTyper = {
            "IDkort": '#vardkontakt\\.idkontroll-ID_KORT',
            "FöretagskortTjänstekort": '#vardkontakt\\.idkontroll-FORETAG_ELLER_TJANSTEKORT',
            "Körkort": '#vardkontakt\\.idkontroll-KORKORT',
            "PersonligKännedom": '#vardkontakt\\.idkontroll-PERS_KANNEDOM',
            "Försäkran": '#vardkontakt\\.idkontroll-FORSAKRAN_KAP18',
            "Pass": '#vardkontakt\\.idkontroll-PASS'
        }

        for (var key in identitet) {
            if (identitet[key]) {
                cy.get(indentifieringsTyper[key]).check();
            }
        }
    }
}
// -------------------- 'Synfunktioner' --------------------
export function sektionSynfunktioner(synfunktioner) {

    if (synfunktioner.synfältsdefekter) {
        cy.get('#syn-synfaltsdefekterYes').check();
    } else {
        cy.get('#syn-synfaltsdefekterNo').check();
    }
    if (synfunktioner.anamnestiskaUppgifter) {
        cy.get('#syn-nattblindhetYes').check();
    } else {
        cy.get('#syn-nattblindhetNo').check();
    }
    if (synfunktioner.ögonsjukdom) {
        cy.get('#syn-progressivOgonsjukdomYes').check();
    } else {
        cy.get('#syn-progressivOgonsjukdomNo').check();
    }
    if (synfunktioner.dubbelseende) {
        cy.get('#syn-diplopiYes').check();
    } else {
        cy.get('#syn-diplopiNo').check();
    }
    if (synfunktioner.nystagmus) {
        cy.get('#syn-nystagmusYes').check();
    } else {
        cy.get('#syn-nystagmusNo').check();
    }
    
    var ögaText = [synfunktioner.synskärpa.högerÖga.utanKorrektion.ja, synfunktioner.synskärpa.högerÖga.medKorrektion.ja, 
        synfunktioner.synskärpa.vänsterÖga.utanKorrektion.ja, synfunktioner.synskärpa.vänsterÖga.medKorrektion.ja,
        synfunktioner.synskärpa.binokulärt.utanKorrektion.ja, synfunktioner.synskärpa.binokulärt.medKorrektion.ja];

    var ögaVärde = [synfunktioner.synskärpa.högerÖga.utanKorrektion.värde, synfunktioner.synskärpa.högerÖga.medKorrektion.värde, 
        synfunktioner.synskärpa.vänsterÖga.utanKorrektion.värde, synfunktioner.synskärpa.vänsterÖga.medKorrektion.värde,
        synfunktioner.synskärpa.binokulärt.utanKorrektion.värde, synfunktioner.synskärpa.binokulärt.medKorrektion.värde];

    var ögaEle = {
        "synfunktioner.synskärpa.högerÖga.utanKorrektion.ja": '#syn-hogerOga-utanKorrektion',
        "synfunktioner.synskärpa.högerÖga.medKorrektion.ja": '#syn-hogerOga-medKorrektion',
        "synfunktioner.synskärpa.vänsterÖga.utanKorrektion.ja": '#syn-vansterOga-utanKorrektion',
        "synfunktioner.synskärpa.vänsterÖga.medKorrektion.ja": '#syn-vansterOga-medKorrektion',
        "synfunktioner.synskärpa.binokulärt.utanKorrektion.ja": '#syn-binokulart-utanKorrektion',
        "synfunktioner.synskärpa.binokulärt.medKorrektion.ja": '#syn-binokulart-medKorrektion'
    }

    var keyNames = Object.keys(ögaEle);

    for (var i=0; i < ögaText.length; i++) {
        if (ögaText[i]) {
            var keyName = keyNames[i];
            cy.get(ögaEle[keyName]).type(ögaVärde[i]);
        }
    }

    if (synfunktioner.synskärpa.högerÖga.kontaktlinser) {
        cy.get('#syn-hogerOga-kontaktlins').check();
    }
    if (synfunktioner.synskärpa.vänsterÖga.kontaktlinser) {
        cy.get('#syn-vansterOga-kontaktlins').check();
    }
    if (synfunktioner.styrka) {
        cy.get('#syn-korrektionsglasensStyrka').check();
    }
}

// -------------------- 'Hörsel och balanssinne' --------------------
export function sektionHörselBalans(hörselOchBalans, intygetAvser) {

    var dEllerTaxiKörkort = [intygetAvser.D1, intygetAvser.D1E, intygetAvser.D, intygetAvser.DE, intygetAvser.Taxi]

    if (hörselOchBalans.balansrubbningar) {
        cy.get('#horselBalans-balansrubbningarYes').check();
    } else {
        cy.get('#horselBalans-balansrubbningarNo').check();
    }

    for (var item in dEllerTaxiKörkort) {
        if (item) {
            if (hörselOchBalans.fyraMeter) {
                cy.get('body').then(($body) => {
                    if ($body.text().includes('hörapparat får användas')) {
                        cy.get('#horselBalans-svartUppfattaSamtal4MeterYes').check();
                    }
                });
            } else {
                cy.get('body').then(($body) => {
                    if ($body.text().includes('hörapparat får användas')) {
                        cy.get('#horselBalans-svartUppfattaSamtal4MeterNo').check();
                    }
                });
            }
        }
    }
}

// -------------------- 'Rörelseorganens funktioner' --------------------
export function sektionRörelseorganensFunktioner(rörelseorganensFunktioner, intygetAvser) {

    var dEllerTaxiKörkort = [intygetAvser.D1, intygetAvser.D1E, intygetAvser.D, intygetAvser.DE, intygetAvser.Taxi]

    if (rörelseorganensFunktioner.rörlighet.ja) {
        cy.get('#funktionsnedsattning-funktionsnedsattningYes').check();
        cy.get('#funktionsnedsattning-beskrivning').type(rörelseorganensFunktioner.rörlighet.text);
    } else {
        cy.get('#funktionsnedsattning-funktionsnedsattningNo').check();
    }

    for (var item in dEllerTaxiKörkort) {
        if (item) {
            if (rörelseorganensFunktioner.nedsättningRörelse) {
                cy.get('body').then(($body) => {
                    if ($body.text().includes('hjälpa passagerare in och ut')) {
                        cy.get('#funktionsnedsattning-otillrackligRorelseformagaYes').check();
                    }
                });
            } else {
                cy.get('body').then(($body) => {
                    if ($body.text().includes('hjälpa passagerare in och ut')) {
                        cy.get('#funktionsnedsattning-otillrackligRorelseformagaNo').check();
                    }
                });
            }
        }
    }
}

// -------------------- 'Hjärt- och kärlsjukdomar' --------------------
export function sektionHjärtKärlSjukdomar(hjärtOchKärl) {
    if (hjärtOchKärl.hjärtOchKärlSjukdom) {
        cy.get('#hjartKarl-hjartKarlSjukdomYes').check();
    } else {
        cy.get('#hjartKarl-hjartKarlSjukdomNo').check();
    }
    if (hjärtOchKärl.hjärnskada) {
        cy.get('#hjartKarl-hjarnskadaEfterTraumaYes').check();
    } else {
        cy.get('#hjartKarl-hjarnskadaEfterTraumaNo').check();
    }
    if (hjärtOchKärl.stroke.ja) {
        cy.get('#hjartKarl-riskfaktorerStrokeYes').check();
        cy.get('#hjartKarl-beskrivningRiskfaktorer').type(hjärtOchKärl.stroke.text);
    } else {
        cy.get('#hjartKarl-riskfaktorerStrokeNo').check();
    }
}

// -------------------- 'Diabetes' --------------------
export function sektionDiabetes(diabetes) {

    const behandling = diabetes.typer.typ2.behandling;

    var behandningsTyper = {
        "kost": '#diabetes-kost',
        "Tabletter": '#diabetes-tabletter',
        "Insulin": '#diabetes-insulin'
    };

    if (diabetes.ja) {
        cy.get('#diabetes-harDiabetesYes').check();
        if (diabetes.typer.typ1) {
            cy.get('#diabetes\\.diabetesTyp-DIABETES_TYP_1').check();
        } else {
            cy.get('#diabetes\\.diabetesTyp-DIABETES_TYP_2').check();
            for (var key in behandling) {
                if (behandling[key]) {
                    cy.get(behandningsTyper[key]).check();
                }
            }
        }
    } else {
        cy.get('#diabetes-harDiabetesNo').check();
    }
}

// -------------------- 'Neurologiska sjukdomar' --------------------
export function sektionNeurologiskaSjukdomar(neurologiskaSjukdomar) {
    if (neurologiskaSjukdomar) {
        cy.get('#neurologi-neurologiskSjukdomYes').check();
    } else {
        cy.get('#neurologi-neurologiskSjukdomNo').check();
    }
}

// ---'Epilepsi, epileptiskt anfall och annan medvetandestörning' ---
export function sektionEpilepsi(epilepsi) {
    if (epilepsi.ja) {
        cy.get('#medvetandestorning-medvetandestorningYes').check();
        cy.get('#medvetandestorning-beskrivning').type(epilepsi.text);
    } else {
        cy.get('#medvetandestorning-medvetandestorningNo').check();
    }
}

// -------------------- 'Njursjukdomar' --------------------
export function sektionNjurSjukdomar(njursjukdomar) {
    if (njursjukdomar) {
        cy.get('#njurar-nedsattNjurfunktionYes').check();
    } else {
        cy.get('#njurar-nedsattNjurfunktionNo').check();
    }
}

// -------------------- 'Demens och andra kognitiva störningar' --------------------
export function sektionDemens(demens) {
    if (demens) {
        cy.get('#kognitivt-sviktandeKognitivFunktionYes').check();
    } else {
        cy.get('#kognitivt-sviktandeKognitivFunktionNo').check();
    }
}

// -------------------- 'Sömn- och vakenhetsstörningar' --------------------
export function sektionSömnOchVakenhetsStörningar(sömnOchVakenhetsStörningar) {
    if (sömnOchVakenhetsStörningar) {
        cy.get('#somnVakenhet-teckenSomnstorningarYes').check();
    } else {
        cy.get('#somnVakenhet-teckenSomnstorningarNo').check();
    }
}

// -------------------- 'Alkohol, narkotika och läkemedel' --------------------
export function sektionAlkoholNarkotikaLäkemedel(alkoholNarkotika) {
    if (alkoholNarkotika.journaluppgifter) {
        cy.get('#narkotikaLakemedel-teckenMissbrukYes').check();
    } else {
        cy.get('#narkotikaLakemedel-teckenMissbrukNo').check();
    }
    if (alkoholNarkotika.vårdinsats) {
        cy.get('#narkotikaLakemedel-foremalForVardinsatsYes').check();
    } else {
        cy.get('#narkotikaLakemedel-foremalForVardinsatsNo').check();
    }
    if (alkoholNarkotika.journaluppgifter || alkoholNarkotika.vårdinsats) {
        if (alkoholNarkotika.provtagning) {
            cy.get('#narkotikaLakemedel-provtagningBehovsYes').check();
        } else {
            cy.get('#narkotikaLakemedel-provtagningBehovsNo').check();
        }
    }
    if (alkoholNarkotika.regelbundet.ja) {
        cy.get('#narkotikaLakemedel-lakarordineratLakemedelsbrukYes').check();
        cy.get('#narkotikaLakemedel-lakemedelOchDos').type(alkoholNarkotika.regelbundet.Läkemedel);
    } else {
        cy.get('#narkotikaLakemedel-lakarordineratLakemedelsbrukNo').check();
    }
}

// -------------------- 'Psykiska sjukdomar och störningar' --------------------
export function sektionPsykiskaSjukdomarStörningar(psykiskaSjukdomar) {
    if (psykiskaSjukdomar) {
        cy.get('#psykiskt-psykiskSjukdomYes').check();
    } else {
        cy.get('#psykiskt-psykiskSjukdomNo').check();
    }
}

// ----- 'ADHD, autismspektrumtillstånd och likartade tillstånd samt psykisk utvecklingsstörning' -----
export function sektionADHD(ADHD) {
    if (ADHD.psykiskUtvStörning) {
        cy.get('#utvecklingsstorning-psykiskUtvecklingsstorningYes').check();
    } else {
        cy.get('#utvecklingsstorning-psykiskUtvecklingsstorningNo').check();
    }
    if (ADHD.ADHD) {
        cy.get('#utvecklingsstorning-harSyndromYes').check();
    } else {
        cy.get('#utvecklingsstorning-harSyndromNo').check();
    }
}

// -------------------- 'Sjukhusvård' --------------------
export function sektionSjukhusvård(sjukhusvård) {
    if (sjukhusvård.ja) {
        cy.get('#sjukhusvard-sjukhusEllerLakarkontaktYes').check();
        cy.get('#sjukhusvard-tidpunkt').type(sjukhusvård.när);
        cy.get('#sjukhusvard-vardinrattning').type(sjukhusvård.klinikNamn);
        cy.get('#sjukhusvard-anledning').type(sjukhusvård.vad);
    } else {
        cy.get('#sjukhusvard-sjukhusEllerLakarkontaktNo').check();
    }
}

// -------------------'Övrig medicinering' -----------------------
export function sektionÖvrigMedicinering(övrigMedicinering) {
    if (övrigMedicinering.ja) {
        cy.get('#medicinering-stadigvarandeMedicineringYes').check();
        cy.get('#medicinering-beskrivning').type(övrigMedicinering.text);
    } else {
        cy.get('#medicinering-stadigvarandeMedicineringNo').check();
    }
}

// -------------------'Övrig kommentar' -----------------------
export function sektionÖvrigKommentar(övrigKommentar) {
    if (övrigKommentar.ja) {
        cy.get('#kommentar').type(övrigKommentar.text);
    }
}

// -------------------'Bedömning' -----------------------
export function sektionBedömning(bedömning) {

    var bedömingAlternativ = {
        "C1": "#bedomning\\.korkortstyp-C1",
        "C1E": "#bedomning\\.korkortstyp-C1E",
        "C": "#bedomning\\.korkortstyp-C",
        "CE": "#bedomning\\.korkortstyp-CE",
        "D1": "#bedomning\\.korkortstyp-D1",
        "D1E": "#bedomning\\.korkortstyp-D1E",
        "D": "#bedomning\\.korkortstyp-D",
        "DE": "#bedomning\\.korkortstyp-DE",
        "Taxi": "#bedomning\\.korkortstyp-TAXI",
        "Annat": "#bedomning\\.korkortstyp-ANNAT"
    };

    if (bedömning.kanInteTaStällning) {
        cy.get('#bedomning\\.korkortstyp-KAN_INTE_TA_STALLNING').check();
    } else {
        for (var key in bedömning.kanTaStällning) {
            if (bedömning.kanTaStällning[key]) {
                cy.get(bedömingAlternativ[key]).check();
            }
        }
    }

    if (bedömning.specialistkompetens.ja) {
        cy.get('#bedomning-lakareSpecialKompetens').type(bedömning.specialistkompetens.text);
    }
}

// -------------------- 'Vårdenhetens adress' --------------------
// Ej implementerad

// -------------------- 'Signera intyget' --------------------
export function signera() {
    // TODO: Utan wait så tappas ofta slutet på texten bort i sista textboxen.
    // Antagligen hinner WebCert inte auto-spara innan man trycker på "signera".
    // Wait är dock ett anti-pattern så finns något annat sätt så är det att föredra.
    cy.wait(1000);

    cy.contains("Klart att signera");
    cy.contains("Obligatoriska uppgifter saknas").should('not.exist');
    cy.contains("Utkastet sparas").should('not.exist');

    // cy.click() fungerar inte alltid. Det finns ärenden rapporterade
    // (stängd pga inaktivitet):
    // https://github.com/cypress-io/cypress/issues/2551
    // https://www.cypress.io/blog/2019/01/22/when-can-the-test-click/ :
    // "If a tree falls in the forest and no one has attached a “fall” event listener, did it really fall?"

    const click = $el => { return $el.click() }

    // Parent() p.g.a. att ett element täcker knappen
    cy.get('#signera-utkast-button').parent().should('be.visible')

    cy.get('#signera-utkast-button')
    .pipe(click, {timeout: 60000}) // ToDo: Lång timeout (problem endast på Jenkins, överlastad slav?)
    .should($el => {
        expect($el.parent()).to.not.be.visible;
    })
}

// -------------------- 'Skicka intyget' --------------------
export function skickaTillFk() {
    cy.get("#sendBtn", { timeout: 60000 }).click();

    // Modal som dyker upp och frågar om man verkligen vill skicka
    cy.get("#button1send-dialog").click();
    cy.contains("Intyget är skickat till Försäkringskassan");
}

// -------------------- 'Skriv ut intyget' --------------------
export function skrivUt(typAvUtskrift, intygsId){
    switch(typAvUtskrift) {
        case "utkast":
        case "fullständigt":
            cy.request({
                method: 'GET',
                url: 'moduleapi/intyg/lisjp/' + intygsId + "/pdf",
            });
            cy.log('Skriver ut ett ' + typAvUtskrift+ ' intyg (via cy.request, ej grafiskt)');
            break;
        case "minimalt":
            cy.request({
                method: 'GET',
                url: 'moduleapi/intyg/lisjp/' + intygsId + "/pdf/arbetsgivarutskrift",
            })
            cy.log('Skriver ut ett minimalt intyg (via cy.request, ej grafiskt)');
            break;
        default:
            cy.log('Ingen korrekt typ av utskrift vald');
    }
}
// ------------------'Förnya intyg'---------------------------
export function fornya() {
    cy.get('#fornyaBtn').click();
}

// ------------------'Radera utkast'--------------------------
export function raderaUtkast() {
    cy.get('#ta-bort-utkast').click();
    cy.get('#confirm-draft-delete-button').click();   
}

// ------------------'Makulera intyg'-------------------------
export function makuleraIntyg(arg) {
    cy.get('#makuleraBtn').click();
    if (arg === "Annat allvarligt fel") {
        cy.get('#reason-ANNAT_ALLVARLIGT_FEL').check();
        cy.get('#clarification-ANNAT_ALLVARLIGT_FEL').type('Testanledning');
        cy.get('#button1makulera-dialog').click();
    } else {
        cy.get('#reason-FEL_PATIENT').check();
        cy.get('#button1makulera-dialog').click();
    }
}