# language: sv
@sekretess @sakerhet
Egenskap: Säkerhet - Sekretessmarkerad patient

Bakgrund: 
	Givet att jag är inloggad som läkare
	Och jag går in på en patient med sekretessmarkering
	
@vårdadmin @waitingForFix @INTYG-5261 @utkast
Scenario: Kontrollera att vårdadmin inte kan se eller öppna något intygsutkast på s-markerad patient
	När jag går in på att skapa ett slumpat SMI-intyg
	
	Givet att jag är inloggad som vårdadministratör
	Och jag går in på utkastet
	Så ska jag varnas om att "Behörighet saknas"
	När jag går in på intyget med edit länken
	Så ska jag varnas om att "Behörighet saknas"

	Och jag går till ej signerade utkast
	Så ska intyget inte finnas i listan

	
@vårdadmin @waitingForFix @INTYG-5261 @signeratintyg @frågasvar
Scenario: Kontrollera att vårdadmin inte kan se eller öppna något signerat intyg eller fråga på s-markerad patient
	När jag går in på att skapa ett slumpat SMI-intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Så ska jag varnas om att "Patienten har en sekretessmarkering."
	
	När jag skickar intyget till Försäkringskassan
	Och Försäkringskassan ställer en "OVRIGT" fråga om intyget
		
	Givet att jag är inloggad som vårdadministratör
	Och jag går in på utkastet
	Så ska jag varnas om att "Behörighet saknas"
	När jag går in på intyget med edit länken
	Så ska jag varnas om att "Behörighet saknas"

	Och jag går till sidan Frågor och svar
	Så ska frågan inte finnas i listan

@makulera @smi @fornya
Scenario: Läkare ska kunna makulera intyg med s-markering
	När jag går in på ett slumpat SMI-intyg med status "Skickat"
	Så ska det finnas en knapp med texten "Förnya"
	Så ska det finnas en knapp med texten "Makulera"

	
@ts @bas @notReady
Scenario: TS-intyg utkast ska inte kunna skapas på s-markerad patient på ts bas
	När jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
	#felar på att elementet inte hittas i drop-down listan. TODO testfall ska uppdateras
	Så ska jag varnas om att "Behörighet saknas"
	
	När att vårdsystemet skapat ett intygsutkast för slumpat TS-intyg
	#detta förväntar vi oss ska fela.
	Och jag går in på intyget
	Så ska jag varnas om att "Behörighet saknas"

@ts @diabetes @notReady
Scenario: TS-intyg utkast ska inte kunna skapas på s-markerad patient på ts diabetes
	När jag går in på att skapa ett "Transportstyrelsens, diabetes" intyg
	#felar på att elementet inte hittas i drop-down listan. TODO testfall ska uppdateras
	Så ska jag varnas om att "Behörighet saknas"
	
@rehabstod @rehabKoordinator
Scenario: Rehabkoordinator ska inte kunna se sekrettessmarkerade intyg.
	Givet vårdenhet ska vara "VG_TestAutomation - TestEnhet2"
	#TSTNMT2321000156-107Q
	
	#Säkerställer att det finns ett lisjp intyg på patienten.
	När jag går in på ett "Läkarintyg för sjukpenning" med status "Skickat"
		
	#Säkerställer att det finns ett fk7263 intyg på patienten.
	Och att vårdsystemet skapat ett intygsutkast för samma patient för "Läkarintyg FK 7263"
	Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag skickar intyget till Försäkringskassan
		
	När jag är inloggad som rehabkoordinator
	Och jag väljer enhet "TSTNMT2321000156-107Q"
	Och jag går till pågående sjukfall i Rehabstöd
	Så ska jag inte se patientens personnummer bland pågående sjukfall

#LÅG PRIO / Manuella Tester:

#@ts
#Scenario: TS-intyg Översiktssidan ska inte lista intyg med sekretessmarkerade patienter
#Låg prio: Kan inte autotestas på ett enkelt sätt då vi inte vet vilka intygsidn som ska saknas eftersom att dom inte ska kunna skapas. 
#kanske går via nytt personnummer / djupintegration (Skapa på patient utan S-markering, ändra personnummer till ett sekretessmarkerat via djupintegration)

#@PU
#Scenario: PU
#Låg prio: Inte rimligt att vi auto-testar scenarion beroende på om PU är nere när vi testar parallelt ställer det till mycket problem för andra testfall.

#@Uthopp
#Scenario: Uthopp
#Inga tester krävs - inga krav påvärkar (efter att FK7263 är bortplockat)

#@Statistik
#Scenario: Statistik
#Inga tester krävs - inga krav påvärkar
