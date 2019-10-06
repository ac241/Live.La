package com.acel.streamlivetool.bean;

import android.os.Parcel;
import android.os.Parcelable;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class Anchor implements Parcelable {
    @Id(autoincrement = true)
    public Long id;
    public String platform;
    public String nickname;
    public String showId;
    public String roomId;
    public String otherParams;

    public Anchor(String platform, String nickname, String showId, String roomId) {
        this.platform = platform;
        this.nickname = nickname;
        this.showId = showId;
        this.roomId = roomId;
        this.otherParams = "";
    }

    @Generated(hash = 1300296455)
    public Anchor(Long id, String platform, String nickname, String showId,
                  String roomId, String otherParams) {
        this.id = id;
        this.platform = platform;
        this.nickname = nickname;
        this.showId = showId;
        this.roomId = roomId;
        this.otherParams = otherParams;
    }

    @Generated(hash = 257379719)
    public Anchor() {
    }

    protected Anchor(Parcel in) {
        platform = in.readString();
        nickname = in.readString();
        showId = in.readString();
        roomId = in.readString();
        otherParams = in.readString();
    }

    public static final Creator<Anchor> CREATOR = new Creator<Anchor>() {
        @Override
        public Anchor createFromParcel(Parcel in) {
            return new Anchor(in);
        }

        @Override
        public Anchor[] newArray(int size) {
            return new Anchor[size];
        }
    };

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getShowId() {
        return this.showId;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getOtherParams() {
        return this.otherParams;
    }

    public void setOtherParams(String otherParams) {
        this.otherParams = otherParams;
    }

    public String getAnchorKey() {
        return platform + roomId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(platform);
        dest.writeString(nickname);
        dest.writeString(showId);
        dest.writeString(roomId);
        dest.writeString(otherParams);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            Anchor objAnchor = (Anchor) obj;
            return platform.equals(objAnchor.platform) && roomId.equals(objAnchor.roomId);
        } else {
            return false;
        }
    }
}
