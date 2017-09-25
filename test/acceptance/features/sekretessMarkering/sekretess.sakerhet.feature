# language: sv
@sekretess @sakerhet
Egenskap: Säkerhet - Sekretessmarkerad patient

#@tagg
#Scenario: 
#Vårdadmin : Intyg ska försvinna ur ej signerade utkast listan
#Vårdadmin : Intyg ska försvinna ur fråga&svar listan


#@tagg
#Scenario: 
#Vårdadmin ska inte kunna se intyget via visa länk
#Vårdadmin ska inte kunna se intyget via edit länk

@makulera @smi @fornya
Scenario: Läkare ska kunna makulera intyg med s-markering
	Givet att jag är inloggad som läkare
	Och jag går in på en patient med sekretessmarkering
	När jag går in på ett slumpat SMI-intyg med status "Skickat"
	Så ska det finnas en knapp med texten "Förnya"
	Så ska det finnas en knapp med texten "Makulera"

#@tagg
#Scenario: 
#TS-intyg utkast ska inte kunna skapas med createDraft
#TS-intyg utkast ska inte kunna skapas via webcerts UI (fristående)

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
#Inga tester krävs - inga krav påvärkar

#@Statistik
#Scenario: Statistik
#Inga tester krävs - inga krav påvärkar
