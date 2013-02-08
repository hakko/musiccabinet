alter table library.file_headertag_import alter artist_name drop not null;
alter table library.file_headertag_import alter track_name drop not null;

create table library.filewarning (file_id integer references library.file (id) not null);
