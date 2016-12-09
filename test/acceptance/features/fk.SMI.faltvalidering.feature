# language: sv
@faltvalidering 
# Funktion har ändrats. TF behöver uppdateras
Egenskap: Fältvalidering LUSE

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient
	
@notReady
Scenario: Validera felaktig diagnoskod i LUSE
	När jag fyller i "000" som diagnoskod
	Så ska valideringsfelet "är ej giltig" visas

@SMIFALT
Scenariomall: Validera felaktigt ifyllda fält
	När jag går in på att skapa ett <intygsTyp> intyg
	Och jag fyller i text i <typAvFält> fältet
	Så ska <typAvFel> valideringsfelet, <feltext> visas 

Exempel:
 	|	                        intygsTyp								|	      typAvFält			|	  typAvFel		|    			    feltext       				|
	|"Läkarutlåtande för sjukersättning"  								|	"postnummer"			|	"postnummer"	|"Postnummer har fel format"					|
	|"Läkarutlåtande för sjukersättning" 								|	"slumpat-datum"			|	"datum"			|"Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarutlåtande för sjukersättning"								|	"underlag-datum"		|	"underlag"		|"Fel datumformat för underlag"					|
    |"Läkarutlåtande för sjukersättning"								|	"kännedom-datum"		|	"kännedom"		|"Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarintyg för sjukpenning"										|	"slumpat-datum"			|	"datum"			|"Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarintyg för sjukpenning"										|	"arbetsförmåga-datum"	|	"arbetsförmåga"	|"Felaktigt datumformat."						|
    |"Läkarintyg för sjukpenning"										|	"postnummer"			|	"postnummer"	|"Postnummer har fel format"					|
    |"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"	|	"slumpat-datum"			|	"datum"			|"Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"	|	"kännedom-datum"		|	"kännedom"		|"Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"	|	"underlag-datum"		|"underlag-LUAEFS"	|"Fel datumformat för underlag"					|
    |"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"	|	"postnummer"			|	"postnummer"	|"Postnummer har fel format"					|
    |"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"|	"slumpat-datum"			|	"datum"			|"Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"|	"kännedom-datum"		|	"kännedom"		|"Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"	|
    |"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"|	"underlag-datum"		|"underlag-LUAENA"	|"Fel datumformat för underlag"					|
    |"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga"|	"postnummer"			|	"postnummer"	|"Postnummer har fel format"					|
