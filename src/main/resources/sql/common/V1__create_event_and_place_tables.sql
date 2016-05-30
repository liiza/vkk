create table PLACE (
    ID serial not null PRIMARY KEY,
    NAME varchar(200) not null,
    LATITUDE double precision not null,
    LONGITUDE double precision not null
);

create table EVENT (
    ID serial not null PRIMARY KEY,
    DEVICE_ID integer not null,
    PLACE_ID integer not null REFERENCES PLACE(ID),
    TIME timestamp,
    TYPE varchar(200) not null,
    VALUE real not null
);

