# language: sv

@TANDLAKARE
Egenskap: Tandläkare

# Bakgrund: Jag befinner mig på webcerts förstasida

Scenario: Ska endast kunna hantera Läkarintyg för sjukpenning
	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-107Q"
	När jag går in på en patient
	Och det finns ett "Transportstyrelsens läkarintyg"
	Och det finns ett "Transportstyrelsens läkarintyg, diabetes"
	Och det finns ett "Läkarutlåtande för sjukersättning" 

	Givet att jag är inloggad som tandläkare på vårdenhet "TSTNMT2321000156-107Q"
	När jag går in på patienten
	#Uppdatera när @LegacyFK7263 försvinner
	Så ska jag inte se intyg av annan typ än "Läkarintyg FK 7263,Läkarintyg för sjukpenning"
	Och jag ska endast se intygstyperna "Läkarintyg FK 7263,Läkarintyg för sjukpenning" i Skapa intyg listan

	När jag går till ej signerade utkast
	#Uppdatera när @LegacyFK7263 försvinner
	Så ska jag inte se utkast av annan typ än "Läkarintyg FK 7263,Läkarintyg för sjukpenning"

@LISJP @SIGNERA @SKICKA @MAKULERA @FORNYA
Scenario: Skapa, Skicka och Makulera Läkarintyg för sjukpenning
	Givet att jag är inloggad som tandläkare
	När jag går in på en patient
	Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg för sjukpenning"
	Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets första status vara "Intyget är signerat"

	När jag skickar intyget till Försäkringskassan
	Så ska intygets första status vara "Intyget är skickat till Försäkringskassan"
	Och ska intygets andra status vara "Intyget är tillgängligt för patienten"

	Så ska det finnas en knapp för att förnya intyget

	När jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"

@LISJP @FRAGASVAR @FRÅGA-FRÅN-FK
Scenario: Svara och skicka fråga till Försäkringskassan
	Givet att jag är inloggad som tandläkare
	Och att vårdsystemet skapat ett intygsutkast för "Läkarintyg för sjukpenning"
	Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag skickar intyget till Försäkringskassan 
	Och Försäkringskassan skickar ett "AVSTMN" meddelande på intyget
	Så ska jag ha möjlighet att vidarebefordra frågan

	När jag svarar på frågan
	Så kan jag se mitt svar i högerfältet

	När jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
	Och ska jag se min fråga som ohanterad