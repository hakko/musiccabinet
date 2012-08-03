create function library.update_statistics() returns int as $$
begin

	update library.statistics 
		set artist_count = artist.count 
	from (select count(*) from library.artist) artist;

	update library.statistics 
		set album_count = album.count 
	from (select count(*) from library.album) album;

	update library.statistics 
		set track_count = track.count 
	from (select count(*) from library.track) track;

	update library.statistics 
		set bytes = coalesce(size.sum, 0)
	from (select sum(size) from library.file) size;
	
	update library.statistics 
		set seconds = coalesce(duration.sum, 0)
	from (select sum(duration) from library.fileheader) duration;
	
	return 0;

end;
$$ language plpgsql;