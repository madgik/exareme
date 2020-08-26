requirevars 'defaultDB' 'input_global_tbl' 'dbIdentifier';
attach database '%{defaultDB}' as defaultDB;

drop table if exists defaultDB.globalmetadatatbl;
create table defaultDB.globalmetadatatbl as
select distinct code, sql_type,categorical from %{input_global_tbl};

select '{"result": [{ "type": "application/json", "dbIdentifier": ' || '%{dbIdentifier}' ||'}]}';
