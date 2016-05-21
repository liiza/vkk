delete from EVENT;
alter table EVENT alter ID type serial;
alter table PLACE alter ID type serial;

insert into EVENT (TIME, TYPE, VALUE) values ('2004-10-19 10:23:54', 'light detector', 2300);