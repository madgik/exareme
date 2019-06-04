requirevars 'defaultDB' 'input_global_tbl' 'testvalue' 'hypothesis' 'effectsize' 'ci' 'meandiff';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.localstatistics';

drop table if exists defaultDB.globalstatistics;
create table  defaultDB.globalstatistics as
select colname, S1total/Ntotal as mean, SQROOT( FARITH('/', '-', '*', Ntotal, S2total, '*', S1total, S1total, '*', Ntotal, '-', Ntotal, 1)) as std, Ntotal
from (select colname, sum(S1) as S1total, sum(S2) as S2total, sum(N) as Ntotal
from %{input_global_tbl}
group by colname);

drop table if exists defaultDB.globalttestresult;
create table defaultDB.globalttestresult as
select * from (t_test testvalue:%{testvalue} effectsize:%{effectsize} ci:%{ci} meandiff:%{meandiff} hypothesis:%{hypothesis}
               select * from  defaultDB.globalstatistics);

-- var 'resultschema' from select * from (getschema outputformat:1 select * from defaultDB.globalstatistics);


drop table if exists defaultDB.ttestresultvisual;
create table defaultDB.ttestresultvisual as
setschema 'result'
select * from (totabulardataresourceformat title:ONE_SAMPLE_T_TEST_TABLE types:text,real,int,real,real,real,real,real,real
               select * from defaultDB.globalttestresult);

select * from defaultDB.ttestresultvisual;
--
-- --Independent T-tests
-- select colnameA, (meanA-meanB) / sqroot(std*std/nA +std*std/nB)
--
--
-- select colnameA,
-- (select colname as colnameA, mean as meanA, std as stdA, n as nA from defaultDB.globalstatistics where group ='F')
-- (select colname as colnameB, mean as meanB, std as stdB, n as nB from defaultDB.globalstatistics where group ='M')
