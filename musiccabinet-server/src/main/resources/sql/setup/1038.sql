create table library.artisttoptag (artist_id integer references music.artist (id) not null, tag_id integer references music.tag (id) not null, tag_count smallint not null);

insert into library.artisttoptag (artist_id, tag_id, tag_count)
select ac.artist_id, tag_id, 100 * tag_count / artist_count from 
(select artist_id, count(artist_id) as artist_count from library.filetag group by artist_id order by artist_id) ac
inner join
(select artist_id, tag_id, count(tag_id) as tag_count from library.filetag where tag_id is not null group by artist_id, tag_id) tc
on ac.artist_id = tc.artist_id;

create unique index library_artisttoptag_tagid_artistid_tagcount on library.artisttoptag (tag_id, artist_id, tag_count);
