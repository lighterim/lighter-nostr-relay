function hashThenSend() {
    const concat = [
        '0',
        $("#pubkey").val(),
        Date.now(),
        // $("#kind").val(),
        // $("#e_tag").val(),
        // $("#p_tag").val(),
        $("#content").val()
    ].join(",");

    const text = [
        '[',
        concat,
        ']'
    ].join('');

    createDigest(text).then((hash) => sendContent(hash));
}

function replaceHash(id_hash) {
    let clickNow = Date.now();
    return "["
        + "\"REQ\","
        + "\"" + id_hash + "\","
        + JSON.stringify(
            {
                'compositionQuery': {
                    'kind': 30079,
                    'anyMatchList': [
                        {'eventIdString': $('#eventId').val() ? [].concat($('#eventId').val().split(",")) : []}
                    ]
                }
            }
        )
        + "]";
}