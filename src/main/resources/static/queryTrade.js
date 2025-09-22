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

// function replaceHash(id_hash) {
//     let clickNow = Date.now();
//     return "["
//         + "\"REQ\","
//         + "\"" + id_hash + "\","
//         + JSON.stringify(
//             {
//                 'compositionQuery': {
//                     'kind': 30078,
//                     'anyMatchList': [
//                         {'pubkey': $('#pubKey').val() ? [].concat($('#pubKey').val().split(",")) : []},
//                         {'nip05': []}
//                     ]
//                 }
//             }
//         )
//         + "]";
function replaceHash(id_hash) {
    let clickNow = Date.now();
    return "["
        + "\"REQ\","
        + "\"" + id_hash + "\","
        + JSON.stringify(
            {"compositionQuery":{"kind":30078,"anyMatchList":[{"pubkey":["287af4fd16ae51fbca1cb2f01825de8108b40b0b958b1718033c83577a7331ef"]},{"nip05":["dust222@lighter.im"]}]}}
        ) + "," +JSON.stringify(
            {"compositionQuery":{"kind":30077,"anyMatchList":[{"side":["sell","buy"]},{"currency":["CNY","USD"]},{"symbol":["xrd","usdt"]},{"paymentMethod":["alipay","wechat","bank","cash"]}]}}
        )
        + "]";

}