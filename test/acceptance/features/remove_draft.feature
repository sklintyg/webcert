# language: sv
@notReady
Egenskap: Kontrollera att webcerts olika funktioner går att använda

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
@removedraft
Scenario: Radera ett utkast för läkarintyg fk7263
När jag väljer patienten "19971019-2387"
Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
Och jag går tillbaka till start
Och jag går in på ett "Läkarintyg FK 7263" med status "Utkast, uppgifter saknas"
Och jag raderar utkastet
Så ska spår av utkastet inte finnas i databasen
Och ska intyget inte finnas i intygsöversikten

@removedraft
Scenario: Radera ett utkast för Transportstyrelsens läkarintyg, diabetes
När jag väljer patienten "19971019-2387"
Och jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
Och jag går tillbaka till start
Och jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Utkast, uppgifter saknas"
Och jag raderar utkastet
Så ska spår av utkastet inte finnas i databasen
Och ska intyget inte finnas i intygsöversikten

@removedraft
Scenario: Radera ett utkast för Transportstyrelsens läkarintyg
När jag väljer patienten "19971019-2387"
Och jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
Och jag går tillbaka till start
Och jag går in på ett "Transportstyrelsens läkarintyg" med status "Utkast, uppgifter saknas"
Och jag raderar utkastet
Så ska spår av utkastet inte finnas i databasen
Och ska intyget inte finnas i intygsöversikten
