package gal.usc.etse.es.tapeacompostela.model.dto;

// An uploaded picture. url is what goes in imageUrl, avatarUrl or iconUrl, e.g. /images/3f2a....png
public record Image(
        String url
) {}
