# language: sv
@sekretess @sakerhet
Egenskap: Säkerhet - Sekretessmarkerad patient

Bakgrund: 
	Givet att jag är inloggad som läkare
	Och jag går in på en patient med sekretessmarkering
	
@vårdadmin @utkast
Scenario: Kontrollera att vårdadmin inte kan se eller öppna något intygsutkast på s-markerad patient
	När jag går in på att skapa ett slumpat SMI-intyg
	
	Givet att jag är inloggad som vårdadministratör
	Och jag går in på utkastet
	Så ska jag varnas om att "Behörighet saknas"
	När jag går in på intyget med edit länken
	Så ska jag varnas om att "Behörighet saknas"

	Och jag går till ej signerade utkast
	Så ska intyget inte finnas i listan

	
@vårdadmin @signeratintyg @frågasvar
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

	
@ts @bas
Scenario: TS-intyg utkast ska inte kunna skapas på s-markerad patient på ts bas
	När jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
	Så ska jag varnas om att "Behörighet saknas"
	
	När att vårdsystemet skapat ett intygsutkast för slumpat TS-intyg
	#detta förväntar vi oss ska fela.
	Och jag går in på intyget
	Så ska jag varnas om att "Behörighet saknas"

@ts @diabetes
Scenario: TS-intyg utkast ska inte kunna skapas på s-markerad patient på ts diabetes
	När jag går in på att skapa ett "Transportstyrelsens, diabetes" intyg
	Så ska jag varnas om att "Behörighet saknas"
	

#@djupintegration
#Scenario: Djupintegrerat: SJF flaggan ger inte några extra rättigheter om patienten är sekrettessmarkerad.
#Går in på vårdenhet A
#Går in på patient som är sekretessmarkerad
#Skapar intyg x
#Går in på vårdenhet B med SJF=true
#Så ska ett felmedelande visas

#@Rehabstod
#Scenario: Rehabkordinator ska inte kunna se sekrettessmarkerade intyg.
#Logga in som läkare, så ska du se S-markerat intyg med namn: "Sekretessmarkerad patient"
#Logga in som Rehabkordinator, så ska du inte se intyget

#LÅG PRIO:

#@ts
#Scenario: TS-intyg Översiktssidan ska inte lista intyg med sekretessmarkerade patienter
#Låg prio: Kan inte autotestas på ett enkelt sätt då vi inte vet vilka intygsidn som ska saknas eftersom att dom inte ska kunna skapas. 
#kanske går via nytt personnummer / djupintegration (Skapa på patient utan S-markering, ändra personnummer till ett sekretessmarkerat via djupintegration)

#@PU
#Scenario: PU
#Låg prio: Inte rimligt att vi auto-testar scenarion beroende på om PU är nere när vi testar parallelt ställer det till mycket problem för andra testfall.

#@Uthopp
#Scenario: Uthopp
#Testa att intyg som är skapat med registerCertificate och skickat till FK kan hämtas från intygstjänsten och visas i webcert. #kristina

#@Statistik
#Scenario: Statistik
#Inga tester krävs - inga krav påvärkar
