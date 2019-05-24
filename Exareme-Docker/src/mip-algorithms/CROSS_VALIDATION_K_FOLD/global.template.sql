requirevars 'defaultDB' 'input_global_tbl' 'dbIdentifier';

--var 'input_global_tbl' 'defaultDB.localmetadatatbl';

attach database '%{defaultDB}' as defaultDB;

drop table if exists defaultDB.globalmetadatatbl;
create table defaultDB.globalmetadatatbl as
select distinct code, categorical from %{input_global_tbl};

-- select code,categorical, group_concat(vals) as enumerations from
-- (select code, categorical,vals from (select code, categorical, strsplitv(enumerations ,'delimiter:,') as vals
-- from %{input_global_tbl}) group by code,vals) group by code;

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
