create table library.fileheader_type (id smallint not null primary key, extension text);
insert into library.fileheader_type (id, extension) values (0, 'OGG');
insert into library.fileheader_type (id, extension) values (1, 'MP3');
insert into library.fileheader_type (id, extension) values (2, 'FLAC');
insert into library.fileheader_type (id, extension) values (3, 'MP4');
insert into library.fileheader_type (id, extension) values (4, 'M4A');
insert into library.fileheader_type (id, extension) values (5, 'M4P');
insert into library.fileheader_type (id, extension) values (6, 'WMA');
insert into library.fileheader_type (id, extension) values (7, 'WAV');
insert into library.fileheader_type (id, extension) values (8, 'RA');
insert into library.fileheader_type (id, extension) values (9, 'RM');
insert into library.fileheader_type (id, extension) values (10, 'M4B');

create table library.directory (id serial primary key, parent_id integer references library.directory (id), path text not null, deleted boolean default false);
create table library.file (id serial primary key, directory_id integer references library.directory (id) not null, filename varchar(256) not null, modified timestamp not null, size integer not null, deleted boolean default false);
create table library.fileheader (file_id integer references library.file (id) not null, type_id smallint references library.fileheader_type (id) not null, bitrate smallint not null, vbr boolean not null, duration smallint not null);
create table library.filetag (file_id integer references library.file (id) not null, artist_id integer references music.artist (id) not null, album_artist_id integer references music.artist (id), composer_id integer references music.artist (id), album_id integer references music.album (id), track_id integer references music.track (id) not null, track_nr smallint, track_nrs smallint, disc_nr smallint, disc_nrs smallint, year smallint, tag_id integer references music.tag (id), coverart boolean not null);

create table library.directory_delete (path text);
create table library.directory_import (parent_path text, parent_id integer references library.directory (id), path text not null);

create table library.file_delete (path text not null, filename varchar(256) not null);
create table library.file_import (path text not null, directory_id integer references library.directory (id), filename varchar(256) not null, modified timestamp not null, size integer not null);
create table library.file_headertag_import (file_id integer references library.file (id), path text not null, filename varchar(256) not null, type_id smallint references library.fileheader_type (id), extension varchar(5) not null, bitrate smallint not null, vbr boolean not null, duration smallint not null, artist_id integer references music.artist (id), artist_name text not null, album_artist_id integer references music.artist (id), album_artist_name text, composer_id integer references music.artist (id), composer_name text, album_id integer references music.album (id), album_name text, track_id integer references music.track (id), track_name text not null, track_nr smallint, track_nrs smallint, disc_nr smallint, disc_nrs smallint, year smallint, tag_id integer references music.tag (id), tag_name text, coverart boolean not null, artistsort_name text, albumartistsort_name text);

create table library.artist (id serial primary key, artist_id integer references music.artist (id) not null);
create table library.album (id serial primary key, album_id integer references music.album (id), year smallint, coverartfile_id integer references library.file (id), embeddedcoverartfile_id integer references library.file (id));
create table library.track (id serial primary key, track_id integer references music.track (id), album_id integer references music.album (id) not null, file_id integer references library.file (id));

create table library.artistsort (artist_id integer references music.artist (id) not null, artistsort_id integer references music.artist (id) not null);

create table library.coverartfilename (filename varchar(256) not null, priority smallint not null);
insert into library.coverartfilename (filename, priority) values ('folder.jpg', 0);
insert into library.coverartfilename (filename, priority) values ('folder.jpeg', 1);
insert into library.coverartfilename (filename, priority) values ('folder.png', 2);
insert into library.coverartfilename (filename, priority) values ('cover.jpg', 3);
insert into library.coverartfilename (filename, priority) values ('cover.jpeg', 4);
insert into library.coverartfilename (filename, priority) values ('cover.png', 5);

create unique index directory_path_id on library.directory (path, id);
create unique index fileheader_fileid on library.fileheader (file_id);
create unique index filetag_fileid on library.fileheader (file_id);

create unique index artist_artistid on library.artist (artist_id);
create unique index album_albumid on library.album (album_id);
create unique index track_albumid_trackid_fileid on library.track (album_id, track_id, file_id);
