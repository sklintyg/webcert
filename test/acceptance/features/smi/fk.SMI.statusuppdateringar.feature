# language: sv
@STATUSUPPDATERINGAR @SMI
Egenskap: Statusuppdateringar för SMI intyg

Bakgrund: Jag har skickat en CreateDraft:2 till Webcert.
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
   Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
   Och jag går in på intygsutkastet via djupintegrationslänk

@SKICKA-TILL-FK @SIGNAT @SKAPAT @SKICKA
Scenario: Statusuppdateringar då SMI-intyg skickas till Försäkringskassan
    Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"

    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska statusuppdatering "SIGNAT" skickas till vårdsystemet. Totalt: "1"

    När jag skickar intyget till Försäkringskassan
    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"

@MAKULERA
Scenario: Statusuppdateringar då SMI-intyg makuleras
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    När jag makulerar intyget
    Så ska statusuppdatering "MAKULE" skickas till vårdsystemet. Totalt: "1"

@RADERA
Scenario: Statusuppdateringar då SMI-utkast raderas
    När jag fyller i alla nödvändiga fält för intyget
    Och jag raderar utkastet
    Så ska statusuppdatering "RADERA" skickas till vårdsystemet. Totalt: "1"

    Och jag försöker gå in på intygsutkastet via djupintegrationslänk
	Så ska jag varnas om att "Intyget gick inte att läsa in"
	Så ska jag varnas om att "Intygsutkastet är raderat och kan därför inte längre visas."

@FRÅGA-FRÅN-FK @NYFRFM
Scenario: Statusuppdateringar vid fråga från FK
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan
    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"

    När Försäkringskassan skickar ett "KONTKT" meddelande på intyget
    Så ska statusuppdatering "NYFRFM" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa mottagna frågor totalt 1,ej besvarade 1,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0

    När jag går in på intyget via djupintegrationslänk
    Och jag svarar på frågan
    Så ska statusuppdatering "HANFRFM" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa mottagna frågor totalt 1,ej besvarade 0,besvarade 0, hanterade 1
    Och ska statusuppdateringen visa skickade frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0


@FRÅGA-TILL-FK
Scenario: Statusuppdateringar vid fråga från vården
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"

    Och jag går in på intyget via djupintegrationslänk
    Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
    Så ska statusuppdatering "NYFRFV" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 1,besvarade 0, hanterade 0

    Och Försäkringskassan skickar ett svar
    Så ska statusuppdatering "NYSVFM" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 0,besvarade 1, hanterade 0
    # Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 1, hanterade svar 0

    Och jag markerar svaret från Försäkringskassan som hanterad

    Så ska statusuppdatering "HANFRFV" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 0,besvarade 0, hanterade 1
    # Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 1, hanterade svar 1

    Och jag markerar svaret från Försäkringskassan som INTE hanterad
    Så ska statusuppdatering "HANFRFV" skickas till vårdsystemet. Totalt: "2"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 0,besvarade 1, hanterade 0
    # Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 1, hanterade svar 0

@FRÅGA-TILL-FK @HANTERA @FKSMOKE
Scenario: Statusuppdateringar vid hantering av fråga från vården
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan

    När jag markerar frågan från vården som hanterad
    Så ska statusuppdatering "HANFRFV" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 0,besvarade 0, hanterade 1

@ANDRAT
Scenario: Statusuppdateringar vid ändring av utkast
    Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"

    När jag ändrar i slumpat fält
    Så ska statusuppdatering "ANDRAT" skickas till vårdsystemet. Totalt: "1"

@VÅRDKONTAKT @REF
Scenario: Referens skickas med statusuppdateringar
    När jag går in på intyget via djupintegrationslänk med parameter "ref=testref-X"

    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag förnyar intyget
	Och jag fyller i nödvändig information ( om intygstyp är "Läkarintyg för sjukpenning")
	Och jag signerar intyget
	
    Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"
	Så ska statusuppdatering "SIGNAT" skickas till vårdsystemet. Totalt: "1"
	#Viktigt att vi kör Signat innan nedan steg
    Och ska statusuppdateringen visa att parametern "ref" är mottagen med värdet "testref-X"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0

    När jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten
    Så ska svaret innehålla intyget jag var inne på
    Och ska svaret innehålla ref med värdet "testref-X"

	
@FÖRNYA @ANDRAT @SKICKA @NYFRFV @NYSVFM @HANFRFV
Scenario: Testa att statusuppdateringar fungerar efter intyget är förnyat
	
	När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
	Och jag förnyar intyget
	
	När jag fyller i nödvändig information ( om intygstyp är "Läkarintyg för sjukpenning")
	Och jag ändrar i slumpat fält
	Så ska statusuppdatering "ANDRAT" skickas till vårdsystemet. Totalt: "1"
	
	Och jag signerar intyget
	
    Och jag skickar intyget till Försäkringskassan
    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"
	
	Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
    Så ska statusuppdatering "NYFRFV" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 1,besvarade 0, hanterade 0

    Och Försäkringskassan skickar ett svar
    Så ska statusuppdatering "NYSVFM" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 0,besvarade 1, hanterade 0
    # Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 1, hanterade svar 0

    Och jag markerar svaret från Försäkringskassan som hanterad

    Så ska statusuppdatering "HANFRFV" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 0,besvarade 0, hanterade 1

    Och jag markerar svaret från Försäkringskassan som INTE hanterad
    Så ska statusuppdatering "HANFRFV" skickas till vårdsystemet. Totalt: "2"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 0,besvarade 1, hanterade 0
	
	
@ERSÄTT @ANDRAT @SKICKA @NYFRFV @NYSVFM @HANFRFV
Scenario: Testa att statusuppdateringar fungerar efter intyget är ersatt
	
	När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
	Och jag klickar på ersätta knappen
	Och jag klickar på ersätt-knappen i dialogen
	
	När jag fyller i nödvändig information ( om intygstyp är "Läkarintyg för sjukpenning")
	Och jag ändrar i slumpat fält
	Så ska statusuppdatering "ANDRAT" skickas till vårdsystemet. Totalt: "1"
	
	Och jag signerar intyget
	
    Och jag skickar intyget till Försäkringskassan
    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"
	
	Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
    Så ska statusuppdatering "NYFRFV" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 1,besvarade 0, hanterade 0

    Och Försäkringskassan skickar ett svar
    Så ska statusuppdatering "NYSVFM" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 0,besvarade 1, hanterade 0

    Och jag markerar svaret från Försäkringskassan som hanterad

    Så ska statusuppdatering "HANFRFV" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 0,besvarade 0, hanterade 1

    Och jag markerar svaret från Försäkringskassan som INTE hanterad
    Så ska statusuppdatering "HANFRFV" skickas till vårdsystemet. Totalt: "2"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 1,ej besvarade 0,besvarade 1, hanterade 0
	