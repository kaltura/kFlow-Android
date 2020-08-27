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
@MultiRequestBuilder.Tokenizer(IotClientConfiguration.Tokenizer.class)
public class IotClientConfiguration extends ObjectBase {

    public interface Tokenizer extends ObjectBase.Tokenizer {
        String announcementTopic();

        String json();
    }

    private String announcementTopic;
    private String json;

    public String getAnnouncementTopic() {
        return announcementTopic;
    }

    public void setAnnouncementTopic(String announcementTopic) {
        this.announcementTopic = announcementTopic;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public IotClientConfiguration() {
        super();
    }

    public IotClientConfiguration(JsonObject jsonObject) throws APIException {
        super(jsonObject);

        if (jsonObject == null) return;

        // set members values:
        announcementTopic = GsonParser.parseString(jsonObject.get("announcementTopic"));
        json = GsonParser.parseString(jsonObject.get("json"));
    }

    public Params toParams() {
        Params kparams = super.toParams();
        kparams.add("objectType", "KalturaIotClientConfiguration");
        kparams.add("announcementTopic", this.announcementTopic);
        kparams.add("json", this.json);
        return kparams;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.announcementTopic);
        dest.writeString(this.json);
    }

    public IotClientConfiguration(Parcel in) {
        super(in);
        this.announcementTopic = in.readString();
        this.json = in.readString();
    }
}
