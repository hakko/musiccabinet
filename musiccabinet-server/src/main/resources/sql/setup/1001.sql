create table library.toptag (id serial primary key, tag_id integer references music.tag (id) not null); 
