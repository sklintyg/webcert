# language: sv
@luse
Egenskap: Validering av texter i Läkarutlåtande för sjukersättning

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som djupintegrerad läkare
	När jag går in på en patient
	Och jag går in på att skapa ett "Läkarutlåtande för sjukersättning" intyg

@adressbyte @namnbyte
Scenario: Som integrerad användare vill jag veta om patienten bytt namn eller adress
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annat namn och adress
    Så ska meddelande visas att man ska "Observera att patientens namn och adress har ändrats sedan det här intyget utfärdades."

@notReady
Scenario: Validera felaktig diagnoskod i LUSE
	När jag fyller i "000" som diagnoskod
	Så ska valideringsfelet "är ej giltig" visas
