# language: sv
@faltvalidering @smi
# Funktion har ändrats. TF behöver uppdateras
Egenskap: Fältvalidering LUSE

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@validering-felaktigt-falt
Scenariomall: Validera felaktigt <typAvFält> i <intygsTyp>
    När jag går in på att skapa ett <intygsTyp> intyg
    Och jag fyller i text i <typAvFält> fältet
    Och jag klickar på signera-knappen
    Så ska valideringsfelet <feltext> visas

Exempel:
    |    typAvFält              |                   feltext                         | intygsTyp |
    |   "postnummer"            |  "Postnummer måste anges med fem siffror"         |	"Läkarintyg för sjukpenning"			|
    # Personligkännedom om patient finns inte för LISJP |   "kännedom-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarintyg för sjukpenning"			|
    |   "slumpat-datum"         |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarintyg för sjukpenning"			|
    # andraMedicinskaUtredningar finns inte för LISJP |   "underlag-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarintyg för sjukpenning"			|
    |   "postnummer"            |  "Postnummer måste anges med fem siffror"         |	"Läkarutlåtande för sjukersättning"		|
    |   "kännedom-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för sjukersättning"		|
    |   "slumpat-datum"         |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för sjukersättning"		|
    |   "underlag-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för sjukersättning"		|
	|   "postnummer"            |  "Postnummer måste anges med fem siffror"         |	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"		|
    |   "kännedom-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"		|
    |   "slumpat-datum"         |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"		|
    |   "underlag-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"		|
    |   "postnummer"            |  "Postnummer måste anges med fem siffror"         |	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"			|
    |   "kännedom-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"			|
    |   "slumpat-datum"         |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"			|
    |   "underlag-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"			|
	

@validering-datum @lisjp @INTYG-3760
Scenario: Intyget kan inte signeras om slut är före startdatum
    När jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och anger ett slutdatum som är tidigare än startdatum
    Och jag klickar på signera-knappen
    Så ska valideringsfelet "Startdatum får inte vara efter slutdatum" visas
