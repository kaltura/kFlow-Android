package com.kaltura.kflow.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
public class AccountEntity {

    private String name;

    @SerializedName("base_url")
    private String baseUrl;

    @SerializedName("partners_id")
    private List<PartnerEntity> partners;

    public String getName() {
        return name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public List<PartnerEntity> getPartners() {
        return partners;
    }
}
