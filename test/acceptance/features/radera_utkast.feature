# language: sv
@notReady
Egenskap: Radera intygsutkast

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	
@removedraft @lisjp
Scenario: Radera ett utkast för Läkarintyg för sjukpenning
	När jag går in på en patient
	Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg för sjukpenning" 
	Och jag går in på utkastet
	Och jag går tillbaka till start
	Och jag går in på ett "Läkarintyg för sjukpenning" med status "Utkast, uppgifter saknas"
	Och jag raderar utkastet
	Så ska spår av utkastet inte finnas i databasen
	Och ska intyget inte finnas i intygsöversikten

@removedraft @ts @tsdiabetes
Scenario: Radera ett utkast för Transportstyrelsens läkarintyg, diabetes
	När jag går in på en patient
	Och jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
	Och jag går tillbaka till start
	Och jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Utkast, uppgifter saknas"
	Och jag raderar utkastet
	Så ska spår av utkastet inte finnas i databasen
	Och ska intyget inte finnas i intygsöversikten

@removedraft @ts @tsbas
Scenario: Radera ett utkast för Transportstyrelsens läkarintyg
	När jag går in på en patient
	Och jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
	Och jag går tillbaka till start
	Och jag går in på ett "Transportstyrelsens läkarintyg" med status "Utkast, uppgifter saknas"
	Och jag raderar utkastet
	Så ska spår av utkastet inte finnas i databasen
	Och ska intyget inte finnas i intygsöversikten
