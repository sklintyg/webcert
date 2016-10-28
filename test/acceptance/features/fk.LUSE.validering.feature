# language: sv
@luse
Egenskap: Validering av text i intyget Läkarutlåtande för sjukersättning

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som djupintegrerad läkare
	När jag går in på en patient

@adressbyte @namnbyte
Scenario: Som integrerad användare vill jag veta om patienten bytt namn eller adress
    När jag går in på att skapa ett "Läkarutlåtande för sjukersättning" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annat namn och adress
    Så ska meddelande visas att man ska "Observera att patientens namn och adress har ändrats sedan det här intyget utfärdades."
