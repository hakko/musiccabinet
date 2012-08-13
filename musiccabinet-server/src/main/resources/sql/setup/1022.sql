insert into library.artist (artist_id)
select distinct artist_id from 
(select artist_id from library.filetag union
 select album_artist_id from library.filetag where album_artist_id is not null) artists
where not exists (
	select 1 from library.artist where artist_id = artists.artist_id
);

update library.artist la
	set artist_name_search = to_tsvector('english', artist_name)
from music.artist ma
where ma.id = la.artist_id and la.artist_name_search is null;

update library.artist art
	set hasalbums = true
from library.album la 
inner join music.album ma on la.album_id = ma.id 
where ma.artist_id = art.artist_id;
