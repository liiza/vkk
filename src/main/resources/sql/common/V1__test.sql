create table DEVICE (
    ID int not null,
    TIME timestamp not null,
    TYPE varchar(200) not null,
    VALUE int not null
);

insert into DEVICE (ID, TIME, TYPE, VALUE) values (1, '004-10-19 10:23:54', 'light detector', 2300);