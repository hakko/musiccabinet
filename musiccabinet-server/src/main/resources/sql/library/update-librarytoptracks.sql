create function library.update_librarytoptracks() returns int as $$
begin

	truncate library.artisttoptrackplaycount;

	-- add references to which artist top tracks we have locally.
	-- if multiple local matches exist for a top track, 
	-- choose using MusicBrainz type: album/EP/single/unknown
	-- if multiple local files still match, prefer earliest release year
	insert into library.artisttoptrackplaycount (track_id, artist_id, rank, play_count)
	select distinct on (lt.track_id) lt.id, att.artist_id, att.rank, coalesce(tpc.play_count, 0) from music.artisttoptrack att
	inner join library.track lt on lt.track_id = att.track_id
	inner join library.album la on la.album_id = lt.album_id
	left outer join music.mb_album mba on mba.album_id = lt.album_id
	left outer join library.trackplaycount tpc on tpc.track_id = lt.track_id
	order by lt.track_id, coalesce(mba.type_id, -1) desc, coalesce(la.year, 32767);

	-- if we have local tracks with Album Artist=X and Artist=X feat Y (or similar),
	-- try matching local tracks with top tracks by album artist instead of artist.
	insert into library.artisttoptrackplaycount (track_id, artist_id, rank, play_count)
	select distinct on (lt.track_id) lt.id, aat.artist_id, att.rank, coalesce(tpc.play_count, 0) from library.filetag ft
	inner join music.track at on ft.track_id = at.id
	inner join music.track aat on ft.album_artist_id = aat.artist_id and at.track_name = aat.track_name
	inner join library.track lt on lt.file_id = ft.file_id
	inner join music.artisttoptrack att on att.track_id = aat.id and att.artist_id = aat.artist_id
	left outer join library.trackplaycount tpc on tpc.track_id = at.id
	where ft.artist_id <> ft.album_artist_id; 
	
	return 0;

end;
$$ language plpgsql;