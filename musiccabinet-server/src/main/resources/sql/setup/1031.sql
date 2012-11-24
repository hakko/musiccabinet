create table music.mb_album_type (id integer primary key, description text not null);
insert into music.mb_album_type (id, description) values (0, 'single');
insert into music.mb_album_type (id, description) values (1, 'ep');
insert into music.mb_album_type (id, description) values (2, 'album');
insert into music.mb_album_type (id, description) values (3, 'compilation');
insert into music.mb_album_type (id, description) values (4, 'soundtrack');
insert into music.mb_album_type (id, description) values (5, 'spokenword');
insert into music.mb_album_type (id, description) values (6, 'interview');
insert into music.mb_album_type (id, description) values (7, 'audiobook');
insert into music.mb_album_type (id, description) values (8, 'live');
insert into music.mb_album_type (id, description) values (9, 'remix');
insert into music.mb_album_type (id, description) values (10, 'other');

create table music.mb_artist (artist_id integer references music.artist (id) not null, mbid char(36) not null, country_code char(2), start_year smallint, active boolean);
create table music.mb_album (album_id integer references music.album (id) not null, mbid char(36) not null, type_id integer references music.mb_album_type (id) not null, release_year smallint);

create table music.mb_artist_import (artist_id integer references music.artist (id), artist_name text not null, mbid char(36) not null, country_code char(2), start_year smallint, active boolean);
create table music.mb_album_import (artist_id integer references music.artist(id) not null, album_id integer references music.album (id), album_name text not null, mbid char(36) not null, type_id integer references music.mb_album_type (id), release_year smallint);

insert into library.webservice_calltype (id, description) values (11, 'MusicBrainz.org artist.query');
insert into library.webservice_calltype (id, description) values (12, 'MusicBrainz.org artist.release-groups');
