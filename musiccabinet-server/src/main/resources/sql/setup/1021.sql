alter table library.artist add column hasalbums boolean not null default false;

insert into library.artist (artist_id)
select distinct artist_id from library.filetag
where artist_id not in (select artist_id from library.artist);

update library.artist la
	set artist_name_search = to_tsvector('english', artist_name)
from music.artist ma
where ma.id = la.artist_id and la.artist_name_search is null;

update library.artist art
	set hasalbums = true
from library.album la 
inner join music.album ma on la.album_id = ma.id 
where ma.artist_id = art.artist_id;
