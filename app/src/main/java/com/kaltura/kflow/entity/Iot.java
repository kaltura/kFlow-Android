package com.kaltura.kflow.entity;

import android.os.Parcel;

import com.google.gson.JsonObject;
import com.kaltura.client.Params;
import com.kaltura.client.types.APIException;
import com.kaltura.client.types.ObjectBase;
import com.kaltura.client.utils.GsonParser;
import com.kaltura.client.utils.request.MultiRequestBuilder;

/**
 * Created by alex_lytvynenko on 25.08.2020.
 */
@SuppressWarnings("serial")
@MultiRequestBuilder.Tokenizer(Iot.Tokenizer.class)
public class Iot extends ObjectBase {

    public interface Tokenizer extends ObjectBase.Tokenizer {
        String accessKey();
        String accessSecretKey();
        String endPoint();
        String extendedEndPoint();
        String identityId();
        String identityPoolId();
        String principal();
        String thingArn();
        String thingId();
        String udid();
        String username();
        String userPassword();
    }

    private String accessKey;
    private String accessSecretKey;
    private String endPoint;
    private String extendedEndPoint;
    private String identityId;
    private String identityPoolId;
    private String principal;
    private String thingArn;
    private String thingId;
    private String udid;
    private String username;
    private String userPassword;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessSecretKey() {
        return accessSecretKey;
    }

    public void setAccessSecretKey(String accessSecretKey) {
        this.accessSecretKey = accessSecretKey;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getExtendedEndPoint() {
        return extendedEndPoint;
    }

    public void setExtendedEndPoint(String extendedEndPoint) {
        this.extendedEndPoint = extendedEndPoint;
    }

    public String getIdentityId() {
        return identityId;
    }

    public void setIdentityId(String identityId) {
        this.identityId = identityId;
    }

    public String getIdentityPoolId() {
        return identityPoolId;
    }

    public void setIdentityPoolId(String identityPoolId) {
        this.identityPoolId = identityPoolId;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getThingArn() {
        return thingArn;
    }

    public void setThingArn(String thingArn) {
        this.thingArn = thingArn;
    }

    public String getThingId() {
        return thingId;
    }

    public void setThingId(String thingId) {
        this.thingId = thingId;
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public Iot() {
        super();
    }

    public Iot(JsonObject jsonObject) throws APIException {
        super(jsonObject);

        if (jsonObject == null) return;

        // set members values:
        accessKey = GsonParser.parseString(jsonObject.get("accessKey"));
        accessSecretKey = GsonParser.parseString(jsonObject.get("accessSecretKey"));
        endPoint = GsonParser.parseString(jsonObject.get("endPoint"));
        extendedEndPoint = GsonParser.parseString(jsonObject.get("extendedEndPoint"));
        identityId = GsonParser.parseString(jsonObject.get("identityId"));
        identityPoolId = GsonParser.parseString(jsonObject.get("identityPoolId"));
        principal = GsonParser.parseString(jsonObject.get("principal"));
        thingArn = GsonParser.parseString(jsonObject.get("thingArn"));
        thingId = GsonParser.parseString(jsonObject.get("thingId"));
        udid = GsonParser.parseString(jsonObject.get("udid"));
        username = GsonParser.parseString(jsonObject.get("username"));
        userPassword = GsonParser.parseString(jsonObject.get("userPassword"));
    }

    public Params toParams() {
        Params kparams = super.toParams();
        kparams.add("objectType", "KalturaIot");
        kparams.add("accessKey", this.accessKey);
        kparams.add("accessSecretKey", this.accessSecretKey);
        kparams.add("endPoint", this.endPoint);
        kparams.add("extendedEndPoint", this.extendedEndPoint);
        kparams.add("identityId", this.identityId);
        kparams.add("identityPoolId", this.identityPoolId);
        kparams.add("principal", this.principal);
        kparams.add("thingArn", this.thingArn);
        kparams.add("thingId", this.thingId);
        kparams.add("udid", this.udid);
        kparams.add("username", this.username);
        kparams.add("userPassword", this.userPassword);
        return kparams;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.accessKey);
        dest.writeString(this.accessSecretKey);
        dest.writeString(this.endPoint);
        dest.writeString(this.extendedEndPoint);
        dest.writeString(this.identityId);
        dest.writeString(this.identityPoolId);
        dest.writeString(this.principal);
        dest.writeString(this.thingArn);
        dest.writeString(this.thingId);
        dest.writeString(this.udid);
        dest.writeString(this.username);
        dest.writeString(this.userPassword);
    }

    public Iot(Parcel in) {
        super(in);
        this.accessKey = in.readString();
        this.accessSecretKey = in.readString();
        this.endPoint = in.readString();
        this.extendedEndPoint = in.readString();
        this.identityId = in.readString();
        this.identityPoolId = in.readString();
        this.principal = in.readString();
        this.thingArn = in.readString();
        this.thingId = in.readString();
        this.udid = in.readString();
        this.username = in.readString();
        this.userPassword = in.readString();
    }
}
