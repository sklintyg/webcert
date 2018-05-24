# language: sv
@SMI @HANTERA
Egenskap: Hantera SMI intyg

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

 @SIGNERA
Scenariomall: Skapa och signera ett intyg för <intygKod>
	När jag går in på att skapa ett <intyg> intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets första status vara "Intyget är signerat"

	När jag går till Mina intyg för patienten
	Så ska intyget finnas i Mina intyg

	Och jag loggar ut ur Mina intyg
	#När jag går in på intyget i Mina intyg
	#Så ska intygets information i Mina intyg vara den jag angett

@LUSE
Exempel:
    |intygKod   | 	intyg 																|
    |LUSE       | 	"Läkarutlåtande för sjukersättning"                                 |

@LUAENA
Exempel:
    |intygKod   | 	intyg 																|
    |LUAE_NA    | 	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga" |

@LISJP
Exempel:
    |intygKod   | 	intyg 																|
    |LISJP      | 	"Läkarintyg för sjukpenning"                                        |

@FKSMOKE
Exempel:
    |intygKod   | 	intyg                                                               |
    |LUAE_NA    | 	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga" |
    |LUAE_FS    | 	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"     |

@MINAINTYG  @INTYGTILLFK @SKICKA @MI-F010
Scenariomall: Skicka ett befintligt <intygKod> intyg till Försäkringskassan
	När jag går in på ett <intyg> med status "Signerat"
	Och jag skickar intyget till Försäkringskassan
	Så ska intygets första status vara "Intyget är skickat till Försäkringskassan"
	Och ska intygets andra status vara "Intyget är tillgängligt för patienten"

	När jag går till Mina intyg för patienten
	Så ska intygets status i Mina intyg visa "Skickat till Försäkringskassan"

@LUAEFS
Exempel:
    |intygKod   | 	intyg 																|
    |LUAE_FS    | 	"Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"     |
@LUAENA
Exempel:
    |intygKod   | 	intyg                                                               |
    |LUAE_NA    | 	"Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga" |
@LUSE
Exempel:
    |intygKod   | 	intyg                                                               |
    |LUSE       | 	"Läkarutlåtande för sjukersättning"                                 |
    #|LISJP      | 	"Läkarintyg för sjukpenning"                                        |
	#TODO undersök om vi kan inkludera LISJP i detta scenario

@MAKULERA @SMOKE @FKSMOKE
Scenario: Makulera ett skickat ett SMI-intyg
	När jag går in på ett slumpat SMI-intyg med status "Skickat"
	Och jag makulerar intyget
	Så ska intygets första status vara "Intyget är makulerat"

	När jag går till Mina intyg för patienten
    Så ska intyget inte finnas i Mina intyg

@SAMTIDAANVANDARE @IS-010
Scenario: IS-010 - Samtida användare ska generera felmeddelande (SMI-intyg)
	När jag går in på att skapa ett slumpat SMI-intyg
	Och sedan öppnar intyget i två webbläsarinstanser
	Så ska ett felmeddelande visas

@SAMTIDAANVANDARE @MAKULERA @SMOKE
#Krav diskutteras i kommentarfältet "Felmeddelanden, Webcert" - "Generella"
Scenario: Samtida användare ska generera felmeddelande (SMI-intyg) efter att intyg blivit makulerat
	När jag går in på att skapa ett slumpat SMI-intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag klickar på signera-knappen
	Och sedan öppnar intyget i två webbläsarinstanser
	Och jag makulerar intyget
	Så ska varningen "Kunde inte makulera intyget" visas om man försöker makulera intyget i andra webbläsarinstansen

@SAMTIDAANVANDARE @SKICKA-MAKULERA @NOTREADY
Scenario: Samtida användare ska generera felmeddelande om fråga/svar skickas efter makulering (LISJP)
		När jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag klickar på signera-knappen
		Och sedan öppnar intyget i två webbläsarinstanser
		Och jag klickar på skicka knappen
		Och jag makulerar intyget
		Och jag skickar en fråga till Försäkringskassan
		Så ska varningen "Förmodligen har en annan användare makulerat intyget medan du arbetat på samma post. Ladda om sidan och försök igen" visas
