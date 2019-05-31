requirevars 'defaultDB' 'input_global_tbl' 'classname' 'dbIdentifier';

--var 'input_global_tbl' 'defaultDB.localmetadatatbl';

attach database '%{defaultDB}' as defaultDB;

drop table if exists defaultDB.globalmetadatatbl;
create table defaultDB.globalmetadatatbl as
select distinct code, categorical from %{input_global_tbl};

-- drop table if exists defaultDB.global_confusionmatrix;
-- create table defaultDB.global_confusionmatrix (
-- iterationNumber int,
-- typecolname text, -- confusion table, statistics,
-- actualclass text,
-- predictedclass text,
-- typestats text, --overall, by class , average
-- statscolname text,
-- val float);


select jdict('dbIdentifier', '%{dbIdentifier}') as results;
