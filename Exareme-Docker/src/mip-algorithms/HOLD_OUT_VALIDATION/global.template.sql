requirevars 'defaultDB' 'input_global_tbl' 'classname' 'dbIdentifier';

var 'input_global_tbl' 'defaultDB.local_variablesdatatype_Existing';

attach database '%{defaultDB}' as defaultDB;

drop table if exists defaultDB.global_inputvariables; --contains the names of classname
create table defaultDB.global_inputvariables as
select 'classname' as variablename, '%{classname}' as val;

drop table if exists defaultDB.global_variablesdatatype_Existing;
create table defaultDB.global_variablesdatatype_Existing as
select * from %{input_global_tbl};

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
