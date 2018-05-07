# language: sv
@SEKRETESSMARKERING @SAKERHET
Egenskap: Säkerhet - Sekretessmarkerad patient

Bakgrund: 
	Givet att jag är inloggad som läkare
	Och jag går in på en patient med sekretessmarkering
	
@VÅRDADMIN @UTKAST
Scenario: Kontrollera att vårdadmin inte kan se eller öppna något intygsutkast på s-markerad patient
	När jag går in på att skapa ett slumpat SMI-intyg
	
	Givet att jag är inloggad som vårdadministratör
	Och jag går in på utkastet
	Så ska jag varnas om att "Behörighet saknas"
	När jag går in på intyget med edit länken
	Så ska jag varnas om att "Behörighet saknas"

	Och jag går till ej signerade utkast
	Så ska intyget inte finnas i listan

	
@VÅRDADMIN @SIGNERATINTYG @FRÅGASVAR
Scenario: Kontrollera att vårdadmin inte kan se eller öppna något signerat intyg eller fråga på s-markerad patient
	När jag går in på att skapa ett slumpat SMI-intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Så ska jag varnas om att "Patienten har en sekretessmarkering"
	
	När jag skickar intyget till Försäkringskassan
	Och Försäkringskassan ställer en "OVRIGT" fråga om intyget
		
	Givet att jag är inloggad som vårdadministratör
	Och jag går in på utkastet
	Så ska jag varnas om att "Behörighet saknas"
	När jag går in på intyget med edit länken
	Så ska jag varnas om att "Behörighet saknas"

	Och jag går till sidan Frågor och svar
	Så ska frågan inte finnas i listan

@MAKULERA @SMI @FORNYA
Scenario: Läkare ska kunna makulera intyg med s-markering
	När jag går in på ett slumpat SMI-intyg med status "Skickat"
	Så ska det finnas en knapp med texten "Förnya"
	Så ska det finnas en knapp med texten "Makulera"

	
@TS @BAS
Scenario: TS-intyg utkast ska inte kunna skapas på s-markerad patient på ts bas
	Så ska jag inte ha alternativet att skapa "Transportstyrelsens läkarintyg" intyg
	Så ska vårdsystemet inte ha möjlighet att skapa "Transportstyrelsens läkarintyg" utkast

@TS @DIABETES
Scenario: TS-intyg utkast ska inte kunna skapas på s-markerad patient på ts diabetes
	Så ska jag inte ha alternativet att skapa "Transportstyrelsens läkarintyg, diabetes" intyg
	Så ska vårdsystemet inte ha möjlighet att skapa "Transportstyrelsens läkarintyg, diabetes" utkast
	
@REHABSTOD @REHABKOORDINATOR
Scenario: Rehabkoordinator ska inte kunna se sekrettessmarkerade intyg.
	Givet vårdenhet ska vara "TestEnhet2"
	#TSTNMT2321000156-107Q
	
	#Säkerställer att det finns ett lisjp intyg på patienten.
	När jag går in på ett "Läkarintyg för sjukpenning" med status "Skickat"
		
	#Säkerställer att det finns ett fk7263 intyg på patienten.
	#@LegacyFK7263
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
#Låg prio: Inte rimligt att vi auto-testar scenarion beroende på om PU är nere när vi testar parallelt ställer det till mycket problem för andra testfall om vi stänger ned PU-tjänsten.

#@Uthopp
#Scenario: Uthopp
#Inga tester krävs - inga krav påverkar (efter att FK7263 är bortplockat)

#@Statistik
#Scenario: Statistik
#Inga tester krävs - inga krav påverkar
