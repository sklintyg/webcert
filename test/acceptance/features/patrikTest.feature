# language: sv
@patrik
Egenskap: Patrik Test

Bakgrund: Jag är inne på en patient
	#Här det börjar ske saker
	
Scenario: Patrik test scenario
	Givet att jag är inloggad som läkare "Ingrid Nilsson Olsson"
	Och jag går in på en patient
	När jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
	Så ska intygets första status vara "Obligatoriska uppgifter saknas"
	När jag fyller i alla nödvändiga fält för intyget
	Så ska intygets första status vara "Klart att signera"
	Och ska intygets andra status vara "Utkastet är sparat"
	Och jag signerar intyget
	Så ska intygets första status vara "Intyget är signerat"
	Och ska intygets andra status vara "Intyget är tillgängligt för patienten"
	När jag skickar intyget till Försäkringskassan
	Så ska intygets första status vara "Intyget är skickat till Försäkringskassan"
	Och ska intygets andra status vara "Intyget är tillgängligt för patienten"