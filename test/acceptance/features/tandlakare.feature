# language: sv

@tandlakare
Egenskap: Tandläkare

# Bakgrund: Jag befinner mig på webcerts förstasida

Scenario: Ska endast kunna hantera FK7263
	Givet att jag är inloggad som läkare på vårdenhet "TSTNMT2321000156-107Q"
	När jag går in på en patient
	Och det finns ett "Transportstyrelsens läkarintyg"
	Och det finns ett "Transportstyrelsens läkarintyg, diabetes"
	Och det finns ett "Läkarutlåtande för sjukersättning" 

	Givet att jag är inloggad som tandläkare på vårdenhet "TSTNMT2321000156-107Q"
	När jag går in på patienten
	Så ska jag inte se intyg av annan typ än "Läkarintyg FK 7263,Läkarintyg för sjukpenning"
	Och jag ska endast ha möjlighet att skapa nya "Läkarintyg FK 7263,Läkarintyg för sjukpenning" utkast

	När jag går till ej signerade utkast
	Så ska jag inte se utkast av annan typ än "Läkarintyg FK 7263,Läkarintyg för sjukpenning"

@fk7263 @signera @skicka @makulera @kopiera
Scenario: Skapa, Skicka och Makulera FK7263
	Givet att jag är inloggad som tandläkare
	När jag går in på en patient
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets status vara "Intyget är signerat"

	När jag skickar intyget till Försäkringskassan
	Så ska intygets status vara "Intyget är signerat och har skickats till Försäkringskassans system."

	Så är kopieraknappen tillgänglig

	När jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"


Scenario: Svara och skicka fråga till Försäkringskassan
   Givet att jag är inloggad som tandläkare
   När jag går in på en patient
   Och jag går in på ett "Läkarintyg FK 7263" med status "Skickat"
   Och Försäkringskassan har ställt en "Avstamningsmote" fråga om intyget
   Så ska jag ha möjlighet att vidarebefordra frågan

   När jag svarar på frågan
   Så kan jag se mitt svar under hanterade frågor

	När jag skickar en fråga med ämnet "Arbetstidsförläggning" till Försäkringskassan
	Så ska ett info-meddelande visa "Frågan är skickad till Försäkringskassan"
	Och ska jag se min fråga under ohanterade frågor


