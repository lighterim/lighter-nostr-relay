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
                'id': '5f33c7de1b9f9115550eb8ee96891dd6e3f968a16a6371ccb7f0e0b41e5c392f',
                'kind': 30078,
                'content': "",
                'tags': [
                    [
                        "relays",
                        "wss://nostr-relay.lighter.im"
                    ],
                    [
                        "published_at",
                        1766978293
                    ],
                    [
                        "eip712",
                        "0xece12A4E213990bC00fe3BCf89E1Df5dF37D663e",
                        "0x6Cd90338966872522Ed24CB2A4b756FC36556a60",
                        "MainnetUserTxn",
                        "1",
                        "0xd523e429a696a0332c6312eeb18c9851d85df852f2c82bf8b6172fc27596ee315d22d01991d1a8bc334527ab0330a65a53ddb1d2643a0f1773ceeba9e4b954591c"
                    ],
                    [
                        "permit2",
                        "0",
                        "",
                        "0xc7E15B88802D31134F8be052C8c99897779e417C",
                        "0xD336000b7004c9F1F0f608058523eF5C00DC78a6",
                        "0xece12A4E213990bC00fe3BCf89E1Df5dF37D663e",
                        "0x000000000022D473030F116dDEE9F6B43aC78BA3",
                        "Permit2"
                    ],
                    [
                        "take",
                        "buy",
                        "4be60269ff10adbac55b6b6e71875cd7f90a923a06ac222df3c205b29b79c7f8",
                        "0xc7E15B88802D31134F8be052C8c99897779e417C",
                        "c504daa92a2fd806fddaaf695222131209cb190ec64e60e4a9774e557b159f1c",
                        "1000000000000",
                        "0xece12A4E213990bC00fe3BCf89E1Df5dF37D663e",
                        "18ebd6a0719c0c5665141d120af2131ae1b97d90f19fabadc5ed75421cff5954",
                        "0",
                        "0",
                        "0xc7E15B88802D31134F8be052C8c99897779e417C"
                    ],
                    [
                        "token",
                        "USDC",
                        "ethereum",
                        "Sepolia",
                        "0x1c7D4B196Cb0C7B01d743Fbc6116a902379C7238",
                        "1000000",
                        "11155111",
                        "1767582819"
                    ],
                    [
                        "limit",
                        "1000000",
                        "1000000"
                    ],
                    [
                        "quote",
                        "1000000000000000000",
                        "USD",
                        "0",
                        "02e86f8cb7fd611e4430a2d9e73f17a943bccb5ecb00294d2ef47805bcee132a32353d7865e2484b9fde4293b4f1c4cbeb1774b368cb65c89b87bf308c7bce03",
                        "0"
                    ],
                    [
                        "payment",
                        "alipay",
                        "123456",
                        "wxp://f2f02FDYPZWmirgF2ZYjMmuarYvoVNUQ4cnoyfA_9HCBo90",
                        "buyer account"
                    ]
                ],
                //'trade_id': 1,
                'pubkey': '18ebd6a0719c0c5665141d120af2131ae1b97d90f19fabadc5ed75421cff5954',
                'created_at': Date.now(),
                'sig': '86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546'
            }
        )
        + "]";
}