function hashThenSend() {
    console.log('11111')
    const concat = [
        '0',
        Date.now(),
        $("#pubkey").val(),
        $("#created_at").val(),
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
                    ['eip712', $("#walletAddress").val(), $("#contractAddress").val(), $("#domainAppName").val(), $("#domainVersion").val(), $("#sign").val()],
                    ['relays', 'wss://nostr-relay.lighter.im'],
                    ['created_by', '', 'notice@lighter.im', 'aaad79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984', $("#tradeId").val()],
                    ['ledger', 'Ethereum','sepolia', '22150315762d4124f8c95180bfdaf13422a986625d722cd0626b106907369657', 'https://sepolia.etherscan.io/address/', 'CreateEscrowEvent']
                ],
                'pubkey': $("#pubkey").val(),
                'created_at': Date.now(),
                'sig': '86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546'
            }
        )
        + "]";
}