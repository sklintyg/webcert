# language: sv
@faltvalidering @smi 
# Funktion har ändrats. TF behöver uppdateras
Egenskap: Fältvalidering LUSE

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

Scenariomall: Validera felaktigt ifyllda fält
	När jag går in på att skapa ett <intygsTyp> intyg
	Och jag fyller i text i <typAvFält> fältet
	Så ska valideringsfelet <feltext> visas 

Exempel:
 	|	                        intygsTyp								|	 typAvFält		     	|    			    feltext       				    |
	|"Läkarutlåtande för sjukersättning"  								|	"postnummer"			|  "Postnummer har fel format"					    |
	|"Läkarutlåtande för sjukersättning" 								|	"slumpat-datum"		    |  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarutlåtande för sjukersättning"								|	"underlag-datum"		|  "Fel datumformat för underlag"					|
    |"Läkarutlåtande för sjukersättning"								|	"kännedom-datum"		|  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarintyg för sjukpenning"										|	"slumpat-datum"			|  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarintyg för sjukpenning"										|	"arbetsförmåga-datum"	|  "Felaktigt datumformat."						|
    |"Läkarintyg för sjukpenning"										|	"postnummer"		    |  "Postnummer har fel format"					|
    |"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"	|	"slumpat-datum"			|  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"	|	"kännedom-datum"		|  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"	|	"underlag-datum"		|  "Fel datumformat för underlag"					|
    |"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"	|	"postnummer"		    |  "Postnummer har fel format"					    |
    |"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"|	"slumpat-datum"			|  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"|	"kännedom-datum"		|  "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"|	"underlag-datum"		|  "Fel datumformat för underlag"					|
    |"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"|	"postnummer"		    |  "Postnummer har fel format"					    |
