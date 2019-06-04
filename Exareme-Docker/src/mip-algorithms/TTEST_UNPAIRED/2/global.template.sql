requirevars 'defaultDB' 'input_global_tbl' 'hypothesis' 'effectsize' 'ci' 'meandiff';

var 'input_global_tbl' 'defaultDB.localstatistics2';
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;


drop table if exists defaultDB.globalstatistics2;
create table  defaultDB.globalstatistics2 as
select colname, groupval, mean, std,  Ntotal, sum(sse) as sse
from %{input_global_tbl}
group by colname, groupval;

drop table if exists defaultDB.globalttestresult;
create table defaultDB.globalttestresult as
select * from (t_test_unpaired colnames:%{x}  effectsize:%{effectsize} ci:%{ci} meandiff:%{meandiff} hypothesis:%{hypothesis}
               select colname, mean, std,  Ntotal,sse from  defaultDB.globalstatistics2);


drop table if exists defaultDB.ttestresultvisual;
create table defaultDB.ttestresultvisual as
setschema 'result'
select * from (totabulardataresourceformat title:UNPAIRED_TEST_TABLE types:text,real,int,real,real,real,real,real,real
              select * from defaultDB.globalttestresult);

select * from ttestresultvisual;
