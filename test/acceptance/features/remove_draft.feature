# language: sv
@smoke @webcert
Egenskap: Kontrollera att webcerts olika funktioner går att använda

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare "Jan Nilsson"

@removeDraft
Scenario: Skapa ett intyg till transportstyrelsen
När jag väljer patienten "19520617-2339"
Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
Och jag går tillbaka till start
Och jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
Och jag går tillbaka till start
Och jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
Och jag går tillbaka till start

@removeDraft
Scenario: Radera ett utkast för läkarintyg fk7263
När jag väljer patienten "19520617-2339"
  Och jag går in på ett "Läkarintyg FK 7263" med status "Utkast, uppgifter saknas"
Och jag raderar utkastet
 Så ska intyget "Läkarintyg FK 7263" med status "Utkast, uppgifter saknas" inte synas mer

@removeDraft
Scenario: Radera ett utkast för Transportstyrelsens läkarintyg, diabetes
När jag väljer patienten "19520617-2339"
  Och jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Utkast, uppgifter saknas"
Och jag raderar utkastet
 Så ska intyget "Transportstyrelsens läkarintyg, diabetes" med status "Utkast, uppgifter saknas" inte synas mer

@removeDraft
Scenario: Radera ett utkast för Transportstyrelsens läkarintyg
När jag väljer patienten "19520617-2339"
  Och jag går in på ett "Transportstyrelsens läkarintyg" med status "Utkast, uppgifter saknas"
Och jag raderar utkastet
 Så ska intyget "Transportstyrelsens läkarintyg, diabetes" med status "Utkast, uppgifter saknas" inte synas mer
