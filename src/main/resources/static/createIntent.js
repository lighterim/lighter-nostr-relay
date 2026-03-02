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
                    ['make', $("#side").val(), '1', $("#pubkey").val(), $("#intentType").val()],
                    ['eip712', $("#walletAddress").val(), $("#contractAddress").val(), $("#domainAppName").val(), $("#domainVersion").val(), $("#sign").val()],
                    ['published_at', '1769928235'],
                    // ['summary', $("#summary").val()],
                    ['limit', '1000', '10000'],
                    [
                        'permit2', $("#nonce").val(), $("#permit2_signature").val(), $('#payer').val(), $('#payer').val(), "0xc73a3413406d719ea763d4d122eb371a1956292f", "0x000000000022D473030F116dDEE9F6B43aC78BA3", "Permit2"
                    ],
                    ['token', $("#symbol").val(), $("#chain").val(), "stokenet", $("#token_addr").val(), '1000', $("#chainId").val(), $("#expiryTime").val(), '0'],
                    ['quote', $("#number").val(), $("#currency").val(), "71900000", $("#sign").val(), "-100000", "0"],
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
                'created_at': 1769928235,
                'sig': '07a50f926b69bfb8fdaaba836dcebc33b6dda64ff2ee5e1201985b65f956d97b0d6bc9629aacd012fafec09abf294b5ff2f514a5aa08ef7ef261eced5e3b210e'
            }
        )
        + "]";
}