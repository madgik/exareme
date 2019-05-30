requirevars 'defaultDB' 'input_global_tbl' 'columns' 'outputformat';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.kmeanslocalresult';

var 'a' from select create_complex_query("clid, ","?_clval as ?,","","clpoints as noofpoints",'%{columns}');
drop table if exists defaultDB.kmeansglobalresult;
create table defaultDB.kmeansglobalresult as
select %{a}
from defaultDB.clustercentersnew_global,
     ( select clid1, sum(clpoints) as clpoints from %{input_global_tbl} group by clid1 )
where clid1 = clid;

var 'columntypes' from select strreplace(a) from (select create_complex_query("int,","real,","","int",'%{columns}') as a);

drop table if exists defaultDB.kmeansvisualization;
create table defaultDB.kmeansvisualization ('result' text);

insert into defaultDB.kmeansvisualization
select * from (highchartbubble select %{columns}, noofpoints  from defaultDB.kmeansglobalresult where '%{outputformat}'= 'highchart_bubble')
where '%{outputformat}'= 'highchart_bubble'
union
select * from (highchartscatter3d select %{columns}, noofpoints from defaultDB.kmeansglobalresult where '%{outputformat}'= 'highchart_bubble')
where '%{outputformat}'= 'highchart_scatter3d'
union
select * from (totabulardataresourceformat title:KMEANS_TABLE types:%{columntypes} select clid as `cluster id`, %{columns}, noofpoints as `number of points`
from defaultDB.kmeansglobalresult) where '%{outputformat}'= 'pfa';


select * from defaultDB.kmeansvisualization;
