create table music.taginfo (id serial primary key, tag_id integer references music.tag (id), summary text, content text);
create table music.taginfo_import (tag_name text not null, tag_id integer references music.tag (id), summary text, content text);
