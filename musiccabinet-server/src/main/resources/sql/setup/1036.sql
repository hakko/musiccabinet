insert into library.webservice_calltype (id, description) values (13, 'Last.fm user.getLovedTracks');

create table music.lovedtrack (lastfmuser_id integer references music.lastfmuser (id) not null, track_id integer references music.track (id) not null);
create table music.lovedtrack_import (lastfm_user text not null, lastfmuser_id integer references music.lastfmuser (id), artist_name text not null, artist_id integer references music.artist (id), track_name text not null, track_id integer references music.track (id));
