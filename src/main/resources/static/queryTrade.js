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
            {"compositionQuery":{"kind":30078,"anyMatchList":[{"pubkey":["aaad79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984"]},{"nip05":["0x846eFbf0F3d91C8896B557Ff27b7E53D76637AF1"]}]}}
        ) + "," +JSON.stringify(
            {"compositionQuery":{"kind":30077,"anyMatchList":[{"side":["sell","buy"]},{"currency":["CNY","USD"]},{"symbol":["xrd","usdt"]},{"paymentMethod":["alipay","wechat","bank","cash"]}]}}
        )
        + "]";

}