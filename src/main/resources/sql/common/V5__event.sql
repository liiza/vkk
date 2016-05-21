delete from EVENT;
alter table EVENT add DEVICE_ID integer not null;
alter table EVENT add PLACE_ID integer not null;
alter table EVENT alter TIME timestamp;