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
                    ['take','buy', $("#eventId").val(), $("#userId").val(), $("#pubkey").val(), $("#volume").val(), "seller1@lighter.im", $("#pubkey").val()],
                    ["token", "XRD", "radixDLT", "stokenet", "resource_tdx_2_1tknxxxxxxxxxradxrdxxxxxxxxx009923554798xxxxxxxxxtfd2jc", "1000"],
                    ["quote", "0.23", "CNY", "7.19"],
                    ["payment", "alipay", "alipay.accountNo", "alipay.qrCode", "alipay.memo"],
                ],
                'pubkey': $("#pubkey").val(),
                'created_at': Date.now(),
                'sig': '86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546'
            }
        )
        + "]";
}