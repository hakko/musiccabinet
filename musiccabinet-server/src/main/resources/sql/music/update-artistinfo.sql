create function music.update_artistinfo() returns int as $$
begin

	-- update all import rows to correct artist id
	update music.artistinfo_import set artist_id = music.artist.id
	from music.artist
		where upper(music.artistinfo_import.artist_name) = music.artist.artist_name;

	-- update existing (changed) artist info
	update music.artistinfo set
		smallimageurl = music.artistinfo_import.smallimageurl,
		mediumimageurl = music.artistinfo_import.mediumimageurl,
		largeimageurl = music.artistinfo_import.largeimageurl,
		extralargeimageurl = music.artistinfo_import.extralargeimageurl,
		listeners = music.artistinfo_import.listeners,
		playcount = music.artistinfo_import.playcount,
		biosummary = music.artistinfo_import.biosummary,
		biocontent = music.artistinfo_import.biocontent
	from music.artistinfo_import
		where music.artistinfo.artist_id = music.artistinfo_import.artist_id;

	-- add new artist infos.
	insert into music.artistinfo (artist_id, smallimageurl, mediumimageurl, 
		largeimageurl, extralargeimageurl, listeners, playcount, biosummary,
		biocontent)
	select artist_id, smallimageurl, mediumimageurl, 
		largeimageurl, extralargeimageurl, listeners, playcount, biosummary,
		biocontent
	from music.artistinfo_import
		where not exists (select 1 from music.artistinfo
			where artist_id = music.artistinfo_import.artist_id);

	return 0;

end;
$$ language plpgsql;