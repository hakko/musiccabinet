alter table library.lastfmuser set schema music;

alter table library.usertopartist set schema music;
alter table library.usertopartist_import set schema music;

alter table library.userrecommendedartist set schema music;
alter table library.userrecommendedartist_import set schema music;

drop function if exists library.update_usertopartists();
drop function if exists library.update_userrecartists();
drop function if exists library.get_lastfmuser_id(text);		