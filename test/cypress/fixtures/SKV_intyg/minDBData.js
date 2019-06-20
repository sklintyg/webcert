skv = {
    "identitet": "Pass",
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
    "implantat": {
        "ja": false,
        "avlägsnats": true
    },
    "undersökning": {
        // Enbart en av dessa ska vara true
        "ja": true,
        "skaGöras": false,
        "kortFöreDöden": false
    },
    "polisanmälan": {
        "ja": false
    }
}
