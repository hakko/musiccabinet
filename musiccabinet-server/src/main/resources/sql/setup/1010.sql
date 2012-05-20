create table library.lastfmuser (id serial primary key, lastfm_user text not null, lastfm_user_capitalization text not null);

create table library.usertopartist (lastfmuser_id integer references library.lastfmuser (id) not null, artist_id integer references music.artist (id) not null, rank integer not null, days integer not null);
create table library.usertopartist_import (lastfmuser_id integer references library.lastfmuser (id), lastfm_user text not null, artist_id integer references music.artist (id), artist_name text not null, rank integer not null, days integer not null);

insert into library.webservice_calltype (id, description) values (7, 'Last.fm user.getTopArtists');

alter table library.webservice_history add column lastfmuser_id integer references library.lastfmuser (id);
