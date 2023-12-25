package com.musify.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoverArtResponse {
    private List<Image> images;

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image{
        private Boolean front;
        private String image;

        public Boolean getFront() {
            return front;
        }

        public void setFront(Boolean front) {
            this.front = front;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
}
