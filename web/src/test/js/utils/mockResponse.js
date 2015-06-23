angular.module('webcertTest').value('mockResponse', {
    utkastList: {
        'totalCount':1,
        'results':[
            {'intygId':'2885f159-51e7-41bc-aba0-88fb02b2d667','patientId':'19121212-1212','source':'WC','intygType':'fk7263','status':'DRAFT_COMPLETE','lastUpdatedSigned':'2015-01-28T13:47:22.071','updatedSignedBy':'Jan Nilsson','vidarebefordrad':false}
        ]
    },
    utkast: {
        'intygId': '6c10506a-1ef1-4ce2-a4c8-a82fa43a1e26',
        'intygType': 'fk7263',
        'patientNamn': 'Tolvan Tolvansson',
        'signeringsDatum': '2015-01-27T16:01:10.000',
        'patientId': {
            'patientIdRoot': '1.2.752.129.2.1.3.1', 'patientIdExtension': '19121212-1212'}
    },
    utkastDefaultFilterFormData: {
        notified: 'default',
        complete: 'default',
        lastFilterQuery: {
            enhetsId: 'enhet1',
                startFrom: 0,
                pageSize: 10,
                filter: {
                notified: undefined, // 3-state, undefined, true, false
                    complete: undefined, // 3-state, undefined, true, false
                    savedFrom: undefined,
                    savedTo: undefined,
                    savedBy: undefined
            }
        }
    }
});