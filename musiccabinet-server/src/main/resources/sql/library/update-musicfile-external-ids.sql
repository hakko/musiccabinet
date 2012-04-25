create function library.update_musicfile_external_ids() returns int as $$
begin

	-- update all import rows to correct artist id
	update library.musicfile_import set artist_id = music.artist.id
	from music.artist
		where upper(library.musicfile_import.artist_name) = music.artist.artist_name;

	-- update all import rows to correct track id
	update library.musicfile_import set track_id = music.track.id
	from music.track
		where library.musicfile_import.artist_id = music.track.artist_id and
			  upper(library.musicfile_import.track_name) = music.track.track_name;
		
	-- update external ids of existing music files
	update library.musicfile
		set external_id = library.musicfile_import.external_id
	from library.musicfile_import
		where library.musicfile_import.track_id = library.musicfile.track_id;

	return 0;

end;
$$ language plpgsql;