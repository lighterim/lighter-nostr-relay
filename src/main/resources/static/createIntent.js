function hashThenSend() {
    console.log('11111')
    const concat = [
        '0',
        Date.now(),
        $("#pubkey").val(),
        $("#created_at").val(),
        $("#kind").val(),
        $("#subject").val(),
        $("#title").val(),
        $("#summary").val(),
        $("#content").val(),
        $("#location").val(),
        // $("#g_tag").val(),
        // $("#t_tag").val(),
        $("#number").val(),
        $("#currency").val(),
        $("#frequency").val(),
        // $("#e_tag").val(),
        // $("#p_tag").val(),
        $("#side").val(),
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
                    // ['subject', $("#subject").val()],
                    ['title', $("#title").val()],
                    ['make', $("#side").val(), $("#userId").val(), $("#pubkey").val()],
                    ['eip712', $("#walletAddress").val(), $("#contractAddress").val(), $("#domainAppName").val(), $("#domainVersion").val(), $("#sign").val()],
                    ['published_at', Date.now()],
                    // ['summary', $("#summary").val()],
                    ['limit', "1000000", "100000000"],
                    [
                        'permit2', $("#nonce").val(), $("#permit2_signature").val()
                    ],
                    ['token', $("#symbol").val(), $("#chain").val(), "stokenet", $("#token_addr").val(), $("#amount").val(), $("#chainId").val(), $("#expiryTime").val()],
                    ['quote', $("#number").val(), $("#currency").val(), "7.19", $("#sign").val()],
                    ['payment', 'alipay', 'cash11', 'cash11', 'cash11']
                    // ['p', 'cccd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984', null, 'seller'],
                    // ['p', 'cccd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984', null, 'buyer'],
                    // ['p', 'cccd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984', null, 'witness']
                    // ['e', $("#e_tag").val()],
                    // ['p', $("#p_tag").val()],
                    // ['t', $("#t_tag").val()],
                    // ['g', $("#g_tag").val()]
                ],
                'pubkey': $("#pubkey").val(),
                'created_at': Date.now(),
                'sig': '86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546'
            }
        )
        + "]";
}