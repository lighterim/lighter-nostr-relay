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
                'kind': parseInt($("#kind").val(), 10),
                'content': "take intent",
                'tags': [
                    ['take','buy', $("#eventId").val(), $("#userId").val(), $("#pubkey").val(), $("#volume").val(), $("#userId").val(), $("#pubkey").val(), "0", "0", $("#userId").val()],
                    ['eip712', $("#walletAddress").val(), $("#contractAddress").val(), $("#domainAppName").val(), $("#domainVersion").val(), $("#sign").val()],
                    ["token", "USDT", "radixDLT", "stokenet", $("#token_addr").val(), $("#volume").val(), $("#chainId").val()],
                    ["quote", $("#number").val(), "CNY", "719", "11111", "55", "10000"],
                    ["limit", "11111", "222222"],
                    ["permit2", "sss", "sss", "ddd", "zz"],
                    ["payment", "alipay", "cash11", "cash11", "cash11"],
                ],
                'pubkey': $("#pubkey").val(),
                'created_at': Date.now(),
                'sig': '86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546'
            }
        )
        + "]";
}