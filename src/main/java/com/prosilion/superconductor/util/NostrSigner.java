package com.prosilion.superconductor.util;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import nostr.base.PublicKey;
import nostr.crypto.bech32.Bech32;
import nostr.encryption.nip04.MessageCipher04;
import nostr.util.NostrException;
import nostr.util.NostrUtil;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HexFormat;

@Component
public class NostrSigner {

    @Value("${nostr.relay.private.key}")
    private String nostrPriKey;

    @Value("${nostr.relay.public.key}")
    private String nostrPubKey;

    private static PublicKey publicKey;

    private static byte[] nostrPriKeyBytes;

    private static MessageCipher04 messageCipher04;

    @PostConstruct
    public void init() {
        this.publicKey = new PublicKey(this.nostrPubKey);
        try {
            nostrPriKeyBytes = NostrUtil.hexToBytes(Bech32.fromBech32(this.nostrPriKey));
        } catch (NostrException e) {
            throw new RuntimeException(e);
        }
        messageCipher04 = new MessageCipher04(nostrPriKeyBytes, publicKey.getRawData());
    }

    public static String encrypt(String message) {
        if(!StringUtils.hasText(message)) {
            return message;
        }
        return messageCipher04.encrypt(message);
    }

    public static String decrypt(String message) {
        if(!StringUtils.hasText(message)) {
            return message;
        }
        return messageCipher04.decrypt(message);
    }
}
