requirevars 'defaultDB' 'input_global_tbl' 'dataset' 'x' 'y' 'type' 'outputformat';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.partialmetadatatbl';

drop table if exists defaultDB.algorithmparameters; --used for testing !!!
create table defaultDB.algorithmparameters (name,val);
insert into defaultDB.algorithmparameters select 'x' , '%{x}' ;
insert into defaultDB.algorithmparameters select 'y' , '%{y}' ;
insert into defaultDB.algorithmparameters select 'type' , '%{type}' ;
insert into defaultDB.algorithmparameters select 'outputformat' , '%{outputformat}' ;
insert into defaultDB.algorithmparameters select 'dataset' , '%{dataset}' ;


  drop table if exists defaultDB.metadatatbl;
  create table defaultDB.metadatatbl as
  select code, group_concat(vals) as enumerations from
  (select code, vals from (select code, strsplitv(enumerations ,'delimiter:,') as vals
  from %{input_global_tbl}) group by code,vals) group by code;

--
-- drop table if exists defaultDB.metadatatbl;
-- create table defaultDB.metadatatbl as
-- select mergepartialmetadata(code, enumerations,enumerationsDB) from
-- (select code, group_concat(vals) as enumerations , enumerationsDB from
-- ( select code, vals ,enumerationsDB  from (select code, strsplitv(enumerations ,'delimiter:,') as vals ,enumerationsDB
-- from %{input_global_tbl}) group by code,vals) group by code);


drop table if exists defaultDB.globalAnovatbl;
create table defaultDB.globalAnovatbl (no int,formula text, sst real, ssregs real, sse real);
insert into defaultDB.globalAnovatbl
select * from (select create_simplified_formulas('%{x}',%{type}), null ,null,  null) ;--where  formula!='intercept';


drop table if exists globalresult;
create table globalresult (tablename text,no int,formula text, sst real, ssregs real, sse real,code text, enumerations text);
insert into globalresult select "globalAnovattbl" , no ,formula , sst , ssregs , sse , null,null  from defaultDB.globalAnovatbl;
insert into globalresult select "metadatatbl" , null ,null , null , null , null , code , enumerations  from defaultDB.metadatatbl;

select * from globalresult;
