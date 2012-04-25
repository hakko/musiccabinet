-- Util schema: meta functionality for database
create schema util;
create table util.musiccabinet_version (update_id integer not null, insert_time timestamp not null default now());


-- Music schema: abstract information on artist, tracks, (global) ratings and relations
create schema music;
create table music.tag (id serial primary key, tag_name text not null);

create table music.artist (id serial primary key, artist_name text);
create table music.track (artist_id integer references music.artist (id), id serial primary key, track_name text);

create table music.trackrelation (source_id integer references music.track (id) not null, target_id integer references music.track (id) not null, weight float not null);
create table music.trackrelation_import (source_id integer not null, target_id integer references music.track (id), target_artist_name text not null, target_track_name text not null, target_artist_id integer references music.artist (id), weight float not null);

create table music.artistrelation (source_id integer references music.artist (id) not null, target_id integer references music.artist (id) not null, weight float not null);
create table music.artistrelation_import (source_id integer not null, target_id integer references music.artist (id), target_artist_name text not null, weight float not null);

create table music.artisttoptrack (artist_id integer references music.artist (id) not null, track_id integer references music.track (id) not null, rank smallint not null);
create table music.artisttoptrack_import (artist_id integer references music.artist (id) not null, track_id integer references music.track (id), track_name text not null, rank smallint not null);

create table music.artisttoptag (artist_id integer references music.artist (id) not null, tag_id integer references music.tag (id) not null, tag_count smallint not null);
create table music.artisttoptag_import (artist_id integer references music.artist (id) not null, tag_id integer references music.tag (id), tag_name text not null, tag_count smallint not null);

create unique index artistrelation_sourceid_targetid on music.artistrelation (source_id, target_id);

create unique index tag_tagname_id on music.tag (tag_name, id);
create unique index artisttoptag_tagid_artistid_tagcount on music.artisttoptag (tag_id, artist_id, tag_count);


-- Library schema: personal information, such as local music files, personal ratings, invoked web service calls
create schema library;
create table library.musicfile (track_id integer references music.track (id) not null, id serial primary key, path text, created timestamp, last_modified timestamp, external_id text);
create table library.musicfile_import (artist_name text, artist_id integer references music.artist (id), track_name text, track_id integer references music.track (id), path text, created timestamp, last_modified timestamp, external_id text);
create table library.musicdirectory (artist_id integer references music.artist (id) not null, id serial primary key, path text, root boolean);
create table library.musicdirectory_import (artist_id integer references music.artist (id), artist_name text not null, path text, root boolean);

create table library.trackplaycount (track_id integer references music.track (id) not null, play_count integer);
create table library.trackplaycount_import (artist_name text, artist_id integer references music.artist (id), track_name text, track_id integer references music.track (id), play_count integer);

create table library.webservice_calltype (id smallint primary key, description text);
create table library.webservice_history (artist_id integer references music.artist (id), track_id integer references music.track (id), calltype_id smallint references library.webservice_calltype (id), page smallint, invocation_time timestamp default now());

create table library.artisttoptrackplaycount (music_file_id integer references library.musicfile (id) not null, artist_id integer not null references music.artist (id), rank smallint not null, play_count integer not null);

insert into library.webservice_calltype (id, description) values (0, 'Last.fm library.getTracks (used for track playcount)');
insert into library.webservice_calltype (id, description) values (1, 'Last.fm artist.getSimilar');
insert into library.webservice_calltype (id, description) values (2, 'Last.fm artist.getTopTracks');
insert into library.webservice_calltype (id, description) values (3, 'Last.fm track.getSimilar');
insert into library.webservice_calltype (id, description) values (4, 'Last.fm artist.getTopTags');

create unique index musicfile_path_trackid on library.musicfile (path, track_id);
create unique index musicdirectory_path_artistid on library.musicdirectory (path, artist_id);
