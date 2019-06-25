requirevars 'defaultDB' 'input_global_tbl' 'dataset' 'x' 'y' 'sstype' 'outputformat';
attach database '%{defaultDB}' as defaultDB;

var 'input_global_tbl' 'defaultDB.partialmetadatatbl' 'iterations_max_number';

drop table if exists defaultDB.algorithmparameters; --used for testing !!!
create table defaultDB.algorithmparameters (name,val);
insert into defaultDB.algorithmparameters select 'x' , '%{x}' ;
insert into defaultDB.algorithmparameters select 'y' , '%{y}' ;
insert into defaultDB.algorithmparameters select 'type' , '%{sstype}' ;
insert into defaultDB.algorithmparameters select 'outputformat' , '%{outputformat}' ;
insert into defaultDB.algorithmparameters select 'dataset' , '%{dataset}' ;

var 'referencevalues' '[]';
drop table if exists defaultDB.metadatatbl;
create table defaultDB.metadatatbl (code text, categorical int, enumerations text, referencevalue text);
insert into defaultDB.metadatatbl
select * from (
setschema 'code,categorical,enumerations,referencevalue'
select definereferencevalues(code, categorical,enumerations,'%{referencevalues}') from
(select code, categorical, group_concat(vals) as enumerations, null
                        from (select code, categorical,vals
                              from (select code, categorical,strsplitv(enumerations ,'delimiter:,') as vals
                                    from %{input_global_tbl} where categorical=1) group by code,vals  )
                        group by code));
insert into defaultDB.metadatatbl select distinct code, categorical,enumerations, null from  %{input_global_tbl} where categorical=0;

drop table if exists defaultDB.globalAnovatbl;
create table defaultDB.globalAnovatbl (no int,formula text, sst real, ssregs real, sse real);
insert into defaultDB.globalAnovatbl
select * from (select create_simplified_formulas('%{x}',%{sstype}), null ,null,  null) ;--where  formula!='intercept';

var 'EH_IterationsMaxNumber' from  select maxnumberofiterations_errorhandling(%{iterations_max_number},no) from (select count(*) as no from  defaultDB.globalAnovatbl);

drop table if exists globalresult;
create table globalresult (tablename text,no int,formula text, sst real, ssregs real, sse real,code text, categorical int, enumerations text, referencevalue text);
insert into globalresult select "globalAnovattbl" , no ,formula , sst , ssregs , sse , null,null,null,null  from defaultDB.globalAnovatbl;
insert into globalresult select "metadatatbl" , null ,null , null , null , null , code ,categorical, enumerations, referencevalue  from defaultDB.metadatatbl;

select * from globalresult;
