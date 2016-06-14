requirevars 'defaultDB' 'input_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;

drop table if exists defaultDB.residuals;
create table defaultDB.residuals as
select * from %{input_global_tbl};





----C2. (GLOBAL LAYER)
--drop table if exists gramian2;
--create table gramian2 as
--select attr1,attr2, sum(val) as val, sum(reccount) as reccount
--from  defaultDB.partialgramian2
--group by attr1,attr2;
--
--
--
-------------------------------------------------------------------------------------------
---- Compute covariance matrix
--drop table if exists defaultDB.CovarianceMatrix;
--create table defaultDB.CovarianceMatrix as
--select attr1,attr2, val/(reccount-1) as covval
--from (select * from gramian2);
--
--insert into defaultDB.CovarianceMatrix
--select attr2 ,attr1, val/(reccount-1) as covval
--from (select * from gramian2)
--where attr1 != attr2;


select 'ok';
