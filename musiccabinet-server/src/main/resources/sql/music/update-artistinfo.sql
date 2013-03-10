create function music.update_artistinfo() returns int as $$
begin

	-- create missing artist(s)
	insert into music.artist (artist_name, artist_name_capitalization)
	select distinct on (upper(artist_name)) upper(artist_name), artist_name
	from music.artistinfo_import aii
		where not exists (select 1 from music.artist
			where artist_name = upper(aii.artist_name));

	-- update all import rows to correct artist id
	update music.artistinfo_import aii set artist_id = a.id
	from music.artist a
		where upper(aii.artist_name) = a.artist_name;

	-- update existing (changed) artist info
	update music.artistinfo ai set
		smallimageurl = aii.smallimageurl,
		mediumimageurl = aii.mediumimageurl,
		largeimageurl = aii.largeimageurl,
		extralargeimageurl = aii.extralargeimageurl,
		listeners = aii.listeners,
		playcount = aii.playcount,
		biosummary = aii.biosummary,
		biocontent = aii.biocontent
	from music.artistinfo_import aii
		where ai.artist_id = aii.artist_id;

	-- add new artist infos.
	insert into music.artistinfo (artist_id, smallimageurl, mediumimageurl, 
		largeimageurl, extralargeimageurl, listeners, playcount, biosummary,
		biocontent)
	select artist_id, smallimageurl, mediumimageurl, 
		largeimageurl, extralargeimageurl, listeners, playcount, biosummary,
		biocontent
	from music.artistinfo_import aii
		where not exists (select 1 from music.artistinfo
			where artist_id = aii.artist_id);

	return 0;

end;
$$ language plpgsql;