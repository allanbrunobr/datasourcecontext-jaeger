if not exists (select * from sysobjects where name='customer' and xtype='U')
    create table customer
(
  id int IDENTITY(1,1) primary key ,
  name varchar(255) not null
);
