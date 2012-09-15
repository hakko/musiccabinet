insert into library.webservice_calltype (id, description) values (10, 'Last.fm group.getWeeklyArtistChart');

create table music.lastfmgroup (id serial primary key, group_name text not null, group_name_capitalization text not null, enabled boolean default true not null);
create table music.lastfmgroup_import (group_name text not null);

alter table library.webservice_history add column lastfmgroup_id integer references music.lastfmgroup (id);

create table music.groupweeklyartistchart (lastfmgroup_id integer references music.lastfmgroup (id) not null, artist_id integer references music.artist (id) not null, playcount integer not null);
create table music.groupweeklyartistchart_import (lastfmgroup_id integer references music.lastfmgroup (id), lastfmgroup_name text not null, artist_id integer references music.artist (id), artist_name text not null, playcount integer not null);

