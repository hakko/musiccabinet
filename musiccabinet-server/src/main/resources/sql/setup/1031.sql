create table music.mb_album_type (id integer primary key, description text not null);
insert into music.mb_album_type (id, description) values (0, 'single');
insert into music.mb_album_type (id, description) values (1, 'ep');
insert into music.mb_album_type (id, description) values (2, 'album');

create table music.mb_format (id serial primary key, description text not null);
insert into music.mb_format (description) values ('CD');
insert into music.mb_format (description) values ('SACD');
insert into music.mb_format (description) values ('HDCD');
insert into music.mb_format (description) values ('DualDisc');
insert into music.mb_format (description) values ('Vinyl');
insert into music.mb_format (description) values ('7" Vinyl');
insert into music.mb_format (description) values ('10" Vinyl');
insert into music.mb_format (description) values ('12" Vinyl');
insert into music.mb_format (description) values ('Cassette');
insert into music.mb_format (description) values ('DVD-Audio');
insert into music.mb_format (description) values ('DVD-Video');
insert into music.mb_format (description) values ('DVD');
insert into music.mb_format (description) values ('Blu-ray');
insert into music.mb_format (description) values ('MiniDisc');
insert into music.mb_format (description) values ('USB Flash Drive');
insert into music.mb_format (description) values ('8cm CD');
insert into music.mb_format (description) values ('VCD');
insert into music.mb_format (description) values ('CD-R');
insert into music.mb_format (description) values ('Digital Media');

create table music.mb_label (id serial primary key, mbid char(36) not null, name text not null);
create table music.mb_artist (artist_id integer references music.artist (id) not null, mbid char(36) not null, country_code char(2), start_year smallint, active boolean);
create table music.mb_album (album_id integer references music.album (id) not null, mbid char(36) not null, type_id integer references music.mb_album_type (id) not null, label_id integer references music.mb_label (id), format_id integer references music.mb_format (id), first_release_year smallint);

create table music.mb_artist_import (artist_id integer references music.artist (id), artist_name text not null, mbid char(36) not null, country_code char(2), start_year smallint, active boolean);
create table music.mb_album_import (artist_id integer references music.artist(id) not null, album_id integer references music.album (id), title text not null, type_id integer references music.mb_album_type (id) not null, release_year smallint, label_id integer references music.mb_label (id), label_name text, label_mbid char(36), format text, format_id integer references music.mb_format (id), release_group_mbid char(36) not null);

insert into library.webservice_calltype (id, description) values (11, 'MusicBrainz.org artist.query');
insert into library.webservice_calltype (id, description) values (12, 'MusicBrainz.org artist.release');
