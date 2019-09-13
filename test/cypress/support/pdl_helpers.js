export function pdlEvent(env, actType, actArgs, actLevel, userIdArg, assignment, titleArg, vgId_mod, vgNamn_mod, veId_mod, 
    veNamn_mod, patientIdArg, careProviderIdArg, careProviderNameArg, careUnitIdArg, careUnitNameArg) {

    var loggHandelse = {
        activity: {
            activityType: actType,
            activityLevel: actLevel,
            activityArgs: actArgs,
        },
        user: {
            userId: userIdArg,
            assignment: assignment,
            title: titleArg,
            careProvider: {
                careProviderId: vgId_mod,
                careProviderName: vgNamn_mod
            },
            careUnit: {
                careUnitId: veId_mod,
                careUnitName: veNamn_mod
            }
        },
        resources: {
            resource: {
                patient: {
                    patientId: patientIdArg
                },
                careProvider: {
                    careProviderId: careProviderIdArg,
                    careProviderName: careProviderNameArg
                },
                careUnit: {
                    careUnitId: careUnitIdArg,
                    careUnitName: careUnitNameArg
                }
            }
        }
    }
    return loggHandelse;
}

export const enumHandelse = {
    LÄSA: "Läsa",
    SKRIVA: "Skriva",
    SIGNERA: "Signera",
    UTSKRIFT: "Utskrift",
    MAKULERA: "Radera",
    RADERA: "Radera"
};

export const enumHandelseArgument = {
    FKASSA: "Intyg skickat till mottagare FKASSA",
    TRANSP: "Intyg skickat till mottagare TRANSP",
    AF: "Intyg skickat till mottagare AF",
    SKV: "Intyg skickat till mottagare SKV",
    SOS: "Intyg skickat till mottagare SOS",
    LÄSASJF: "Läsning i enlighet med sammanhållen journalföring",
    UTSKRIFT: "Intyg utskrivet",
    UTSKRIFTUTKAST: "Utkastet utskrivet",
    SRS_PREDIKTION: "Prediktion från SRS av risk för lång sjukskrivning",
    SRS_LÄKARES_ÅSIKT: "Läkarens egen bedömning"
};
