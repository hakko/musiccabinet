create function music.update_albuminfo() returns int as $$
begin

	-- assumes that artists and albums already exist

	-- update all import rows to correct artist id
	update music.albuminfo_import set artist_id = music.artist.id
	from music.artist
		where upper(music.albuminfo_import.artist_name) = music.artist.artist_name;

	-- update all import rows to correct album id
	update music.albuminfo_import set album_id = music.album.id
	from music.album
		where music.albuminfo_import.artist_id = music.album.artist_id
			and upper(music.albuminfo_import.album_name) = music.album.album_name;

	-- update existing (changed) album info
	update music.albuminfo set
		smallimageurl = music.albuminfo_import.smallimageurl,
		mediumimageurl = music.albuminfo_import.mediumimageurl,
		largeimageurl = music.albuminfo_import.largeimageurl,
		extralargeimageurl = music.albuminfo_import.extralargeimageurl,
		listeners = music.albuminfo_import.listeners,
		playcount = music.albuminfo_import.playcount
	from music.albuminfo_import
		where music.albuminfo_import.album_id = music.albuminfo.album_id
		and music.albuminfo_import.album_id is not null;

	-- add new album infos.
	insert into music.albuminfo (album_id, smallimageurl, mediumimageurl, 
		largeimageurl, extralargeimageurl, listeners, playcount)
	select album_id, smallimageurl, mediumimageurl, 
		largeimageurl, extralargeimageurl, listeners, playcount
	from music.albuminfo_import
		where not exists (select 1 from music.albuminfo
			where album_id = music.albuminfo_import.album_id);

	return 0;

end;
$$ language plpgsql;