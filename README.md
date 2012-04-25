MusicCabinet
============

Create automatic playlists.
---------------------------

MusicCabinet helps you create good playlists, based on your local music library.

* pick one or more genres (pop, 90s, piano...), and get a playlist of popular songs by artists matching these genres.
* pick an artist, and get a playlist of popular songs by related artists.
* pick an artist, and get a playlist of popular songs by that artist.

Browse by theme.
----------------

MusicCabinet also helps you browse your library, not by artist name, but rather by musical themes.

* pick a genre, and display your top artists that fits best in that category.
* pick an artist, and display your other artists that are most closely related.
* browsing is enhanced using artist image, artist biographies, genre descriptions, and album cover art.

How does it work?
-----------------

* you run a PostgreSQL database on your own machine.
* the database stores information about your local music library (artist, albums, tracks).
* meta-data, such as related artists, top tracks, genres and image urls, is fetched from last.fm and stored.
* methods to help you browse your library or generate playlists are exposed through an API.
* MusicCabinet is a Java library, meant to be bundled with other applications.

Released under the GPL license. To get in touch, drop an email at musiccabinet@dilerium.se.
