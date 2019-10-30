requirevars 'defaultDB' 'input_global_tbl' ;
--var 'input_global_tbl' 'defaultDB.localmetadatatbl';

attach database '%{defaultDB}' as defaultDB;

drop table if exists defaultDB.globalmetadatatbl;
create table defaultDB.globalmetadatatbl as
select distinct code, sql_type,categorical from %{input_global_tbl};

select * from defaultDB.globalmetadatatbl;
