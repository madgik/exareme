requirevars  'defaultDB' 'input_global_tbl' 'y';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl'  'defaultDB.partialhistogramresults';

drop table if exists defaultDB.histresult;
create table defaultDB.histresult as
select grouping,id,val, minval,maxval,sum(num) as totalsum
from %{input_global_tbl}
group by grouping,id;


-- Privacy Output Result
var 'minNumberOfData' 10;
drop table if exists defaultDB.privatehistresult;
create table  defaultDB.privatehistresult as
select grouping, id, val, minval, maxval, case when totalsum < %{minNumberOfData} then 0 else cast(totalsum as int) end as totalsum from defaultDB.histresult;

var 'enumerations' from select enumerations from defaultDB.metadatatbl where code =='%{y}';

highchartsbasiccolumn enumerations:%{enumerations} title:Histogram ytitle:Count select * from defaultDB.histresult;
