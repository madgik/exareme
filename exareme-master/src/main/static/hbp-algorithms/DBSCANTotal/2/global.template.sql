requirevars 'input_global_tbl' 'eps' 'k';

--drop table if exists dbscantable;
--create table dbscantable as

create temporary table heatmapglobal as
select colname0, minvalue0, maxvalue0, colname1, minvalue1, maxvalue1, sum(num) as total
from %{input_global_tbl}
group by colname0, minvalue0, maxvalue0, colname1, minvalue1, maxvalue1 ;
--
--
--drop table if exists dbscantable;
--create table dbscantable as
--
select dbscanfinal(colname0, minvalue0, maxvalue0, colname1, minvalue1, maxvalue1, total, %{eps}, %{k})
from heatmapglobal;