# language: sv

@notReady
Egenskap: Kontrollera att de olika rollerna kan logga in och utföra gällande privilegier

Bakgrund: Jag befinner mig på webcerts förstasida

Scenario: Logga in och signera intyg som Läkare
	Givet att jag är inloggad som läkare
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Så ska intygets status vara "Intyget är signerat"

# @vardadmin
Scenario: Det ska inte gå att signera intyg som en vårdadministratör
	Givet att jag är inloggad som vårdadministratör
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig

@roll @tandlakare
Scenario: Logga in och signera intyg som Tandläkare
	Givet att jag är inloggad som tandläkare
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Så ska intygets status vara "Intyget är signerat"
