create extension if not exists pg_trgm;

alter table library.artist drop column artist_name_search;
alter table library.album drop column album_name_search;
alter table library.track drop column track_name_search;

alter table library.artist add column artist_name_search text;
alter table library.album add column album_name_search text;
alter table library.track add column track_name_search text;

update library.artist la
	set artist_name_search = array_to_string(array(select unnest(string_to_array(artist_name, ' ')) order by 1), ' ')
from music.artist ma
where ma.id = la.artist_id;

update library.album la
	set album_name_search = array_to_string(array(select unnest(string_to_array(artist_name || ' ' || album_name, ' ')) order by 1), ' ')
from music.album malb
inner join music.artist mart on malb.artist_id = mart.id
where malb.id = la.album_id;

update library.track lt
	set track_name_search = array_to_string(array(select unnest(string_to_array(artist_name || ' ' || album_name || ' ' || track_name, ' ')) order by 1), ' ')
from music.album malb, music.track mt 
inner join music.artist mart on mt.artist_id = mart.id
where mt.id = lt.track_id and malb.id = lt.album_id;

create index artist_name_search on library.artist using gin (artist_name_search gin_trgm_ops);
create index album_name_search on library.album using gin (album_name_search gin_trgm_ops);
create index track_name_search on library.track using gin (track_name_search gin_trgm_ops);
