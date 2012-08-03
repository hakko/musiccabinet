alter table library.lastfmuser add column session_key varchar(32);

create unique index lastfmuser_user on library.lastfmuser (lastfm_user);
