package com.github.hakko.musiccabinet.service.library;

import static java.util.regex.Pattern.compile;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang.math.NumberUtils.toInt;
import static org.jaudiotagger.tag.FieldKey.ALBUM;
import static org.jaudiotagger.tag.FieldKey.ALBUM_ARTIST_SORT;
import static org.jaudiotagger.tag.FieldKey.ARTIST;
import static org.jaudiotagger.tag.FieldKey.ARTIST_SORT;
import static org.jaudiotagger.tag.FieldKey.COMPOSER;
import static org.jaudiotagger.tag.FieldKey.DISC_NO;
import static org.jaudiotagger.tag.FieldKey.DISC_TOTAL;
import static org.jaudiotagger.tag.FieldKey.GENRE;
import static org.jaudiotagger.tag.FieldKey.LYRICS;
import static org.jaudiotagger.tag.FieldKey.TITLE;
import static org.jaudiotagger.tag.FieldKey.TRACK;
import static org.jaudiotagger.tag.FieldKey.TRACK_TOTAL;
import static org.jaudiotagger.tag.FieldKey.YEAR;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.reference.GenreTypes;

import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.domain.model.library.MetaData.Mediatype;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;

public class AudioTagService {

	private static final Logger LOG = Logger.getLogger(AudioTagService.class);

	private static final String ALBUM_ARTIST = "TPE2";
	
	private Set<String> ALLOWED_EXTENSIONS = new HashSet<>();

	private static final Pattern GENRE_PATTERN = compile("\\((\\d+)\\).*");
    private static final Pattern TRACK_NUMBER_PATTERN = compile("(\\d+)/\\d+");

	public static final String UNKNOWN_ALBUM = "[Unknown album]";
	
	public AudioTagService() {
		for (MetaData.Mediatype mediaType : MetaData.Mediatype.values()) {
			ALLOWED_EXTENSIONS.add(mediaType.getFilesuffix());
		}
		LogManager.getLogManager().reset(); // turns off verbose JAudioTagger log
	}

	public void updateMetadata(File file) {

		String extension = getExtension(file.getFilename()).toUpperCase();
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			return;
		}

		MetaData metaData = new MetaData();
		metaData.setMediaType(Mediatype.valueOf(extension));

		try {
			AudioFile audioFile = AudioFileIO.read(new java.io.File(file
					.getDirectory(), file.getFilename()));
			
			Tag tag = audioFile.getTag();
			if (tag != null) {
				metaData.setArtist(getTagField(tag, ARTIST));
				metaData.setArtistSort(getTagField(tag, ARTIST_SORT));
				metaData.setAlbumArtist(toAlbumArtist(tag));
				metaData.setAlbumArtistSort(getTagField(tag, ALBUM_ARTIST_SORT));
				metaData.setAlbum(toAlbum(getTagField(tag, ALBUM)));
				metaData.setTitle(getTagField(tag, TITLE));
				metaData.setYear(getTagField(tag, YEAR));
				metaData.setGenre(toGenre(getTagField(tag, GENRE)));
				metaData.setLyrics(getTagField(tag, LYRICS));
				metaData.setComposer(getTagField(tag, COMPOSER));
				metaData.setDiscNr(toFirstNumber(getTagField(tag, DISC_NO)));
				metaData.setDiscNrs(toShort(getTagField(tag, DISC_TOTAL)));
				metaData.setTrackNr(toFirstNumber(getTagField(tag, TRACK)));
				metaData.setTrackNrs(toShort(getTagField(tag, TRACK_TOTAL)));
				metaData.setCoverArtEmbedded(tag.getFirstArtwork() != null);
			}

			AudioHeader audioHeader = audioFile.getAudioHeader();
			if (audioHeader != null) {
				metaData.setVbr(audioHeader.isVariableBitRate());
				metaData.setBitrate((short) audioHeader.getBitRateAsNumber());
				metaData.setDuration((short) audioHeader.getTrackLength());
			}

			file.setMetaData(metaData);
			
		} catch (CannotReadException | IOException | TagException
				| ReadOnlyFileException | InvalidAudioFrameException 
				| RuntimeException e) { 
			// AudioFileIO has been seen to throw NumberFormatException
			LOG.warn("Could not read metadata of file " + file.getFilename()
					+ " from " + file.getDirectory(), e);
		}
	}

	public boolean isAudioFile(String extension) {
		return extension != null && ALLOWED_EXTENSIONS.contains(extension.toUpperCase());
	}
	
    public Artwork getArtwork(java.io.File file) throws ApplicationException {
    	Tag tag = null;
    	try {
    		AudioFile audioFile = AudioFileIO.read(file);
    		tag = audioFile.getTag();
    	} catch (CannotReadException | IOException | TagException
    			| ReadOnlyFileException | InvalidAudioFrameException 
    			| RuntimeException e) {
    		throw new ApplicationException("Failed reading artwork from file " + file, e);
    	}
        return tag == null ? null : tag.getFirstArtwork();
    }
	
	private String getTagField(Tag tag, FieldKey fieldKey) {
		try {
			return StringUtils.trimToNull(tag.getFirst(fieldKey));
		} catch (Exception e) {
			LOG.warn("JAudioTagger failed reading tag!", e);
		}
		return null;
	}
	
	private String toAlbumArtist(Tag tag) {
		String albumArtist = getTagField(tag, FieldKey.ALBUM_ARTIST);
		if (albumArtist == null && tag instanceof AbstractID3v2Tag && tag.hasField(ALBUM_ARTIST)) {
			// TPE2 is commonly used for "Album artist", but JAudioTagger doesn't pick it up
			albumArtist = StringUtils.trimToNull(tag.getFirst(ALBUM_ARTIST));
		}
		return albumArtist;
	}

	/**
	 * Sometimes the genre is returned as "(17)" or "(17)Rock", instead of
	 * "Rock". Try mapping to the latter format.
	 */
	private String toGenre(String genre) {
		if (genre == null) {
			return null;
		}
		Matcher matcher = GENRE_PATTERN.matcher(genre);
		if (matcher.matches()) {
			return GenreTypes.getInstanceOf().getValueForId(
					toInt(matcher.group(1), -1));
		}
		return genre;
	}

	private String toAlbum(String album) {
		return album == null ? UNKNOWN_ALBUM : album;
	}

	/*
	 * Track and disc number are allowed to be on form x/y.
	 */
	private Short toFirstNumber(String tag) {
		if (tag == null) {
			return null;
		} else if (NumberUtils.isDigits(tag)) { 
			return NumberUtils.toShort(tag);
		} else {
            Matcher matcher = TRACK_NUMBER_PATTERN.matcher(tag);
            if (matcher.matches()) {
            	return NumberUtils.toShort(matcher.group(1));
            }
		}
		return null;
	}
	
	private Short toShort(String tag) {
		tag = StringUtils.trimToEmpty(tag);
		return NumberUtils.isDigits(tag) ? NumberUtils.toShort(tag) : null;
	}

}