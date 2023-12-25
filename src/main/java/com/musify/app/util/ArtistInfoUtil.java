package com.musify.app.util;

import com.musify.app.dto.MusicBrainzResponse;
import com.musify.app.dto.WikiDataResponse;
import com.musify.app.exception.UnparseableDataException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class ArtistInfoUtil {
    public final static String TYPE_WIKIDATA = "wikidata";
    public static Optional<String> getLastPath(String urlStr) {
        try {
            URL url = new URL(urlStr);
            String path = url.getPath();
            String lastPath = path.substring(path.lastIndexOf("/") + 1);
            String decodedPath = URLDecoder.decode(lastPath);
            return Optional.ofNullable(decodedPath);
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }

    public static String extractWikipediaTitle(WikiDataResponse response, String wikiId) {
        String url = Optional
            .ofNullable(response.getEntities())
            .map(entities -> entities.get(wikiId))
            .map(WikiDataResponse.WikiDataEntity::getSiteLinks)
            .map(WikiDataResponse.WikiDataSiteLinks::getEnwiki)
            .map(WikiDataResponse.SiteLink::getUrl)
            .orElseThrow(() -> new UnparseableDataException("Could not find necessary data in WikiData."));
        return getLastPath(url)
            .orElseThrow(() -> new UnparseableDataException("Failed to parse url for Wikipedia."));
    }

    public static Optional<String> extractWikiDataItem(MusicBrainzResponse response) {
        // api doesn't guarantee only one item of type wikidata,
        // while it seems so if looking at example response.
        return Optional
            .ofNullable(response.getRelations())
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .filter(relation -> TYPE_WIKIDATA.equals(relation.getType()))
            .filter(relation -> (relation.getUrl() != null && relation.getUrl().getResource()!= null))
            .map(relation -> relation.getUrl().getResource())
            .map(ArtistInfoUtil::getLastPath)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

}
