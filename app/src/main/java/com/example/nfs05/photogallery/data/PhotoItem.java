package com.example.nfs05.photogallery.data;

import com.google.gson.annotations.SerializedName;

public class PhotoItem {

        @SerializedName("owner")
        private String owner;

        @SerializedName("server")
        private String server;

        @SerializedName("height_s")
        private String heightS;

        @SerializedName("width_s")
        private String widthS;

        @SerializedName("url_s")
        private String urlS;

        @SerializedName("ispublic")
        private int ispublic;

        @SerializedName("isfriend")
        private int isfriend;

        @SerializedName("farm")
        private int farm;

        @SerializedName("id")
        private String id;

        @SerializedName("secret")
        private String secret;

        @SerializedName("title")
        private String title;

        @SerializedName("isfamily")
        private int isfamily;

        public void setOwner(String owner){
            this.owner = owner;
        }

        public String getOwner(){
            return owner;
        }

        public void setServer(String server){
            this.server = server;
        }

        public String getServer(){
            return server;
        }

        public void setHeightS(String heightS){
            this.heightS = heightS;
        }

        public String getHeightS(){
            return heightS;
        }

        public void setWidthS(String widthS){
            this.widthS = widthS;
        }

        public String getWidthS(){
            return widthS;
        }

        public void setUrlS(String urlS){
            this.urlS = urlS;
        }

        public String getUrlS(){
            return urlS;
        }

        public void setIspublic(int ispublic){
            this.ispublic = ispublic;
        }

        public int getIspublic(){
            return ispublic;
        }

        public void setIsfriend(int isfriend){
            this.isfriend = isfriend;
        }

        public int getIsfriend(){
            return isfriend;
        }

        public void setFarm(int farm){
            this.farm = farm;
        }

        public int getFarm(){
            return farm;
        }

        public void setId(String id){
            this.id = id;
        }

        public String getId(){
            return id;
        }

        public void setSecret(String secret){
            this.secret = secret;
        }

        public String getSecret(){
            return secret;
        }

        public void setTitle(String title){
            this.title = title;
        }

        public String getTitle(){
            return title;
        }

        public void setIsfamily(int isfamily){
            this.isfamily = isfamily;
        }

        public int getIsfamily(){
            return isfamily;
        }

        @Override
        public String toString(){
            return
                    "PhotoItem{" +
                            "owner = '" + owner + '\'' +
                            ",server = '" + server + '\'' +
                            ",height_s = '" + heightS + '\'' +
                            ",width_s = '" + widthS + '\'' +
                            ",url_s = '" + urlS + '\'' +
                            ",ispublic = '" + ispublic + '\'' +
                            ",isfriend = '" + isfriend + '\'' +
                            ",farm = '" + farm + '\'' +
                            ",id = '" + id + '\'' +
                            ",secret = '" + secret + '\'' +
                            ",title = '" + title + '\'' +
                            ",isfamily = '" + isfamily + '\'' +
                            "}";
        }
    }

