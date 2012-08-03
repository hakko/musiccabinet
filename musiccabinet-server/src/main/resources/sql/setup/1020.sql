create index filetag_artistid_fileid on library.filetag (artist_id, file_id);
create index track_fileid on library.track (file_id);

update library.track lt
	set track_name_search = to_tsvector('english', artist_name || ' ' || album_name || ' ' || track_name)
from music.album malb, music.track mt 
inner join music.artist mart on mt.artist_id = mart.id
where mt.id = lt.track_id and malb.id = lt.album_id;