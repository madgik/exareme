requirevars 'defaultDB' 'input_global_tbl'  ;
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.localstatistics';

drop table if exists defaultDB.globalstatistics;
create table  defaultDB.globalstatistics as
select colname, groupval, S1total/Ntotal as mean, SQROOT( FARITH('/', '-', '*', Ntotal, S2total, '*', S1total, S1total, '*', Ntotal, '-', Ntotal, 1)) as std, Ntotal
from (select colname, groupval,sum(S1) as S1total, sum(S2) as S2total, sum(N) as Ntotal
from %{input_global_tbl}
group by colname,groupval);

select * from defaultDB.globalstatistics;



--
-- drop table if exists defaultDB.globalttestresult;
-- create table defaultDB.globalttestresult as
-- select * from (t_test testvalue:%{testvalue} effectsize:%{effectsize} ci:%{ci} meandiff:%{meandiff} hypothesis:%{hypothesis}
--                select * from  defaultDB.globalstatistics);
--
-- -- var 'resultschema' from select * from (getschema outputformat:1 select * from defaultDB.globalstatistics);
--
--
-- drop table if exists defaultDB.ttestresultvisual;
-- create table defaultDB.ttestresultvisual as
-- setschema 'result'
-- select * from (totabulardataresourceformat title:ONE_SAMPLE_T_TEST_TABLE types:text,real,int,real,real,real,real,real,real
--                select * from defaultDB.globalttestresult);
--
-- select * from defaultDB.ttestresultvisual;
