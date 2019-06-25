package com.kaltura.kflow.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
public class PartnerEntity {

    private String name;

    @SerializedName("parthnerid")
    private String partnerId;

    public String getName() {
        return name;
    }

    public String getPartnerId() {
        return partnerId;
    }
}
