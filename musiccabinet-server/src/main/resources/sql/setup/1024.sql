create table music.tagtopartist (tag_id integer references music.tag (id) not null, artist_id integer references music.artist (id) not null, rank integer not null);
create table music.tagtopartist_import (tag_id integer references music.tag (id), tag_name text not null, artist_id integer references music.artist (id), artist_name text not null, rank integer not null);

insert into library.webservice_calltype (id, description) values (8, 'Last.fm tag.getTopArtists');

alter table library.webservice_history add column tag_id integer references music.tag (id);
