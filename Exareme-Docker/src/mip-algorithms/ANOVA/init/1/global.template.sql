requirevars 'defaultDB' 'input_global_tbl' 'dataset' 'x' 'y' 'sstype';
attach database '%{defaultDB}' as defaultDB;

var 'referencevalues' '[]';
drop table if exists metadatatbl;
create temp table metadatatbl (code text, categorical int, enumerations text, referencevalue text);
insert into metadatatbl
select * from (
setschema 'code,categorical,enumerations,referencevalue'
select definereferencevalues(code, categorical,enumerations,'%{referencevalues}') from
(select code, categorical, group_concat(vals) as enumerations, null
                        from (select code, categorical,vals
                              from (select code, categorical,strsplitv(enumerations ,'delimiter:,') as vals
                                    from %{input_global_tbl} where categorical=1) group by code,vals  )
                        group by code));
insert into metadatatbl select distinct code, categorical,enumerations, null from  %{input_global_tbl} where categorical=0;

drop table if exists defaultDB.globalAnovatbl;
create table defaultDB.globalAnovatbl (no int,formula text, sst real, ssregs real, sse real);
insert into defaultDB.globalAnovatbl
select * from (select create_simplified_formulas('%{x}',%{sstype}), null ,null,  null) ;--where  formula!='intercept';

drop table if exists globalresult;
create temp table globalresult (tablename text,no int,formula text, sst real, ssregs real, sse real,code text, categorical int, enumerations text, referencevalue text);
insert into globalresult select "globalAnovattbl" , no ,formula , sst , ssregs , sse , null,null,null,null  from defaultDB.globalAnovatbl;
insert into globalresult select "metadatatbl" , null ,null , null , null , null , code ,categorical, enumerations, referencevalue  from metadatatbl;

select * from globalresult;
