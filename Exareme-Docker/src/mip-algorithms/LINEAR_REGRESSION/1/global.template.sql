requirevars 'defaultDB' 'input_global_tbl' 'dataset' 'x' 'y' 'outputformat';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.partialmetadatatbl';

drop table if exists defaultDB.algorithmparameters; --used for testing !!!
create table defaultDB.algorithmparameters (name,val);
insert into defaultDB.algorithmparameters select 'x' , '%{x}' ;
insert into defaultDB.algorithmparameters select 'y' , '%{y}' ;
insert into defaultDB.algorithmparameters select 'outputformat' , '%{outputformat}' ;
insert into defaultDB.algorithmparameters select 'dataset' , '%{dataset}' ;

drop table if exists defaultDB.metadatatbl;
create table defaultDB.metadatatbl (code text, categorical int, enumerations text, referencevalue text);
insert into defaultDB.metadatatbl
select definereferencevalues(code, categorical,enumerations,'%{referencevalues}') from
(select code, categorical, group_concat(vals) as enumerations, null
                        from (select code, categorical,vals
                              from (select code, categorical,strsplitv(enumerations ,'delimiter:,') as vals
                                    from %{input_global_tbl} where categorical=1) group by code,vals  )
                        group by code);
insert into defaultDB.metadatatbl select distinct code, categorical,enumerations, null from  %{input_global_tbl} where categorical=0;



drop table if exists defaultDB.regressiontbls;
create table  defaultDB.regressiontbls (tablename text,formula text, code text, categorical int,enumerations text,referencevalue text);
insert into defaultDB.regressiontbls select "simplifiedformula", formula,  null, null ,null ,null from (select create_simplified_formulas('%{x}',4));
insert into defaultDB.regressiontbls select "metadatatbl" ,         null,  code, categorical, enumerations,referencevalue  from defaultDB.metadatatbl;

select * from defaultDB.regressiontbls;
