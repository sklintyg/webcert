# language: sv
@faltvalidering @smi 
# Funktion har ändrats. TF behöver uppdateras
Egenskap: Fältvalidering LUSE

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient


Scenariomall: Validera felaktigt <typAvFält> i SMI-intyg
    När jag går in på att skapa ett slumpat SMI-intyg
    Och jag fyller i text i <typAvFält> fältet
    Och jag klickar på signera-knappen
    Så ska valideringsfelet <feltext> visas 

Exempel:
    |    typAvFält              |                   feltext                         |
    |   "postnummer"            |  "Postnummer måste anges med fem siffror"         |
    |   "kännedom-datum"        |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |
    |   "slumpat-datum"         |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"   |
    |   "underlag-datum"        |  "Fel datumformat för underlag"                   |


Scenariomall: Validera felaktigt <typAvFält> i <intygsTyp>
	När jag går in på att skapa ett <intygsTyp> intyg
	Och jag fyller i text i <typAvFält> fältet
    Och jag klickar på signera-knappen
	Så ska valideringsfelet <feltext> visas 

Exempel:
 	| intygsTyp                     |	 typAvFält		     	| feltext       				    |
    |"Läkarintyg för sjukpenning"	|	"arbetsförmåga-datum"	| "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"			|