requirevars 'input_global_tbl' 'defaultDB' 'iterationNumber';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' ' defaultDB.local_confusionmatrix';

drop table if exists defaultDB.global_oneconfusionmatrix;
create table defaultDB.global_oneconfusionmatrix as
select  iterationNumber, actualclass, predictedclass, sum(val) as val
from %{input_global_tbl}
group by actualclass,predictedclass;

insert into defaultDB.global_confusionmatrix
select  iterationNumber, "confusion table", actualclass, predictedclass, null,null, val from defaultDB.global_oneconfusionmatrix;

--insert into defaultDB.global_confusionmatrix
--select %{iterationNumber}, "statistics", null,null,typestats,statscolname, statsval
--from (rconfusionmatrixtable select predictedclass,actualclass,val,noclasses
--                            from defaultDB.global_oneconfusionmatrix,
--							(select count(distinct predictedclass) as noclasses from defaultDB.global_oneconfusionmatrix) order by predictedclass);

--select * from defaultDB.global_oneconfusionmatrix;

select tabletojson(actualclass,predictedclass,val, "actualclass,predictedclass,val")  as componentresult
from defaultdb.global_oneconfusionmatrix;
