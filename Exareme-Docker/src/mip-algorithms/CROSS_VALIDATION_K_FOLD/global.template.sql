requirevars 'defaultDB' 'input_global_tbl' 'dbIdentifier';
attach database '%{defaultDB}' as defaultDB;

drop table if exists defaultDB.globalmetadatatbl;
create table defaultDB.globalmetadatatbl as
select distinct code, sql_type,categorical from %{input_global_tbl};

var 'jsonResult' from select '{ "type": "application/json", "data": ' || results ||'}' from
(select jdict('dbIdentifier', '%{dbIdentifier}') as results);

select '{"result": [{ "type": "application/json", "dbIdentifier": ' || '%{dbIdentifier}' ||'}]}';
