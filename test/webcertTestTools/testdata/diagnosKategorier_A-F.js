/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

var diagnosKategorier = [{
        diagnosKod: 'A00',
        beskrivning: 'Kolera'
    },
    {
        diagnosKod: 'A01',
        beskrivning: 'Tyfoidfeber och paratyfoidfeber'
    },
    {
        diagnosKod: 'A02',
        beskrivning: 'Andra salmonellainfektioner'
    },
    {
        diagnosKod: 'A03',
        beskrivning: 'Shigellos (bakteriell dysenteri, rodsot)'
    },
    {
        diagnosKod: 'A04',
        beskrivning: 'Andra bakteriella tarminfektioner'
    },
    {
        diagnosKod: 'A05',
        beskrivning: 'Annan matforgiftning orsakad av bakterier som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'A06',
        beskrivning: 'Amobainfektion'
    },
    {
        diagnosKod: 'A07',
        beskrivning: 'Andra protozosjukdomar i tarmen'
    },
    {
        diagnosKod: 'A08',
        beskrivning: 'Tarminfektioner orsakade av virus och andra specificerade organismer'
    },
    {
        diagnosKod: 'A09',
        beskrivning: 'Annan gastroenterit och kolit av infektios och ospecificerad orsak'
    },
    {
        diagnosKod: 'A15',
        beskrivning: 'Tuberkulos i andningsorganen, bakteriologiskt och histologiskt verifierad'
    },
    {
        diagnosKod: 'A16',
        beskrivning: 'Tuberkulos i andningsorganen, ej verifierad bakteriologiskt eller histologiskt'
    },
    {
        diagnosKod: 'A17',
        beskrivning: 'Tuberkulos i nervsystemet'
    },
    {
        diagnosKod: 'A18',
        beskrivning: 'Tuberkulos i andra organ'
    },
    {
        diagnosKod: 'A19',
        beskrivning: 'Miliartuberkulos (utspridd tuberkulos)'
    },
    {
        diagnosKod: 'A20',
        beskrivning: 'Pest'
    },
    {
        diagnosKod: 'A21',
        beskrivning: 'Tularemi (harpest)'
    },
    {
        diagnosKod: 'A22',
        beskrivning: 'Mjaltbrand'
    },
    {
        diagnosKod: 'A23',
        beskrivning: 'Undulantfeber'
    },
    {
        diagnosKod: 'A24',
        beskrivning: 'Rots och melioidos'
    },
    {
        diagnosKod: 'A25',
        beskrivning: 'Rattbettsfeber'
    },
    {
        diagnosKod: 'A26',
        beskrivning: 'Erysipeloid'
    },
    {
        diagnosKod: 'A27',
        beskrivning: 'Leptospiros'
    },
    {
        diagnosKod: 'A28',
        beskrivning: 'Andra djurburna bakteriesjukdomar som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'A30',
        beskrivning: 'Lepra'
    },
    {
        diagnosKod: 'A31',
        beskrivning: 'Sjukdomar orsakade av andra mykobakterier'
    },
    {
        diagnosKod: 'A32',
        beskrivning: 'Listerios'
    },
    {
        diagnosKod: 'A33',
        beskrivning: 'Stelkramp hos nyfodd'
    },
    {
        diagnosKod: 'A34',
        beskrivning: 'Obstetrisk stelkramp'
    },
    {
        diagnosKod: 'A35',
        beskrivning: 'Annan stelkramp'
    },
    {
        diagnosKod: 'A36',
        beskrivning: 'Difteri'
    },
    {
        diagnosKod: 'A37',
        beskrivning: 'Kikhosta'
    },
    {
        diagnosKod: 'A38',
        beskrivning: 'Scharlakansfeber'
    },
    {
        diagnosKod: 'A39',
        beskrivning: 'Meningokockinfektion'
    },
    {
        diagnosKod: 'A40',
        beskrivning: 'Sepsis orsakad av streptokocker'
    },
    {
        diagnosKod: 'A41',
        beskrivning: 'Annan sepsis'
    },
    {
        diagnosKod: 'A42',
        beskrivning: 'Aktinomykos (stralsvamp)'
    },
    {
        diagnosKod: 'A43',
        beskrivning: 'Nokardios'
    },
    {
        diagnosKod: 'A44',
        beskrivning: 'Bartonellos'
    },
    {
        diagnosKod: 'A46',
        beskrivning: 'Rosfeber'
    },
    {
        diagnosKod: 'A48',
        beskrivning: 'Andra bakteriesjukdomar som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'A49',
        beskrivning: 'Bakterieinfektion med icke specificerad lokalisation'
    },
    {
        diagnosKod: 'A50',
        beskrivning: 'Medfodd syfilis'
    },
    {
        diagnosKod: 'A51',
        beskrivning: 'Tidig syfilis'
    },
    {
        diagnosKod: 'A52',
        beskrivning: 'Sen syfilis'
    },
    {
        diagnosKod: 'A53',
        beskrivning: 'Annan och icke specificerad syfilis'
    },
    {
        diagnosKod: 'A54',
        beskrivning: 'Gonokockinfektion'
    },
    {
        diagnosKod: 'A55',
        beskrivning: 'Lymfogranulom (veneriskt) orsakat av klamydia'
    },
    {
        diagnosKod: 'A56',
        beskrivning: 'Andra sexuellt overforda klamydiasjukdomar'
    },
    {
        diagnosKod: 'A57',
        beskrivning: 'Chankroid (mjuk schanker)'
    },
    {
        diagnosKod: 'A58',
        beskrivning: 'Granuloma inguinale'
    },
    {
        diagnosKod: 'A59',
        beskrivning: 'Trikomonasinfektion'
    },
    {
        diagnosKod: 'A60',
        beskrivning: 'Anogenital infektion med herpes simplex-virus'
    },
    {
        diagnosKod: 'A63',
        beskrivning: 'Andra huvudsakligen sexuellt overforda sjukdomar som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'A64',
        beskrivning: 'Icke specificerad sexuellt overford sjukdom'
    },
    {
        diagnosKod: 'A65',
        beskrivning: 'Icke venerisk syfilis'
    },
    {
        diagnosKod: 'A66',
        beskrivning: 'Yaws'
    },
    {
        diagnosKod: 'A67',
        beskrivning: 'Pinta'
    },
    {
        diagnosKod: 'A68',
        beskrivning: 'aterfallsfeber'
    },
    {
        diagnosKod: 'A69',
        beskrivning: 'Andra spiroketinfektioner'
    },
    {
        diagnosKod: 'A70',
        beskrivning: 'Infektion orsakad av Chlamydia psittaci (papegojsjuka)'
    },
    {
        diagnosKod: 'A71',
        beskrivning: 'Trakom'
    },
    {
        diagnosKod: 'A74',
        beskrivning: 'Andra sjukdomar orsakade av klamydier'
    },
    {
        diagnosKod: 'A75',
        beskrivning: 'Flacktyfus overford av loss, loppor och kvalster'
    },
    {
        diagnosKod: 'A77',
        beskrivning: 'Rickettsiasjukdom overford av fastingar'
    },
    {
        diagnosKod: 'A78',
        beskrivning: 'Q-feber'
    },
    {
        diagnosKod: 'A79',
        beskrivning: 'Andra rickettsiasjukdomar'
    },
    {
        diagnosKod: 'A80',
        beskrivning: 'Akut polio (barnforlamning)'
    },
    {
        diagnosKod: 'A81',
        beskrivning: 'Atypisk virusinfektion i centrala nervsystemet'
    },
    {
        diagnosKod: 'A82',
        beskrivning: 'Rabies (vattuskrack)'
    },
    {
        diagnosKod: 'A83',
        beskrivning: 'Virusencefalit overford av myggor'
    },
    {
        diagnosKod: 'A84',
        beskrivning: 'Virusencefalit overford av fastingar'
    },
    {
        diagnosKod: 'A85',
        beskrivning: 'Andra virusencefaliter som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'A86',
        beskrivning: 'Icke specificerad virusencefalit'
    },
    {
        diagnosKod: 'A87',
        beskrivning: 'Virusmeningit'
    },
    {
        diagnosKod: 'A88',
        beskrivning: 'Andra virusinfektioner i centrala nervsystemet som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'A89',
        beskrivning: 'Icke specificerad virusinfektion i centrala nervsystemet'
    },
    {
        diagnosKod: 'A92',
        beskrivning: 'Andra febersjukdomar an dengue orsakade av virus overforda av myggor'
    },
    {
        diagnosKod: 'A93',
        beskrivning: 'Andra febersjukdomar orsakade av virus overforda av leddjur, som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'A94',
        beskrivning: 'Icke specificerad febersjukdom orsakad av virus overfort av leddjur'
    },
    {
        diagnosKod: 'A95',
        beskrivning: 'Gula febern'
    },
    {
        diagnosKod: 'A96',
        beskrivning: 'Arenaviral hemorragisk feber'
    },
    {
        diagnosKod: 'A97',
        beskrivning: 'Dengue'
    },
    {
        diagnosKod: 'A98',
        beskrivning: 'Andra hemorragiska febersjukdomar orsakade av virus som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'A99',
        beskrivning: 'Icke specificerad hemorragisk febersjukdom orsakad av virus'
    },
    {
        diagnosKod: 'B00',
        beskrivning: 'Herpes simplex-infektioner'
    },
    {
        diagnosKod: 'B01',
        beskrivning: 'Vattkoppor'
    },
    {
        diagnosKod: 'B02',
        beskrivning: 'Baltros'
    },
    {
        diagnosKod: 'B03',
        beskrivning: 'Smittkoppor'
    },
    {
        diagnosKod: 'B04',
        beskrivning: 'Apkoppor'
    },
    {
        diagnosKod: 'B05',
        beskrivning: 'Massling'
    },
    {
        diagnosKod: 'B06',
        beskrivning: 'Roda hund'
    },
    {
        diagnosKod: 'B07',
        beskrivning: 'Virusvartor'
    },
    {
        diagnosKod: 'B08',
        beskrivning: 'Andra virussjukdomar med hud- och slemhinneutslag som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'B09',
        beskrivning: 'Icke specificerad virusinfektion med hud- och slemhinneutslag'
    },
    {
        diagnosKod: 'B15',
        beskrivning: 'Akut hepatit A'
    },
    {
        diagnosKod: 'B16',
        beskrivning: 'Akut hepatit B'
    },
    {
        diagnosKod: 'B17',
        beskrivning: 'Annan akut virushepatit'
    },
    {
        diagnosKod: 'B18',
        beskrivning: 'Kronisk virushepatit'
    },
    {
        diagnosKod: 'B19',
        beskrivning: 'Icke specificerad virushepatit'
    },
    {
        diagnosKod: 'B20',
        beskrivning: 'Sjukdom orsakad av humant immunbristvirus [HIV] tillsammans med infektions- och parasitsjukdom'
    },
    {
        diagnosKod: 'B21',
        beskrivning: 'Sjukdom orsakad av humant immunbristvirus [HIV] tillsammans med maligna tumorer'
    },
    {
        diagnosKod: 'B22',
        beskrivning: 'Sjukdom orsakad av humant immunbristvirus [HIV] tillsammans med andra specificerade sjukdomar'
    },
    {
        diagnosKod: 'B23',
        beskrivning: 'Sjukdom orsakad av humant immunbristvirus [HIV] tillsammans med andra tillstand'
    },
    {
        diagnosKod: 'B24',
        beskrivning: 'Icke specificerad sjukdom orsakad av humant immunbristvirus [HIV]'
    },
    {
        diagnosKod: 'B25',
        beskrivning: 'Cytomegalvirussjukdom'
    },
    {
        diagnosKod: 'B26',
        beskrivning: 'Passjuka'
    },
    {
        diagnosKod: 'B27',
        beskrivning: 'Kortelfeber'
    },
    {
        diagnosKod: 'B30',
        beskrivning: 'Viruskonjunktivit'
    },
    {
        diagnosKod: 'B33',
        beskrivning: 'Andra virussjukdomar som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'B34',
        beskrivning: 'Virussjukdom med icke specificerad lokalisation'
    },
    {
        diagnosKod: 'B35',
        beskrivning: 'Dermatofytos (hudsvampsjukdom)'
    },
    {
        diagnosKod: 'B36',
        beskrivning: 'Andra ytliga mykoser'
    },
    {
        diagnosKod: 'B37',
        beskrivning: 'Candidainfektion'
    },
    {
        diagnosKod: 'B38',
        beskrivning: 'Koccidioidomykos'
    },
    {
        diagnosKod: 'B39',
        beskrivning: 'Histoplasmos'
    },
    {
        diagnosKod: 'B40',
        beskrivning: 'Blastomykos'
    },
    {
        diagnosKod: 'B41',
        beskrivning: 'Parakoccidioidomykos'
    },
    {
        diagnosKod: 'B42',
        beskrivning: 'Sporotrikos'
    },
    {
        diagnosKod: 'B43',
        beskrivning: 'Kromomykos och feomykotisk abscess'
    },
    {
        diagnosKod: 'B44',
        beskrivning: 'Aspergillos'
    },
    {
        diagnosKod: 'B45',
        beskrivning: 'Kryptokockos'
    },
    {
        diagnosKod: 'B46',
        beskrivning: 'Zygomykos'
    },
    {
        diagnosKod: 'B47',
        beskrivning: 'Mycetom'
    },
    {
        diagnosKod: 'B48',
        beskrivning: 'Andra mykoser som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'B49',
        beskrivning: 'Icke specificerad mykos'
    },
    {
        diagnosKod: 'B50',
        beskrivning: 'Malaria orsakad av Plasmodium falciparum'
    },
    {
        diagnosKod: 'B51',
        beskrivning: 'Malaria orsakad av Plasmodium vivax'
    },
    {
        diagnosKod: 'B52',
        beskrivning: 'Malaria orsakad av Plasmodium malariae'
    },
    {
        diagnosKod: 'B53',
        beskrivning: 'Annan parasitologiskt verifierad malaria'
    },
    {
        diagnosKod: 'B54',
        beskrivning: 'Malaria, ospecificerad'
    },
    {
        diagnosKod: 'B55',
        beskrivning: 'Leishmanios'
    },
    {
        diagnosKod: 'B56',
        beskrivning: 'Afrikansk trypanosomiasis (somnsjuka)'
    },
    {
        diagnosKod: 'B57',
        beskrivning: 'Chagas sjukdom'
    },
    {
        diagnosKod: 'B58',
        beskrivning: 'Toxoplasmos'
    },
    {
        diagnosKod: 'B59',
        beskrivning: 'Pneumocystos'
    },
    {
        diagnosKod: 'B60',
        beskrivning: 'Andra protozosjukdomar som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'B64',
        beskrivning: 'Icke specificerad protozosjukdom'
    },
    {
        diagnosKod: 'B65',
        beskrivning: 'Schistosomiasis'
    },
    {
        diagnosKod: 'B66',
        beskrivning: 'Andra trematodinfektioner (infektioner med flundror och andra sugmaskar)'
    },
    {
        diagnosKod: 'B67',
        beskrivning: 'Blasmasksjuka'
    },
    {
        diagnosKod: 'B68',
        beskrivning: 'Taeniainfektion'
    },
    {
        diagnosKod: 'B69',
        beskrivning: 'Cysticerkos'
    },
    {
        diagnosKod: 'B70',
        beskrivning: 'Infektion med binnikemaskar'
    },
    {
        diagnosKod: 'B71',
        beskrivning: 'Andra bandmaskinfektioner'
    },
    {
        diagnosKod: 'B72',
        beskrivning: 'Dracontiasis'
    },
    {
        diagnosKod: 'B73',
        beskrivning: 'Onchocerciasis'
    },
    {
        diagnosKod: 'B74',
        beskrivning: 'Filariainfektion'
    },
    {
        diagnosKod: 'B75',
        beskrivning: 'Trikinos (sjukdom orsakad av trikiner)'
    },
    {
        diagnosKod: 'B76',
        beskrivning: 'Hakmasksjukdom'
    },
    {
        diagnosKod: 'B77',
        beskrivning: 'Spolmaskinfektion'
    },
    {
        diagnosKod: 'B78',
        beskrivning: 'Strongyloidesinfektion'
    },
    {
        diagnosKod: 'B79',
        beskrivning: 'Piskmaskinfektion'
    },
    {
        diagnosKod: 'B80',
        beskrivning: 'Springmaskinfektion'
    },
    {
        diagnosKod: 'B81',
        beskrivning: 'Andra tarmmaskinfektioner som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'B82',
        beskrivning: 'Infektion med icke specificerade tarmparasiter'
    },
    {
        diagnosKod: 'B83',
        beskrivning: 'Andra masksjukdomar'
    },
    {
        diagnosKod: 'B85',
        beskrivning: 'Lusangrepp'
    },
    {
        diagnosKod: 'B86',
        beskrivning: 'Skabb'
    },
    {
        diagnosKod: 'B87',
        beskrivning: 'Infestation av fluglarver'
    },
    {
        diagnosKod: 'B88',
        beskrivning: 'Andra infestationer'
    },
    {
        diagnosKod: 'B89',
        beskrivning: 'Icke specificerade parasitsjukdomar'
    },
    {
        diagnosKod: 'B90',
        beskrivning: 'Sena effekter av tuberkulos'
    },
    {
        diagnosKod: 'B91',
        beskrivning: 'Sena effekter av polio'
    },
    {
        diagnosKod: 'B92',
        beskrivning: 'Sena effekter av lepra'
    },
    {
        diagnosKod: 'B94',
        beskrivning: 'Sena effekter av andra och icke specificerade infektionssjukdomar och parasitsjukdomar'
    },
    {
        diagnosKod: 'B95',
        beskrivning: 'Streptokocker och stafylokocker som orsak till sjukdomar som klassificeras i andra kapitel'
    },
    {
        diagnosKod: 'B96',
        beskrivning: 'Vissa andra specificerade bakterier som orsak till sjukdomar som klassificeras i andra kapitel'
    },
    {
        diagnosKod: 'B97',
        beskrivning: 'Virus som orsak till sjukdomar som klassificeras i andra kapitel'
    },
    {
        diagnosKod: 'B98',
        beskrivning: 'Andra specificerade infektiosa organismer som orsak till sjukdomar som klassificeras annorstades'
    },
    {
        diagnosKod: 'B99',
        beskrivning: 'Andra och icke specificerade infektionssjukdomar'
    },
    {
        diagnosKod: 'C00',
        beskrivning: 'Malign tumor i lapp'
    },
    {
        diagnosKod: 'C01',
        beskrivning: 'Malign tumor i tungbasen'
    },
    {
        diagnosKod: 'C02',
        beskrivning: 'Malign tumor i annan och icke specificerad del av tungan'
    },
    {
        diagnosKod: 'C03',
        beskrivning: 'Malign tumor i tandkottet'
    },
    {
        diagnosKod: 'C04',
        beskrivning: 'Malign tumor i munbotten'
    },
    {
        diagnosKod: 'C05',
        beskrivning: 'Malign tumor i gom'
    },
    {
        diagnosKod: 'C06',
        beskrivning: 'Malign tumor i annan och icke specificerad del av munhalan'
    },
    {
        diagnosKod: 'C07',
        beskrivning: 'Malign tumor i parotiskortel'
    },
    {
        diagnosKod: 'C08',
        beskrivning: 'Malign tumor i andra och icke specificerade stora spottkortlar'
    },
    {
        diagnosKod: 'C09',
        beskrivning: 'Malign tumor i tonsill'
    },
    {
        diagnosKod: 'C10',
        beskrivning: 'Malign tumor i orofarynx (mellansvalget)'
    },
    {
        diagnosKod: 'C11',
        beskrivning: 'Malign tumor i rinofarynx (ovre svalgrummet)'
    },
    {
        diagnosKod: 'C12',
        beskrivning: 'Malign tumor i fossa piriformis'
    },
    {
        diagnosKod: 'C13',
        beskrivning: 'Malign tumor i hypofarynx (svalget i hojd med struphuvudet)'
    },
    {
        diagnosKod: 'C14',
        beskrivning: 'Malign tumor med annan och ofullstandigt angiven lokalisation i lapp, munhala och svalg'
    },
    {
        diagnosKod: 'C15',
        beskrivning: 'Malign tumor i matstrupen'
    },
    {
        diagnosKod: 'C16',
        beskrivning: 'Malign tumor i magsacken'
    },
    {
        diagnosKod: 'C17',
        beskrivning: 'Malign tumor i tunntarmen'
    },
    {
        diagnosKod: 'C18',
        beskrivning: 'Malign tumor i tjocktarmen'
    },
    {
        diagnosKod: 'C19',
        beskrivning: 'Malign tumor i rektosigmoidala granszonen'
    },
    {
        diagnosKod: 'C20',
        beskrivning: 'Malign tumor i andtarmen'
    },
    {
        diagnosKod: 'C21',
        beskrivning: 'Malign tumor i anus och analkanalen'
    },
    {
        diagnosKod: 'C22',
        beskrivning: 'Malign tumor i levern och intrahepatiska gallgangarna'
    },
    {
        diagnosKod: 'C23',
        beskrivning: 'Malign tumor i gallblasan'
    },
    {
        diagnosKod: 'C24',
        beskrivning: 'Malign tumor i andra och icke specificerade delar av gallvagarna'
    },
    {
        diagnosKod: 'C25',
        beskrivning: 'Malign tumor i pankreas'
    },
    {
        diagnosKod: 'C26',
        beskrivning: 'Malign tumor med annan och ofullstandigt angiven lokalisation i matsmaltningsorganen'
    },
    {
        diagnosKod: 'C30',
        beskrivning: 'Malign tumor i nashala och mellanora'
    },
    {
        diagnosKod: 'C31',
        beskrivning: 'Malign tumor i nasans bihalor'
    },
    {
        diagnosKod: 'C32',
        beskrivning: 'Malign tumor i struphuvudet'
    },
    {
        diagnosKod: 'C33',
        beskrivning: 'Malign tumor i luftstrupen'
    },
    {
        diagnosKod: 'C34',
        beskrivning: 'Malign tumor i bronk och lunga'
    },
    {
        diagnosKod: 'C37',
        beskrivning: 'Malign tumor i tymus'
    },
    {
        diagnosKod: 'C38',
        beskrivning: 'Malign tumor i hjartat, mediastinum (lungmellanrummet) och lungsacken'
    },
    {
        diagnosKod: 'C39',
        beskrivning: 'Maligna tumorer med annan och ofullstandigt angiven lokalisation i andningsorganen och brosthalans organ'
    },
    {
        diagnosKod: 'C40',
        beskrivning: 'Malign tumor i ben och extremitetsledbrosk'
    },
    {
        diagnosKod: 'C41',
        beskrivning: 'Malign tumor i ben och ledbrosk med annan och icke specificerad lokalisation'
    },
    {
        diagnosKod: 'C43',
        beskrivning: 'Malignt melanom i huden'
    },
    {
        diagnosKod: 'C44',
        beskrivning: 'Andra maligna tumorer i huden'
    },
    {
        diagnosKod: 'C45',
        beskrivning: 'Mesoteliom'
    },
    {
        diagnosKod: 'C46',
        beskrivning: 'Kaposis sarkom'
    },
    {
        diagnosKod: 'C47',
        beskrivning: 'Malign tumor i perifera nerver och autonoma nervsystemet'
    },
    {
        diagnosKod: 'C48',
        beskrivning: 'Malign tumor i bukhinnan och retroperitonealrummet (utrymmet bakom bukhinnan)'
    },
    {
        diagnosKod: 'C49',
        beskrivning: 'Malign tumor i annan bindvav och mjukvavnad'
    },
    {
        diagnosKod: 'C50',
        beskrivning: 'Malign tumor i brostkortel'
    },
    {
        diagnosKod: 'C51',
        beskrivning: 'Malign tumor i vulva'
    },
    {
        diagnosKod: 'C52',
        beskrivning: 'Malign tumor i vagina'
    },
    {
        diagnosKod: 'C53',
        beskrivning: 'Malign tumor i livmoderhalsen'
    },
    {
        diagnosKod: 'C54',
        beskrivning: 'Malign tumor i livmoderkroppen'
    },
    {
        diagnosKod: 'C55',
        beskrivning: 'Malign tumor i livmodern med icke specificerad lokalisation'
    },
    {
        diagnosKod: 'C56',
        beskrivning: 'Malign tumor i aggstock'
    },
    {
        diagnosKod: 'C57',
        beskrivning: 'Malign tumor i andra och icke specificerade kvinnliga konsorgan'
    },
    {
        diagnosKod: 'C58',
        beskrivning: 'Malign tumor i moderkakan'
    },
    {
        diagnosKod: 'C60',
        beskrivning: 'Malign tumor i penis'
    },
    {
        diagnosKod: 'C61',
        beskrivning: 'Malign tumor i prostata'
    },
    {
        diagnosKod: 'C62',
        beskrivning: 'Malign tumor i testikel'
    },
    {
        diagnosKod: 'C63',
        beskrivning: 'Malign tumor i andra och icke specificerade manliga konsorgan'
    },
    {
        diagnosKod: 'C64',
        beskrivning: 'Malign tumor i njure med undantag for njurbacken'
    },
    {
        diagnosKod: 'C65',
        beskrivning: 'Malign tumor i njurbacken'
    },
    {
        diagnosKod: 'C66',
        beskrivning: 'Malign tumor i uretar (urinledare)'
    },
    {
        diagnosKod: 'C67',
        beskrivning: 'Malign tumor i urinblasan'
    },
    {
        diagnosKod: 'C68',
        beskrivning: 'Malign tumor i andra och icke specificerade urinorgan'
    },
    {
        diagnosKod: 'C69',
        beskrivning: 'Malign tumor i oga och narliggande vavnader'
    },
    {
        diagnosKod: 'C70',
        beskrivning: 'Malign tumor i centrala nervsystemets hinnor'
    },
    {
        diagnosKod: 'C71',
        beskrivning: 'Malign tumor i hjarnan'
    },
    {
        diagnosKod: 'C72',
        beskrivning: 'Malign tumor i ryggmargen, kranialnerver och andra delar av centrala nervsystemet'
    },
    {
        diagnosKod: 'C73',
        beskrivning: 'Malign tumor i tyreoidea'
    },
    {
        diagnosKod: 'C74',
        beskrivning: 'Malign tumor i binjure'
    },
    {
        diagnosKod: 'C75',
        beskrivning: 'Malign tumor i andra endokrina kortlar och darmed beslaktade vavnader'
    },
    {
        diagnosKod: 'C76',
        beskrivning: 'Malign tumor med annan och ofullstandigt angiven lokalisation'
    },
    {
        diagnosKod: 'C77',
        beskrivning: 'Sekundar malign tumor (metastas) och icke specificerad malign tumor i lymfkortlar'
    },
    {
        diagnosKod: 'C78',
        beskrivning: 'Sekundar malign tumor (metastas) i andningsorganen och matsmaltningsorganen'
    },
    {
        diagnosKod: 'C79',
        beskrivning: 'Sekundar malign tumor (metastas) med andra och icke specificerade lokalisationer'
    },
    {
        diagnosKod: 'C80',
        beskrivning: 'Malign tumor utan specificerad lokalisation'
    },
    {
        diagnosKod: 'C81',
        beskrivning: 'Hodgkins lymfom'
    },
    {
        diagnosKod: 'C82',
        beskrivning: 'Follikulart lymfom'
    },
    {
        diagnosKod: 'C83',
        beskrivning: 'Icke-follikulart lymfom'
    },
    {
        diagnosKod: 'C84',
        beskrivning: 'Mogna T/NK-cellslymfom'
    },
    {
        diagnosKod: 'C85',
        beskrivning: 'Andra och icke specificerade typer av non-Hodgkin-lymfom'
    },
    {
        diagnosKod: 'C86',
        beskrivning: 'Andra specificerade typer av T/NK-cellslymfom'
    },
    {
        diagnosKod: 'C88',
        beskrivning: 'Maligna immunoproliferativa sjukdomar'
    },
    {
        diagnosKod: 'C90',
        beskrivning: 'Myelom och maligna plasmacellstumorer'
    },
    {
        diagnosKod: 'C91',
        beskrivning: 'Lymfatisk leukemi'
    },
    {
        diagnosKod: 'C92',
        beskrivning: 'Myeloisk leukemi'
    },
    {
        diagnosKod: 'C93',
        beskrivning: 'Monocytleukemi'
    },
    {
        diagnosKod: 'C94',
        beskrivning: 'Andra leukemier med specificerad celltyp'
    },
    {
        diagnosKod: 'C95',
        beskrivning: 'Leukemi med icke specificerad celltyp'
    },
    {
        diagnosKod: 'C96',
        beskrivning: 'ovriga och icke specificerade maligna tumorer i lymfoid, blodbildande och beslaktad vavnad'
    },
    {
        diagnosKod: 'C97',
        beskrivning: 'Flera (primara) maligna tumorer med olika utgangspunkter'
    },
    {
        diagnosKod: 'D00',
        beskrivning: 'Cancer in situ i munhala, esofagus och magsack'
    },
    {
        diagnosKod: 'D01',
        beskrivning: 'Cancer in situ i andra och icke specificerade delar av matsmaltningsorganen'
    },
    {
        diagnosKod: 'D02',
        beskrivning: 'Cancer in situ i mellanora och andningsorgan'
    },
    {
        diagnosKod: 'D03',
        beskrivning: 'Melanom in situ'
    },
    {
        diagnosKod: 'D04',
        beskrivning: 'Cancer in situ i huden'
    },
    {
        diagnosKod: 'D05',
        beskrivning: 'Cancer in situ i brostkortel'
    },
    {
        diagnosKod: 'D06',
        beskrivning: 'Cancer in situ i livmoderhalsen'
    },
    {
        diagnosKod: 'D07',
        beskrivning: 'Cancer in situ i andra och icke specificerade konsorgan'
    },
    {
        diagnosKod: 'D09',
        beskrivning: 'Cancer in situ med annan och icke specificerad lokalisation'
    },
    {
        diagnosKod: 'D10',
        beskrivning: 'Benign tumor i munhala och svalg'
    },
    {
        diagnosKod: 'D11',
        beskrivning: 'Benign tumor i de stora spottkortlarna'
    },
    {
        diagnosKod: 'D12',
        beskrivning: 'Benign tumor i tjocktarm, andtarm, anus och analkanal'
    },
    {
        diagnosKod: 'D13',
        beskrivning: 'Benign tumor i andra och ofullstandigt angivna delar av matsmaltningsorganen'
    },
    {
        diagnosKod: 'D14',
        beskrivning: 'Benign tumor i mellanora och andningsorgan'
    },
    {
        diagnosKod: 'D15',
        beskrivning: 'Benign tumor i andra och icke specificerade organ i brosthalan'
    },
    {
        diagnosKod: 'D16',
        beskrivning: 'Benign tumor i ben och ledbrosk'
    },
    {
        diagnosKod: 'D17',
        beskrivning: 'Lipom (fettsvulst)'
    },
    {
        diagnosKod: 'D18',
        beskrivning: 'Hemangiom (blodkarlssvulst) och lymfangiom (lymfkarlssvulst), alla lokalisationer'
    },
    {
        diagnosKod: 'D19',
        beskrivning: 'Benign tumor i mesotelial (kroppshaletackande) vavnad'
    },
    {
        diagnosKod: 'D20',
        beskrivning: 'Benign tumor i mjukvavnad i retroperitonealrummet (utrymmet bakom bukhinnan) och i peritoneum (bukhinnan)'
    },
    {
        diagnosKod: 'D21',
        beskrivning: 'Andra benigna tumorer i bindvav och annan mjukvavnad'
    },
    {
        diagnosKod: 'D22',
        beskrivning: 'Melanocytnevus'
    },
    {
        diagnosKod: 'D23',
        beskrivning: 'Andra benigna tumorer i huden'
    },
    {
        diagnosKod: 'D24',
        beskrivning: 'Benign tumor i brostkortel'
    },
    {
        diagnosKod: 'D25',
        beskrivning: 'Uterusmyom (muskelsvulst i livmodern)'
    },
    {
        diagnosKod: 'D26',
        beskrivning: 'Andra benigna tumorer i livmodern'
    },
    {
        diagnosKod: 'D27',
        beskrivning: 'Benign tumor i ovarium'
    },
    {
        diagnosKod: 'D28',
        beskrivning: 'Benign tumor i andra och icke specificerade kvinnliga konsorgan'
    },
    {
        diagnosKod: 'D29',
        beskrivning: 'Benign tumor i de manliga konsorganen'
    },
    {
        diagnosKod: 'D30',
        beskrivning: 'Benign tumor i urinorganen'
    },
    {
        diagnosKod: 'D31',
        beskrivning: 'Benign tumor i oga och narliggande vavnader'
    },
    {
        diagnosKod: 'D32',
        beskrivning: 'Benign tumor i centrala nervsystemets hinnor'
    },
    {
        diagnosKod: 'D33',
        beskrivning: 'Benign tumor i hjarnan och andra delar av centrala nervsystemet'
    },
    {
        diagnosKod: 'D34',
        beskrivning: 'Benign tumor i tyreoidea (skoldkorteln)'
    },
    {
        diagnosKod: 'D35',
        beskrivning: 'Benign tumor i andra och icke specificerade endokrina kortlar'
    },
    {
        diagnosKod: 'D36',
        beskrivning: 'Benign tumor med annan och icke specificerad lokalisation'
    },
    {
        diagnosKod: 'D37',
        beskrivning: 'Tumor av osaker eller okand natur i munhalan och matsmaltningsorganen'
    },
    {
        diagnosKod: 'D38',
        beskrivning: 'Tumor av osaker eller okand natur i mellanora, andningsorganen och brostkorgens organ'
    },
    {
        diagnosKod: 'D39',
        beskrivning: 'Tumor av osaker eller okand natur i de kvinnliga konsorganen'
    },
    {
        diagnosKod: 'D40',
        beskrivning: 'Tumor av osaker eller okand natur i de manliga konsorganen'
    },
    {
        diagnosKod: 'D41',
        beskrivning: 'Tumor av osaker eller okand natur i urinorganen'
    },
    {
        diagnosKod: 'D42',
        beskrivning: 'Tumor av osaker eller okand natur i centrala nervsystemets hinnor'
    },
    {
        diagnosKod: 'D43',
        beskrivning: 'Tumor av osaker eller okand natur i hjarnan och andra delar av centrala nervsystemet'
    },
    {
        diagnosKod: 'D44',
        beskrivning: 'Tumor av osaker eller okand natur i de endokrina kortlarna'
    },
    {
        diagnosKod: 'D45',
        beskrivning: 'Polycythaemia vera (sjuklig okning av antalet roda blodkroppar)'
    },
    {
        diagnosKod: 'D46',
        beskrivning: 'Myelodysplastiska syndrom'
    },
    {
        diagnosKod: 'D47',
        beskrivning: 'Andra tumorer av osaker eller okand natur i lymfatisk, blodbildande och beslaktad vavnad'
    },
    {
        diagnosKod: 'D48',
        beskrivning: 'Tumor av osaker eller okand natur med annan och icke specificerad lokalisation'
    },
    {
        diagnosKod: 'D50',
        beskrivning: 'Jarnbristanemi'
    },
    {
        diagnosKod: 'D51',
        beskrivning: 'Anemi pa grund av vitamin B12-brist'
    },
    {
        diagnosKod: 'D52',
        beskrivning: 'Folatbristanemi'
    },
    {
        diagnosKod: 'D53',
        beskrivning: 'Andra nutritionsanemier'
    },
    {
        diagnosKod: 'D55',
        beskrivning: 'Anemi orsakad av enzymrubbningar'
    },
    {
        diagnosKod: 'D56',
        beskrivning: 'Talassemi (medelhavsanemi)'
    },
    {
        diagnosKod: 'D57',
        beskrivning: 'Sicklecellssjukdomar'
    },
    {
        diagnosKod: 'D58',
        beskrivning: 'Andra arftliga hemolytiska anemier (arftlig blodbrist pa grund av okad nedbrytning av roda blodkroppar)'
    },
    {
        diagnosKod: 'D59',
        beskrivning: 'Forvarvad hemolytisk anemi (forvarvad blodbrist pa grund av okad nedbrytning av roda blodkroppar)'
    },
    {
        diagnosKod: 'D60',
        beskrivning: 'Forvarvad isolerad aplasi av roda blodkroppar [Aquired pure red cell aplasia]'
    },
    {
        diagnosKod: 'D61',
        beskrivning: 'Andra aplastiska anemier (annan blodbrist pa grund av upphord eller minskad blodbildning i benmargen)'
    },
    {
        diagnosKod: 'D62',
        beskrivning: 'Anemi efter akut storre blodning'
    },
    {
        diagnosKod: 'D63',
        beskrivning: 'Anemi vid kroniska sjukdomar som klassificeras annorstades'
    },
    {
        diagnosKod: 'D64',
        beskrivning: 'Andra anemier'
    },
    {
        diagnosKod: 'D65',
        beskrivning: 'Disseminerad intravasal koagulation [defibrineringssyndrom]'
    },
    {
        diagnosKod: 'D66',
        beskrivning: 'arftlig brist pa faktor VIII'
    },
    {
        diagnosKod: 'D67',
        beskrivning: 'arftlig brist pa faktor IX'
    },
    {
        diagnosKod: 'D68',
        beskrivning: 'Andra koagulationsrubbningar'
    },
    {
        diagnosKod: 'D69',
        beskrivning: 'Purpura (punktformiga blodningar i huden mm) och andra blodningstillstand'
    },
    {
        diagnosKod: 'D70',
        beskrivning: 'Agranulocytos'
    },
    {
        diagnosKod: 'D71',
        beskrivning: 'Funktionella rubbningar hos polymorfkarniga neutrofila celler (vissa vita blodkroppar)'
    },
    {
        diagnosKod: 'D72',
        beskrivning: 'Andra sjukdomar i vita blodkroppar'
    },
    {
        diagnosKod: 'D73',
        beskrivning: 'Sjukdomar i mjalten'
    },
    {
        diagnosKod: 'D74',
        beskrivning: 'Methemoglobinemi'
    },
    {
        diagnosKod: 'D75',
        beskrivning: 'Andra sjukdomar i blod och blodbildande organ'
    },
    {
        diagnosKod: 'D76',
        beskrivning: 'Andra specificerade sjukdomar som engagerar lymforetikular och retikulohistiocytar vavnad'
    },
    {
        diagnosKod: 'D77',
        beskrivning: 'Andra forandringar i blod och blodbildande organ vid sjukdomar som klassificeras annorstades'
    },
    {
        diagnosKod: 'D80',
        beskrivning: 'Immunbrist med huvudsakligen antikroppsdefekter'
    },
    {
        diagnosKod: 'D81',
        beskrivning: 'Kombinerade immunbristtillstand'
    },
    {
        diagnosKod: 'D82',
        beskrivning: 'Immunbrist i kombination med andra omfattande defekter'
    },
    {
        diagnosKod: 'D83',
        beskrivning: 'Vanlig variabel immunbrist'
    },
    {
        diagnosKod: 'D84',
        beskrivning: 'Andra immunbristtillstand'
    },
    {
        diagnosKod: 'D86',
        beskrivning: 'Sarkoidos'
    },
    {
        diagnosKod: 'D89',
        beskrivning: 'Andra rubbningar i immunsystemet som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'E00',
        beskrivning: 'Medfott jodbristsyndrom'
    },
    {
        diagnosKod: 'E01',
        beskrivning: 'Jodbristrelaterade skoldkortelsjukdomar och darmed sammanhangande tillstand'
    },
    {
        diagnosKod: 'E02',
        beskrivning: 'Subklinisk jodbristhypotyreos (underfunktion av skoldkorteln)'
    },
    {
        diagnosKod: 'E03',
        beskrivning: 'Annan hypotyreos (underfunktion av skoldkorteln)'
    },
    {
        diagnosKod: 'E04',
        beskrivning: 'Annan atoxisk struma (struma utan overfunktion)'
    },
    {
        diagnosKod: 'E05',
        beskrivning: 'Tyreotoxikos [hypertyreos] (overfunktion av skoldkorteln)'
    },
    {
        diagnosKod: 'E06',
        beskrivning: 'Skoldkortelinflammation'
    },
    {
        diagnosKod: 'E07',
        beskrivning: 'Andra sjukdomar i skoldkorteln'
    },
    {
        diagnosKod: 'E10',
        beskrivning: 'Diabetes mellitus typ 1'
    },
    {
        diagnosKod: 'E11',
        beskrivning: 'Diabetes mellitus typ 2'
    },
    {
        diagnosKod: 'E12',
        beskrivning: 'Naringsbristrelaterad diabetes'
    },
    {
        diagnosKod: 'E13',
        beskrivning: 'Annan specificerad diabetes'
    },
    {
        diagnosKod: 'E14',
        beskrivning: 'Icke specificerad diabetes'
    },
    {
        diagnosKod: 'E15',
        beskrivning: 'Icke diabetiskt hypoglykemiskt koma (djup medvetsloshet pa grund av glukosbrist)'
    },
    {
        diagnosKod: 'E16',
        beskrivning: 'Andra rubbningar i bukspottkortelns inre sekretion'
    },
    {
        diagnosKod: 'E20',
        beskrivning: 'Hypoparatyreoidism (underfunktion av biskoldkortel)'
    },
    {
        diagnosKod: 'E21',
        beskrivning: 'Hyperparatyreoidism (overfunktion av biskoldkortel) och andra sjukdomar i biskoldkortlarna'
    },
    {
        diagnosKod: 'E22',
        beskrivning: 'Hyperfunktion av hypofysen'
    },
    {
        diagnosKod: 'E23',
        beskrivning: 'Hypofunktion och andra sjukdomar i hypofysen'
    },
    {
        diagnosKod: 'E24',
        beskrivning: 'Cushings syndrom (overproduktion av binjurebarkhormoner)'
    },
    {
        diagnosKod: 'E25',
        beskrivning: 'Adrenogenitala rubbningar (rubbningar i binjurens produktion av konshormon)'
    },
    {
        diagnosKod: 'E26',
        beskrivning: 'Hyperaldosteronism (overproduktion av aldosteron)'
    },
    {
        diagnosKod: 'E27',
        beskrivning: 'Andra sjukdomar i binjurarna'
    },
    {
        diagnosKod: 'E28',
        beskrivning: 'Rubbningar i aggstockarnas funktion'
    },
    {
        diagnosKod: 'E29',
        beskrivning: 'Rubbningar i testiklarnas funktion'
    },
    {
        diagnosKod: 'E30',
        beskrivning: 'Pubertetsstorningar som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'E31',
        beskrivning: 'Samtidig rubbning i flera inresekretoriska organ'
    },
    {
        diagnosKod: 'E32',
        beskrivning: 'Sjukdomar i tymus'
    },
    {
        diagnosKod: 'E34',
        beskrivning: 'Andra endokrina rubbningar'
    },
    {
        diagnosKod: 'E35',
        beskrivning: 'Endokrina rubbningar vid sjukdomar som klassificeras annorstades'
    },
    {
        diagnosKod: 'E40',
        beskrivning: 'Svar proteinundernaring [Kwashiorkor]'
    },
    {
        diagnosKod: 'E41',
        beskrivning: 'Svar energiundernaring'
    },
    {
        diagnosKod: 'E42',
        beskrivning: 'Svar protein-energiundernaring'
    },
    {
        diagnosKod: 'E43',
        beskrivning: 'Icke specificerad svar undernaring'
    },
    {
        diagnosKod: 'E44',
        beskrivning: 'Protein-energiundernaring av mattlig och latt grad'
    },
    {
        diagnosKod: 'E45',
        beskrivning: 'Forsenad utveckling efter protein-energiundernaring'
    },
    {
        diagnosKod: 'E46',
        beskrivning: 'Icke specificerad protein-energiundernaring'
    },
    {
        diagnosKod: 'E50',
        beskrivning: 'A-vitaminbrist'
    },
    {
        diagnosKod: 'E51',
        beskrivning: 'Tiaminbrist'
    },
    {
        diagnosKod: 'E52',
        beskrivning: 'Niacinbrist [pellagra]'
    },
    {
        diagnosKod: 'E53',
        beskrivning: 'Brist pa andra vitaminer i B-gruppen'
    },
    {
        diagnosKod: 'E54',
        beskrivning: 'Askorbinsyrabrist'
    },
    {
        diagnosKod: 'E55',
        beskrivning: 'D-vitaminbrist'
    },
    {
        diagnosKod: 'E56',
        beskrivning: 'Andra vitaminbristtillstand'
    },
    {
        diagnosKod: 'E58',
        beskrivning: 'Dietbetingad kalciumbrist'
    },
    {
        diagnosKod: 'E59',
        beskrivning: 'Dietbetingad selenbrist'
    },
    {
        diagnosKod: 'E60',
        beskrivning: 'Dietbetingad zinkbrist'
    },
    {
        diagnosKod: 'E61',
        beskrivning: 'Brist pa andra grundamnen i fodan'
    },
    {
        diagnosKod: 'E63',
        beskrivning: 'Andra naringsbristtillstand'
    },
    {
        diagnosKod: 'E64',
        beskrivning: 'Sena effekter av undernaring och andra naringsbristtillstand'
    },
    {
        diagnosKod: 'E65',
        beskrivning: 'Lokaliserad fetma'
    },
    {
        diagnosKod: 'E66',
        beskrivning: 'Fetma'
    },
    {
        diagnosKod: 'E67',
        beskrivning: 'Annan overnaring'
    },
    {
        diagnosKod: 'E68',
        beskrivning: 'Sena effekter av overnaring'
    },
    {
        diagnosKod: 'E70',
        beskrivning: 'Rubbningar i omsattningen av aromatiska aminosyror'
    },
    {
        diagnosKod: 'E71',
        beskrivning: 'Rubbningar i omsattningen av grenade aminosyror och av fettsyror'
    },
    {
        diagnosKod: 'E72',
        beskrivning: 'Andra rubbningar i omsattningen av aminosyror'
    },
    {
        diagnosKod: 'E73',
        beskrivning: 'Laktosintolerans'
    },
    {
        diagnosKod: 'E74',
        beskrivning: 'Andra rubbningar i kolhydratomsattningen'
    },
    {
        diagnosKod: 'E75',
        beskrivning: 'Rubbningar i sfingolipidomsattningen och andra rubbningar i fettupplagringen'
    },
    {
        diagnosKod: 'E76',
        beskrivning: 'Rubbningar i omsattningen av glukosaminoglykaner'
    },
    {
        diagnosKod: 'E77',
        beskrivning: 'Rubbningar i glykoproteinomsattningen'
    },
    {
        diagnosKod: 'E78',
        beskrivning: 'Rubbning i omsattningen av lipoprotein och andra lipidemier'
    },
    {
        diagnosKod: 'E79',
        beskrivning: 'Rubbningar i purin- och pyrimidinomsattningen'
    },
    {
        diagnosKod: 'E80',
        beskrivning: 'Rubbningar i omsattningen av porfyrin och bilirubin'
    },
    {
        diagnosKod: 'E83',
        beskrivning: 'Rubbningar i mineralomsattningen'
    },
    {
        diagnosKod: 'E84',
        beskrivning: 'Cystisk fibros'
    },
    {
        diagnosKod: 'E85',
        beskrivning: 'Amyloidos'
    },
    {
        diagnosKod: 'E86',
        beskrivning: 'Minskad vatskevolym'
    },
    {
        diagnosKod: 'E87',
        beskrivning: 'Andra rubbningar i vatske-, elektrolyt- och syrabasbalans'
    },
    {
        diagnosKod: 'E88',
        beskrivning: 'Andra amnesomsattningssjukdomar'
    },
    {
        diagnosKod: 'E89',
        beskrivning: 'Endokrina rubbningar och amnesomsattningssjukdomar efter kirurgiska och medicinska ingrepp som ej klassificeras annorstades'
    },
    {
        diagnosKod: 'E90',
        beskrivning: 'Rubbningar i nutrition och amnesomsattning vid sjukdomar som klassificeras annorstades'
    },
    {
        diagnosKod: 'F00',
        beskrivning: 'Demens vid Alzheimers sjukdom'
    },
    {
        diagnosKod: 'F01',
        beskrivning: 'Vaskular demens'
    },
    {
        diagnosKod: 'F02',
        beskrivning: 'Demens vid andra sjukdomar som klassificeras annorstades'
    },
    {
        diagnosKod: 'F03',
        beskrivning: 'Ospecificerad demens'
    },
    {
        diagnosKod: 'F04',
        beskrivning: 'Organiska amnesisyndrom ej framkallade av alkohol eller andra psykoaktiva substanser'
    },
    {
        diagnosKod: 'F05',
        beskrivning: 'Delirium ej framkallat av alkohol eller andra psykoaktiva substanser'
    },
    {
        diagnosKod: 'F06',
        beskrivning: 'Andra psykiska storningar orsakade av hjarnskada, cerebral dysfunktion eller kroppslig sjukdom'
    },
    {
        diagnosKod: 'F07',
        beskrivning: 'Personlighets- och beteendestorningar orsakade av hjarnsjukdom, hjarnskada eller cerebral dysfunktion'
    },
    {
        diagnosKod: 'F09',
        beskrivning: 'Ospecificerad organisk eller symtomatisk psykisk storning'
    },
    {
        diagnosKod: 'F10',
        beskrivning: 'Psykiska storningar och beteendestorningar orsakade av alkohol'
    },
    {
        diagnosKod: 'F11',
        beskrivning: 'Psykiska storningar och beteendestorningar orsakade av opioider'
    },
    {
        diagnosKod: 'F12',
        beskrivning: 'Psykiska storningar och beteendestorningar orsakade av cannabis'
    },
    {
        diagnosKod: 'F13',
        beskrivning: 'Psykiska storningar och beteendestorningar orsakade av sedativa och hypnotika'
    },
    {
        diagnosKod: 'F14',
        beskrivning: 'Psykiska storningar och beteendestorningar orsakade av kokain'
    },
    {
        diagnosKod: 'F15',
        beskrivning: 'Psykiska storningar och beteendestorningar orsakade av andra stimulantia, daribland koffein'
    },
    {
        diagnosKod: 'F16',
        beskrivning: 'Psykiska storningar och beteendestorningar orsakade av hallucinogener'
    },
    {
        diagnosKod: 'F17',
        beskrivning: 'Psykiska storningar och beteendestorningar orsakade av tobak'
    },
    {
        diagnosKod: 'F18',
        beskrivning: 'Psykiska storningar och beteendestorningar orsakade av flyktiga losningsmedel'
    },
    {
        diagnosKod: 'F19',
        beskrivning: 'Psykiska storningar och beteendestorningar orsakade av flera droger i kombination och av andra psykoaktiva substanser'
    },
    {
        diagnosKod: 'F20',
        beskrivning: 'Schizofreni'
    },
    {
        diagnosKod: 'F21',
        beskrivning: 'Schizotyp storning'
    },
    {
        diagnosKod: 'F22',
        beskrivning: 'Kroniska vanforestallningssyndrom'
    },
    {
        diagnosKod: 'F23',
        beskrivning: 'Akuta och overgaende psykotiska syndrom'
    },
    {
        diagnosKod: 'F24',
        beskrivning: 'Inducerat vanforestallningssyndrom'
    },
    {
        diagnosKod: 'F25',
        beskrivning: 'Schizoaffektiva syndrom'
    },
    {
        diagnosKod: 'F28',
        beskrivning: 'Andra icke organiska psykotiska storningar'
    },
    {
        diagnosKod: 'F29',
        beskrivning: 'Ospecificerad icke organisk psykos'
    },
    {
        diagnosKod: 'F30',
        beskrivning: 'Manisk episod'
    },
    {
        diagnosKod: 'F31',
        beskrivning: 'Bipolar sjukdom'
    },
    {
        diagnosKod: 'F32',
        beskrivning: 'Depressiv episod'
    },
    {
        diagnosKod: 'F33',
        beskrivning: 'Recidiverande depressioner'
    },
    {
        diagnosKod: 'F34',
        beskrivning: 'Kroniska forstamningssyndrom'
    },
    {
        diagnosKod: 'F38',
        beskrivning: 'Andra forstamningssyndrom'
    },
    {
        diagnosKod: 'F39',
        beskrivning: 'Ospecificerat forstamningssyndrom'
    },
    {
        diagnosKod: 'F40',
        beskrivning: 'Fobiska syndrom'
    },
    {
        diagnosKod: 'F41',
        beskrivning: 'Andra angestsyndrom'
    },
    {
        diagnosKod: 'F42',
        beskrivning: 'Tvangssyndrom'
    },
    {
        diagnosKod: 'F43',
        beskrivning: 'Anpassningsstorningar och reaktion pa svar stress'
    },
    {
        diagnosKod: 'F44',
        beskrivning: 'Dissociativa syndrom'
    },
    {
        diagnosKod: 'F45',
        beskrivning: 'Somatoforma syndrom'
    },
    {
        diagnosKod: 'F48',
        beskrivning: 'Andra neurotiska syndrom'
    },
    {
        diagnosKod: 'F50',
        beskrivning: 'atstorningar'
    },
    {
        diagnosKod: 'F51',
        beskrivning: 'Icke organiska somnstorningar'
    },
    {
        diagnosKod: 'F52',
        beskrivning: 'Sexuell dysfunktion, ej orsakad av organisk storning eller sjukdom'
    },
    {
        diagnosKod: 'F53',
        beskrivning: 'Psykiska storningar och beteendestorningar sammanhangande med barnsangstiden, vilka ej klassificeras annorstades'
    },
    {
        diagnosKod: 'F54',
        beskrivning: 'Psykologiska faktorer och beteendefaktorer med betydelse for storningar eller sjukdomar som klassificeras annorstades'
    },
    {
        diagnosKod: 'F55',
        beskrivning: 'Missbruk av substanser som ej ar beroendeframkallande'
    },
    {
        diagnosKod: 'F59',
        beskrivning: 'Ospecificerade beteendesyndrom forenade med fysiologiska storningar och fysiska faktorer'
    },
    {
        diagnosKod: 'F60',
        beskrivning: 'Specifika personlighetsstorningar'
    },
    {
        diagnosKod: 'F61',
        beskrivning: 'Personlighetsstorningar av blandtyp och andra personlighetsstorningar'
    },
    {
        diagnosKod: 'F62',
        beskrivning: 'Kroniska personlighetsforandringar ej orsakade av hjarnskada eller hjarnsjukdom'
    },
    {
        diagnosKod: 'F63',
        beskrivning: 'Impulskontrollstorningar'
    },
    {
        diagnosKod: 'F64',
        beskrivning: 'Konsidentitetsstorningar'
    },
    {
        diagnosKod: 'F65',
        beskrivning: 'Storningar av sexuell preferens'
    },
    {
        diagnosKod: 'F66',
        beskrivning: 'Psykiska storningar och beteendestorningar sammanhangande med sexuell utveckling och orientering'
    },
    {
        diagnosKod: 'F68',
        beskrivning: 'Andra storningar av personlighet och beteende hos vuxna'
    },
    {
        diagnosKod: 'F69',
        beskrivning: 'Ospecificerad storning av personlighet och beteende hos vuxna'
    },
    {
        diagnosKod: 'F70',
        beskrivning: 'Lindrig psykisk utvecklingsstorning'
    },
    {
        diagnosKod: 'F71',
        beskrivning: 'Medelsvar psykisk utvecklingsstorning'
    },
    {
        diagnosKod: 'F72',
        beskrivning: 'Svar psykisk utvecklingsstorning'
    },
    {
        diagnosKod: 'F73',
        beskrivning: 'Grav psykisk utvecklingsstorning'
    },
    {
        diagnosKod: 'F78',
        beskrivning: 'Annan psykisk utvecklingsstorning'
    },
    {
        diagnosKod: 'F79',
        beskrivning: 'Ospecificerad psykisk utvecklingsstorning'
    },
    {
        diagnosKod: 'F80',
        beskrivning: 'Specifika storningar av tal- och sprakutvecklingen'
    },
    {
        diagnosKod: 'F81',
        beskrivning: 'Specifika utvecklingsstorningar av inlarningsfardigheter'
    },
    {
        diagnosKod: 'F82',
        beskrivning: 'Specifik motorisk utvecklingsstorning'
    },
    {
        diagnosKod: 'F83',
        beskrivning: 'Blandade specifika utvecklingsstorningar'
    },
    {
        diagnosKod: 'F84',
        beskrivning: 'Genomgripande utvecklingsstorningar'
    },
    {
        diagnosKod: 'F88',
        beskrivning: 'Andra specificerade storningar av psykisk utveckling'
    },
    {
        diagnosKod: 'F89',
        beskrivning: 'Ospecificerad storning av psykisk utveckling'
    },
    {
        diagnosKod: 'F90',
        beskrivning: 'Hyperaktivitetsstorningar'
    },
    {
        diagnosKod: 'F91',
        beskrivning: 'Beteendestorningar av utagerande slag'
    },
    {
        diagnosKod: 'F92',
        beskrivning: 'Blandade storningar av beteende och kansloliv'
    },
    {
        diagnosKod: 'F93',
        beskrivning: 'Emotionella storningar med debut sarskilt under barndomen'
    },
    {
        diagnosKod: 'F94',
        beskrivning: 'Storningar av social funktion med debut sarskilt under barndom och ungdomstid'
    },
    {
        diagnosKod: 'F95',
        beskrivning: 'Tics'
    },
    {
        diagnosKod: 'F98',
        beskrivning: 'Andra beteendestorningar och emotionella storningar med debut vanligen under barndom och ungdomstid'
    },
    {
        diagnosKod: 'F99',
        beskrivning: 'Psykisk storning ej specificerad pa annat satt'
    }
];

module.exports = diagnosKategorier;
