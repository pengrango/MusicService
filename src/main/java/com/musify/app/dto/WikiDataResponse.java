package com.musify.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WikiDataResponse {

    private Map<String, WikiDataEntity> entities;

    public Map<String, WikiDataEntity> getEntities() {
        return entities;
    }

    public void setEntities(Map<String, WikiDataEntity> entities) {
        this.entities = entities;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WikiDataEntity {
        @JsonProperty("sitelinks")
        private WikiDataSiteLinks siteLinks;

        public WikiDataSiteLinks getSiteLinks() {
            return siteLinks;
        }

        public void setSiteLinks(WikiDataSiteLinks siteLinks) {
            this.siteLinks = siteLinks;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WikiDataSiteLinks {
        private SiteLink enwiki;

        public SiteLink getEnwiki() {
            return enwiki;
        }

        public void setEnwiki(SiteLink enwiki) {
            this.enwiki = enwiki;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SiteLink {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
