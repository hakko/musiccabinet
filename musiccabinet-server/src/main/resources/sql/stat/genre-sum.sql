-- for each genre that's available as a radio tag,
-- display a calculated value of how much relevant
-- music there is available in the library.

select t.id, tag_name, sum from music.tag t inner join (
select tag_id, sum(tag_count * (100-rank)) from music.artisttoptag att
inner join library.artisttoptrackplaycount attpc on att.artist_id = attpc.artist_id
where att.tag_id in (select tag_id from library.toptag) group by tag_id
) tagsum on t.id = tagsum.tag_id order by sum
