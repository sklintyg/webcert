# language: sv

@ListCertificatesForCareWithQA
Egenskap: ListCertificatesForCareWithQA från journalsystem

@smoke
Scenario: Hämta en lista med intyg och utkast för en specifik vårdenhet och patient
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
    Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    Och jag skickar en ListCertificateForCareWithQA för patienten och vårdenheten

    Så ska svaret innehålla intyget jag var inne på
    Och ska svaret endast innehålla intyg för utvald patient
    Och ska svaret endast innehålla intyg för vårdenheten