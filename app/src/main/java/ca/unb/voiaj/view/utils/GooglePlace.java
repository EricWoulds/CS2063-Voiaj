package ca.unb.voiaj.view.utils;

import androidx.annotation.NonNull;

public class GooglePlace {
    private String name;
    private String lng;
    private  String lat;

    public GooglePlace(Builder builder) {
        this.name = builder.name;
        this.lng = builder.lng;
        this.lat = builder.lat;
    }

    public String getName() {
        return name;
    }

    public String getLongitude(){return lng; }

    public String getLatitude(){return lat; }

    public static class Builder {
        private String name;
        private String lat;
        private String lng;

        public Builder(@NonNull String name,
                       @NonNull String lat,
                       @NonNull String lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }

        public GooglePlace build(){return new GooglePlace(this);}
    }

}