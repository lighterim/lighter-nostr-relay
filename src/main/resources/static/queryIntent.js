function hashThenSend() {
    const concat = [
        '0',
        $("#pubkey").val(),
        dateNow,
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
                    'kind': 30077,
                    'allMatchList':[
                        {'side': $('#side').val() ? [].concat($('#side').val().split(",")) : []},
                        {'currency': $('#currency').val() ? [].concat($('#currency').val().split(",")) : []},
                        {'symbol': $('#symbol').val() ? [].concat($('#symbol').val().split(",")) : []},
                        {'paymentMethod': $('#paymentMethod').val() ? [].concat($('#paymentMethod').val().split(",")) : []}
                    ]
                }
            }
        )
        + "]";
}