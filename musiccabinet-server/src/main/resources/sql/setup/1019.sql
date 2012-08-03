create table library.artistindex (ascii_code integer not null);

insert into library.artistindex (ascii_code)
select distinct ascii(artist_name) from music.artist ma 
inner join library.artist la on la.artist_id = ma.id
where ascii(artist_name) <= 90;

insert into library.artistindex (ascii_code)
select ascii('#') from music.artist ma 
inner join library.artist la on la.artist_id = ma.id
where ascii(artist_name) > 90 limit 1;
