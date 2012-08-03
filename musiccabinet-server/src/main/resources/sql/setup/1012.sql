create table music.duplicate_artists (artist_id integer);

insert into music.duplicate_artists (artist_id)
select id from music.artist where artist_name in (
	select artist_name from music.artist 
	group by artist_name having count(artist_name) > 1
);

delete from library.artisttoptrackplaycount where artist_id in (
	select artist_id from music.duplicate_artists
);
	
truncate library.musicdirectory_import;
delete from library.musicdirectory where artist_id in (
	select artist_id from music.duplicate_artists
);

truncate library.musicfile_import;
delete from library.musicfile where track_id in (
	select id from music.track where artist_id in (
		select artist_id from music.duplicate_artists
	)
);

truncate library.trackplaycount_import;
delete from library.trackplaycount where track_id in (
	select id from music.track where artist_id in (
		select artist_id from music.duplicate_artists
	)
);

truncate library.usertopartist_import;
delete from library.usertopartist where artist_id in (
	select artist_id from music.duplicate_artists
);

delete from library.webservice_history where artist_id in (
	select artist_id from music.duplicate_artists
);

delete from library.webservice_history where album_id in (
	select id from music.album where artist_id in (
		select artist_id from music.duplicate_artists
	)
);

truncate music.albuminfo_import;
truncate music.artistinfo_import;
truncate music.artistrelation_import;
truncate music.artisttoptag_import;
truncate music.artisttoptrack_import;
truncate music.trackrelation_import;

delete from music.albuminfo where album_id in (
	select id from music.album where artist_id in (
		select artist_id from music.duplicate_artists
	)
);

delete from music.artistinfo where artist_id in (
	select artist_id from music.duplicate_artists
);

delete from music.artistrelation where source_id in (
	select artist_id from music.duplicate_artists
);
delete from music.artistrelation where target_id in (
	select artist_id from music.duplicate_artists
);

delete from music.artisttoptag where artist_id in (
	select artist_id from music.duplicate_artists
);

delete from music.artisttoptrack where artist_id in (
	select artist_id from music.duplicate_artists
);

delete from music.album where artist_id in (
	select artist_id from music.duplicate_artists
);

delete from music.track where artist_id in (
	select artist_id from music.duplicate_artists
);

delete from music.artist where id in (
	select artist_id from music.duplicate_artists
);

drop table music.duplicate_artists;

create unique index artist_artistname on music.artist (artist_name);
