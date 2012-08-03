create table music.duplicate_tracks (track_id integer);

insert into music.duplicate_tracks (track_id)
select t1.id from music.track t1 inner join 
	(select artist_id, track_name from music.track 
		group by artist_id, track_name having count(track_name) > 1) t2
on t1.artist_id = t2.artist_id and t1.track_name = t2.track_name;

delete from library.artisttoptrackplaycount where music_file_id in (
	select mf.id from library.musicfile mf 
	inner join music.duplicate_tracks dt on mf.track_id = dt.track_id
);

truncate library.trackplaycount_import;

delete from library.trackplaycount where track_id in (
	select track_id from music.duplicate_tracks
);

truncate music.trackrelation_import;

truncate music.trackrelation;

truncate music.artisttoptrack_import;

delete from music.artisttoptrack where track_id in (
	select track_id from music.duplicate_tracks
);
	
delete from library.musicfile_import where track_id in (
	select track_id from music.duplicate_tracks
);

delete from library.musicfile where track_id in (
	select track_id from music.duplicate_tracks
);

delete from music.track where id in (
	select track_id from music.duplicate_tracks
);

drop table music.duplicate_tracks;

create unique index track_artist_trackname on music.track (artist_id, track_name);




create table music.duplicate_albums (album_id integer);

insert into music.duplicate_albums (album_id)
select a1.id from music.album a1 inner join 
	(select artist_id, album_name from music.album
		group by artist_id, album_name having count(album_name) > 1) a2
on a1.artist_id = a2.artist_id and a1.album_name = a2.album_name;

truncate music.albuminfo_import;

delete from library.webservice_history where album_id in (
	select album_id from music.duplicate_albums
);

delete from music.albuminfo where album_id in (
	select album_id from music.duplicate_albums
);

truncate library.musicdirectory_import;

delete from library.musicdirectory where album_id in (
	select album_id from music.duplicate_albums
);

delete from music.album where id in (
	select album_id from music.duplicate_albums
);

drop table music.duplicate_albums;

create unique index album_artist_albumname on music.album (artist_id, album_name);
