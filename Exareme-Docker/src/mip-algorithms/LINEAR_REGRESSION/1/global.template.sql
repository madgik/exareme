requirevars 'defaultDB' 'input_global_tbl' 'dataset' 'x' 'y' 'referencevalues';
attach database '%{defaultDB}' as defaultDB;

var 'referencevalues' from (select case when '%{referencevalues}'= '' then '[]' else '%{referencevalues}' end);

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



drop table if exists defaultDB.regressiontbls;
create table  defaultDB.regressiontbls (tablename text,formula text, code text, categorical int,enumerations text,referencevalue text);
insert into defaultDB.regressiontbls select "simplifiedformula", formula,  null, null ,null ,null from (select create_simplified_formulas('%{x}',4));
insert into defaultDB.regressiontbls select "metadatatbl" ,         null,  code, categorical, enumerations,referencevalue  from metadatatbl;

select * from defaultDB.regressiontbls;
