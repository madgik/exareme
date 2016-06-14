requirevars 'defaultDB' 'input_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;

drop table if exists defaultDB.residuals;
create table defaultDB.residuals as
select * from %{input_global_tbl};

select 'ok';
