requirevars 'defaultDB' 'y';
attach database '%{defaultDB}' as defaultDB;

var 'formula' from select formula from defaultDB.localAnovatbl where no in ( select min(no) from defaultDB.localAnovatbl where sst is null);
var 'metadata' from select jdictgroup(code,enumerations)  from defaultdb.metadatatbl;
var 'xnames' from select group_concat(xname) as  xname from
            (select distinct xname from (select strsplitv(regexpr("\+|\:|\*|\-",'%{x}',"+") ,'delimiter:+') as xname) where xname!=0);

drop table if exists localinputtblflatCategoricalEncoding;
create temp table localinputtblflatCategoricalEncoding as
categoricalcoding encodingcategory:sumscoding metadata:%{metadata} select * from defaultDB.localinputtblflat;


var 'metadata' from select jgroup(code,enumerations,referencevalue) from defaultdb.metadatatbl;
var 'derivedcolumnsofmodel' from
select group_concat (modelcolnamesdummycodded) from (
select formulaparts, modelcolnamesdummycodded from (select strsplitv(regexpr("\+",'%{formula}',"+") ,'delimiter:+') as formulaparts),
                                                   (select modelcolnames,group_concat(modelcolnamesdummycodded) as modelcolnamesdummycodded
                                                    from (select modelvariables('%{formula}','%{metadata}'))group by modelcolnames)
                                              where formulaparts = modelcolnames);
select '%{derivedcolumnsofmodel}';


drop table if exists defaultDB.input_local_tbl_LR_Final;
create table defaultDB.input_local_tbl_LR_Final as
createderivedcolumns newSchema:%{derivedcolumnsofmodel},%{y} select * from localinputtblflatCategoricalEncoding;

--Result: (computation of gramian and statistics):
drop table if exists localgramianandstatistics;
create temp table localgramianandstatistics (tablename text,attr1 text,attr2 text,val real,reccount real,colname text,S1 real,N real);

insert into localgramianandstatistics
select "gramian" as tablename, attr1,attr2, val, reccount , null, null, null
from (gramianflat select * from defaultDB.input_local_tbl_LR_Final);

insert into localgramianandstatistics
select 'statistics' as tablename, null, null, null, null,colname,  S1,  N
from (statisticsflat select * from defaultDB.input_local_tbl_LR_Final);

select * from localgramianandstatistics;
