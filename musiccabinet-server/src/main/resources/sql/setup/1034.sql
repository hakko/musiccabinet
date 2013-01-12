delete from music.artistinfo where artist_id is null;

delete from library.webservice_history h where h.calltype_id = 5 and not exists 
(select 1 from music.artistinfo ai where ai.artist_id = h.artist_id);
