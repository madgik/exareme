requirevars 'defaultDB' 'prv_output_global_tbl' 'x' 'y' 'encodingparameter';
attach database '%{defaultDB}' as defaultDB;

--var 'prv_output_global_tbl' 'defaultDB.regressiontbls';

var 'formula' from select formula from %{prv_output_global_tbl} where tablename ='simplifiedformula';
var 'categoricalmetadata' from select jdictgroup(code,enumerations) from %{prv_output_global_tbl} where tablename ='metadatatbl' and categorical = 1;

drop table if exists defaultDB.localinputtblflatCategoricalEncoding;
create table defaultDB.localinputtblflatCategoricalEncoding as
categoricalcoding encodingcategory:%{encodingparameter} metadata:%{categoricalmetadata} select * from defaultDB.localinputtblflat;



var 'categoricalmetadata' from select jgroup(code,enumerations,referencevalue) from %{prv_output_global_tbl} where tablename ='metadatatbl'  and categorical = 1 ;

var 'derivedcolumnsofmodel' from
select group_concat (modelcolnamesdummycodded) from (
select formulaparts, modelcolnamesdummycodded from (select strsplitv(regexpr("\+",'%{formula}',"+") ,'delimiter:+') as formulaparts),
                                                   (select modelcolnames,group_concat(modelcolnamesdummycodded) as modelcolnamesdummycodded
                                                    from (select modelvariables('%{formula}','%{categoricalmetadata}')) group by modelcolnames)
                                              where formulaparts = modelcolnames);
select '%{derivedcolumnsofmodel}';

drop table if exists defaultDB.input_local_tbl_LR_Final;
create table defaultDB.input_local_tbl_LR_Final as
createderivedcolumns newSchema:%{derivedcolumnsofmodel},%{y} select * from defaultDB.localinputtblflatCategoricalEncoding;


--Result: (computation of gramian and statistics):
drop table if exists defaultDB.localgramianandstatistics;
create table defaultDB.localgramianandstatistics (tablename text,attr1 text,attr2 text,val real,reccount real,colname text,S1 real,N real);

insert into defaultDB.localgramianandstatistics
select "gramian" as tablename, attr1,attr2, val, reccount , null, null, null
from (gramianflat select * from defaultDB.input_local_tbl_LR_Final);

insert into defaultDB.localgramianandstatistics
select 'statistics' as tablename, null, null, null, null,colname,  S1,  N
from (statisticsflat select * from defaultDB.input_local_tbl_LR_Final);

select * from defaultDB.localgramianandstatistics;
