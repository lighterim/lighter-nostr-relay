package com.prosilion.superconductor.entity.event;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AddressBookEvent {

    @SerializedName("id")
    private String id;

    @SerializedName("kind")
    private int kind;

    @SerializedName("content")
    private String content;

    @SerializedName("tags")
    private List<List<String>> tags;

    @SerializedName("pubkey")
    private String pubkey;

    @SerializedName("created_at")
    private long createdAt;

    @SerializedName("sig")
    private String sig;
}
