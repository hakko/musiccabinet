alter table library.filetag add column lyrics text;
alter table library.file_headertag_import add column lyrics text;

alter table library.filetag alter lyrics set storage extended;
