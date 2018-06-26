# language: sv

@REHABSTOD @fk7263 @NOTREADY @DEPRICATED @MOVE_TO_SMI
Egenskap: FK7263-integration med rehabstöd

Bakgrund: Jag befinner mig på Rehabstöds förstasida

@INTEGRATION
Scenario: Ett nyskapat FK7263 intyg ska finnas i Rehabstöd
    När jag är inloggad som läkare i Rehabstöd
    Och jag väljer enhet "TSTNMT2321000156-107P"
    Och jag går till pågående sjukfall i Rehabstöd
    Och jag söker efter slumpvald patient och sparar antal intyg

    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107P"
    Och jag går in på en patient som sparats från Rehabstöd
    Givet att vårdsystemet skapat ett intygsutkast för samma patient för "Läkarintyg FK 7263" 
	Och jag går in på utkastet
    Och jag fyller i ett "FK7263" intyg som inte är smitta med ny sjukskrivningsperiod
    Och jag signerar intyget

    När jag går in på Rehabstöd
    Och jag är inloggad som läkare i Rehabstöd
    Och jag väljer enhet "TSTNMT2321000156-107P"
    Och jag går till pågående sjukfall i Rehabstöd
    Så ska antalet intyg ökat med 1 på patient som sparats från Rehabstöd

@INTEGRATION @MAKULERA
Scenario: Ett makulerat FK7263 intyg ska tas bort från Rehabstöd
    När jag är inloggad som läkare i Rehabstöd
    Och jag väljer enhet "TSTNMT2321000156-107P"
    Och jag går till pågående sjukfall i Rehabstöd
    Och jag söker efter slumpvald patient och sparar antal intyg

    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107P"
    Och jag går in på en patient som sparats från Rehabstöd
    Givet att vårdsystemet skapat ett intygsutkast för samma patient för "Läkarintyg FK 7263"  
	Och jag går in på utkastet
    Och jag fyller i ett "FK7263" intyg som inte är smitta med ny sjukskrivningsperiod
    Och jag signerar intyget

    När jag går in på Rehabstöd
    Och jag är inloggad som läkare i Rehabstöd
    Och jag väljer enhet "TSTNMT2321000156-107P"
    Och jag går till pågående sjukfall i Rehabstöd
    Så ska antalet intyg ökat med 1 på patient som sparats från Rehabstöd

    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107P"
    Och jag går in på intyget som tidigare skapats
    Och jag makulerar intyget

    När jag går in på Rehabstöd
    Och jag är inloggad som läkare i Rehabstöd
    Och jag väljer enhet "TSTNMT2321000156-107P"
    Och jag går till pågående sjukfall i Rehabstöd
    Så ska antalet intyg ökat med 0 på patient som sparats från Rehabstöd
