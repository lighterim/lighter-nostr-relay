function hashThenSend() {
    const concat = [
        '0',
        $("#pubkey").val(),
        Date.now(),
        $("#kind").val(),
        $("#eventId").val(),
        $("#userId").val(),
        $("#volume").val()
        // $("#e_tag").val(),
        // $("#p_tag").val(),
        // $("#content").val()
    ].join(",");

    const text = [
        '[',
        concat,
        ']'
    ].join('');

    console.log(text)
    createDigest(text).then((hash) => sendContent(hash));
}

function replaceHash(id_hash) {
    let clickNow = Date.now();
    return "["
        + "\"EVENT\","
        + JSON.stringify(
            {
                'id': id_hash,
                'kind': $("#kind").val(),
                'content': "",
                'tags': [
                    ['e', $("#eventId").val()],
                    ['user_id', $("#userId").val()],
                    ['volume', $("#volume").val()],
                ],
                'pubkey': $("#pubkey").val(),
                'created_at': Date.now(),
                'sig': '86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546'
            }
        )
        + "]";
}