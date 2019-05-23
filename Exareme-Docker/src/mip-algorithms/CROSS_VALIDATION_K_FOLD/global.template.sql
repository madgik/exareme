requirevars 'defaultDB' 'input_global_tbl' 'classname' 'dbIdentifier';

--var 'input_global_tbl' 'defaultDB.defaultDB';

attach database '%{defaultDB}' as defaultDB;


-- drop table if exists defaultDB.global_inputvariables; --contains the names of classname
-- create table defaultDB.global_inputvariables as
-- select 'classname' as variablename, '%{classname}' as val;
-
-- drop table if exists defaultDB.metadatatbl;
-- create table defaultDB.metadatatbl as
-- select distinct code, sql_type, categorical, enumerations, min, max from %{input_global_tbl};

drop table if exists defaultDB.global_confusionmatrix;
create table defaultDB.global_confusionmatrix (
iterationNumber int,
typecolname text, -- confusion table, statistics,
actualclass text,
predictedclass text,
typestats text, --overall, by class , average
statscolname text,
val float);

select jdict('dbIdentifier', '%{dbIdentifier}') as results;
