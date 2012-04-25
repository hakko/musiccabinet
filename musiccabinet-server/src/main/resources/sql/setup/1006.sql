create table music.album (id serial primary key, artist_id integer references music.artist (id), album_name text not null, album_name_capitalization text not null);

create table music.albuminfo (id serial primary key, album_id integer references music.album (id) not null, smallimageurl text, mediumimageurl text, largeimageurl text, extralargeimageurl text, listeners integer, playcount integer);
create table music.albuminfo_import (artist_name text not null, artist_id integer references music.artist (id), album_name text not null, album_id integer references music.album (id), smallimageurl text, mediumimageurl text, largeimageurl text, extralargeimageurl text, listeners integer, playcount integer);

alter table library.musicdirectory add column album_id integer references music.album (id);
alter table library.musicdirectory drop column root;
alter table library.musicdirectory_import drop column root;
alter table library.musicdirectory_import add column album_id integer references music.album (id);
alter table library.musicdirectory_import add column album_name text;

alter table library.webservice_history add column album_id integer references music.album (id);

insert into library.webservice_calltype (id, description) values (6, 'Last.fm album.getInfo');

create unique index musicdirectory_path_albumid on library.musicdirectory (path, album_id);
