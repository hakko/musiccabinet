create table music.artistinfo (id serial primary key, artist_id integer references music.artist (id), smallimageurl text, mediumimageurl text, largeimageurl text, extralargeimageurl text, listeners integer, playcount integer, biosummary text);
create table music.artistinfo_import (artist_name text not null, artist_id integer references music.artist (id), smallimageurl text, mediumimageurl text, largeimageurl text, extralargeimageurl text, listeners integer, playcount integer, biosummary text);

insert into library.webservice_calltype (id, description) values (5, 'Last.fm artist.getInfo');
