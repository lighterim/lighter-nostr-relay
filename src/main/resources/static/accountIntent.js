function hashThenSend() {
    console.log('11111')
    const concat = [
        '0',
        Date.now(),
        $("#pubkey").val(),
        $("#kind").val(),
        $("#content").val(),
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
    return "["
        + "\"EVENT\","
        + JSON.stringify(
            {
                'id': id_hash,
                'kind': parseInt($("#kind").val(), 10),
                'content': $("#content").val(),
                'tags': [
                    ['account', $("#nftId").val(), $("#chainId").val(), $("#tba").val(), $("#pubkey").val()],
                ],
                'pubkey': $("#pubkey").val(),
                'created_at': Date.now(),
                'sig': '86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546'
            }
        )
        + "]";
}