# language: sv
@smoke @nedsattarbetsformaga @smi
Egenskap: Hantera SMI intyg

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@keepIntyg @signera
Scenariomall: Skapa och signera ett intyg för <intygKod>
	När jag går in på att skapa ett <intyg> intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets status vara "Intyget är signerat"

	När jag går till Mina intyg för patienten
	Så ska intyget finnas i Mina intyg

	#När jag går in på intyget i Mina intyg
	#Så ska intygets information i Mina intyg vara den jag angett

@luse
Exempel:
    |intygKod   | 	intyg 																|
    |LUSE       | 	"Läkarutlåtande för sjukersättning"                                 |

@luaena
Exempel:
    |intygKod   | 	intyg 																|
    |LUAE_NA    | 	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga" |

Exempel:
    |intygKod   | 	intyg                                                               |
    |LUAE_NA    | 	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga" |
    |LUAE_FS    | 	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"     |

    #|LISJP      | 	"Läkarintyg för sjukpenning"                                        |

@minaintyg @keepIntyg @intygTillFK @skicka
Scenariomall: Skicka ett befintligt <intygKod> intyg till Försäkringskassan
	När jag går in på ett <intyg> med status "Signerat"
	Och jag skickar intyget till Försäkringskassan
	Så ska intygets status vara "Intyget är signerat och har skickats till Försäkringskassans system"

	När jag går till Mina intyg för patienten
	Så ska intygets status i Mina intyg visa "Skickat till Försäkringskassan"

@luaeFS
Exempel:
    |intygKod   | 	intyg 																|
    |LUAE_FS    | 	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"     |
@luaeNA
Exempel:
    |intygKod   | 	intyg                                                               |
    |LUAE_NA    | 	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga" |
@luse
Exempel:
    |intygKod   | 	intyg                                                               |
    |LUSE       | 	"Läkarutlåtande för sjukersättning"                                 |
    #|LISJP      | 	"Läkarintyg för sjukpenning"                                        |

@makulera
Scenario: Makulera ett skickat ett SMI-intyg
	När jag går in på ett slumpat SMI-intyg med status "Mottaget"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"

	När jag går till Mina intyg för patienten
	Så ska intygets status i Mina intyg visa "Makulerat"

@samtidaanvandare
Scenario: Samtida användare ska generera felmeddelande (SMI-intyg)
	När jag går in på att skapa ett slumpat SMI-intyg
	Och sedan öppnar intyget i två webbläsarinstanser
	Så ska ett felmeddelande visas

@samtidaanvandare @makulera
Scenario: Samtida användare ska generera felmeddelande (SMI-intyg) om intyg kopieras efter makulera
	När jag går in på att skapa ett slumpat SMI-intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag klickar på signera-knappen
	Och sedan öppnar intyget i två webbläsarinstanser
	Och jag makulerar intyget
	Så ska varningen "Kunde inte makulera intyget" visas om man försöker makulera intyget i andra webbläsarinstansen
