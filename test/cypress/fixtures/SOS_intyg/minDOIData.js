skv = {
    "patientuppgifter": {
        "identitet": "Pass",
        "land": {
            "ja": false,
            "land":"Narnia"
        }
    },
    "dödsuppgifter": {
        "dödsdatum": {
            "säkert": true,
            //Datum sätts i funktionen "sektionDödsdatumDödsplats" i dbIntyg.js
        },
        "dödsplats": "Karlstad",
        "påträffades": {
            //Enbart en av dessa ska vara true
            "sjukhus": true,
            "ordinärt": false,
            "särskilt": false,
            "okänd": false
        }
    },
    "avlidit28dagar": {
        "ja": true
    },
    "dödsorsak": {
        "antal": 1, // Mellan 1-4
        "BeskrivningA": "Hjärtattack",
        "BeskrivningB": "Blodpropp",
        "BeskrivningC": "benbrott",
        "BeskrivningD": "gymnastik",
        "sjukdommar": {
            "antal": 0, // Mellan 0-8
            "sjukdomEtt": "Feber",
            "sjukdomTvå": "Magsjuka",
            "sjukdomTre": "Ont i knät",
            "sjukdomFyra": "Hosta",
            "sjukdomFem": "Magkatarr",
            "sjukdomSex": "Bengan sjuk",
            "sjukdomSju": "Trött",
            "sjukdomÅtta": "Depression"
        }
    },
    "operation": {
        // Enbart en av dessa ska vara true
        "ja": false,
        "nej": true,
        "uppgiftSaknas": false,
        "tillstånd": "Brutet ben"
    },
    "skada": {
        "ja": false,
        "orsak": {
            // Endast en av dessa ska bara true
            "olycksfall": false,
            "självmord": false,
            "avsiktligt": true,
            "oklart": false,
        },
        "beskrivning": "En kort beskrivning av hur skadan/förgiftningen uppkom"
    },
    "dödsorsaksuppgifter": {
        "föreDöden": false,
        "efterDöden": false,
        "kliniskObduktion": true,
        "obduktion": false,
        "likbesiktning": false
    }
}
