# language: sv

@rehabstod @fk7263
Egenskap: FK7263-integration med rehabstöd

Bakgrund: Jag befinner mig på Rehabstöds förstasida

@integration @skapa
Scenario: Ett nyskapat FK7263 intyg ska finnas i Rehabstöd
    När jag är inloggad som läkare i Rehabstöd
    Och jag väljer enhet "TSTNMT2321000156-107P"
    Och jag klickar på knappen "Visa pågående sjukfall" i Rehabstöd
    Och jag söker efter slumpvald patient och sparar antal intyg

    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107P"
    Och jag går in på en patient som sparats från Rehabstöd
    Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
    Och jag fyller i ett "FK7263" intyg som inte är smitta med ny sjukskrivningsperiod
    Och jag signerar intyget

    När jag går in på Rehabstöd
    Och jag är inloggad som läkare i Rehabstöd
    Och jag väljer enhet "TSTNMT2321000156-107P"
    Och jag klickar på knappen "Visa pågående sjukfall" i Rehabstöd
    Så ska antalet intyg ökat med 1 på patient som sparats från Rehabstöd

@integration @makulera
Scenario: Ett makulerat FK7263 intyg ska tas bort från Rehabstöd
    När jag är inloggad som läkare i Rehabstöd
    Och jag väljer enhet "TSTNMT2321000156-107P"
    Och jag klickar på knappen "Visa pågående sjukfall" i Rehabstöd
    Och jag söker efter slumpvald patient och sparar antal intyg

    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107P"
    Och jag går in på en patient som sparats från Rehabstöd
    Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
    Och jag fyller i ett "FK7263" intyg som inte är smitta med ny sjukskrivningsperiod
    Och jag signerar intyget

    När jag går in på Rehabstöd
    Och jag är inloggad som läkare i Rehabstöd
    Och jag väljer enhet "TSTNMT2321000156-107P"
    Och jag klickar på knappen "Visa pågående sjukfall" i Rehabstöd
    Så ska antalet intyg ökat med 1 på patient som sparats från Rehabstöd

    När jag är inloggad som läkare i Webcert med enhet "TSTNMT2321000156-107P"
    Och jag går in på intyget som tidigare skapats
    Och jag makulerar intyget

    När jag går in på Rehabstöd
    Och jag är inloggad som läkare i Rehabstöd
    Och jag väljer enhet "TSTNMT2321000156-107P"
    Och jag klickar på knappen "Visa pågående sjukfall" i Rehabstöd
    Så ska antalet intyg ökat med 0 på patient som sparats från Rehabstöd
