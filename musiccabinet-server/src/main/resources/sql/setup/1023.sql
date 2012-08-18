alter table music.tag add column corrected_id integer references music.tag (id);

create index tag_correctedid on music.tag (coalesce(corrected_id, id));
