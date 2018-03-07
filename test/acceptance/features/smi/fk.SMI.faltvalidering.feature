# language: sv
@FALTVALIDERING @SMI @F.Val
# Funktion har ändrats. TF behöver uppdateras
Egenskap: F.Val - Fältvalidering SMI

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@VALIDERING-FELAKTIGT-FALT
Scenariomall: <krav> Validera felaktigt <typAvFält> i <intygsTyp>
    När jag går in på att skapa ett <intygsTyp> intyg
    Och jag fyller i text i <typAvFält> fältet
    Och jag klickar på signera-knappen
    Så ska valideringsfelet <feltext> visas

Exempel:
    | krav |    typAvFält              |                   feltext                         | intygsTyp |
    | F.Val-001 |   "postnummer"            |  "Postnummer måste anges med fem siffror"         |	"Läkarintyg för sjukpenning"			|
    # Personligkännedom om patient finns inte för LISJP |   "kännedom-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarintyg för sjukpenning"			|
    | F.Val-030 |   "slumpat-datum"         |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarintyg för sjukpenning"			|
    # andraMedicinskaUtredningar finns inte för LISJP |   "underlag-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarintyg för sjukpenning"			|
    | F.Val-001 |   "postnummer"            |  "Postnummer måste anges med fem siffror"         |	"Läkarutlåtande för sjukersättning"		|
    | F.Val-030 |   "kännedom-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för sjukersättning"		|
    | F.Val-030 |   "slumpat-datum"         |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för sjukersättning"		|
    | F.Val-030 |   "underlag-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för sjukersättning"		|
	| F.Val-001 |   "postnummer"            |  "Postnummer måste anges med fem siffror"         |	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"		|
    | F.Val-030 |   "kännedom-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"		|
    | F.Val-030 |   "slumpat-datum"         |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"		|
    | F.Val-030 |   "underlag-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"		|
    | F.Val-001 |   "postnummer"            |  "Postnummer måste anges med fem siffror"         |	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"			|
    | F.Val-030 |   "kännedom-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"			|
    | F.Val-030 |   "slumpat-datum"         |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"			|
    | F.Val-030 |   "underlag-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"			|
	

@VALIDERING-DATUM @LISJP @F.Val-044
Scenario: F.Val-044 - Intyget kan inte signeras om slut är före startdatum
    När jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och anger ett slutdatum som är tidigare än startdatum
    Och jag klickar på signera-knappen
    Så ska valideringsfelet "Startdatum får inte vara efter slutdatum" visas
