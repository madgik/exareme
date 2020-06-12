requirevars 'defaultDB' 'input_global_tbl' 'y' ;
attach database '%{defaultDB}' as defaultDB;

var 'x' '%{y}';

var 'a' from select create_complex_query("clid, ","?_clval as ?,","","clpoints as noofpoints",'%{x}');
drop table if exists kmeansglobalresult;
create temp table kmeansglobalresult as
select %{a}
from defaultDB.clustercentersnew_global,
     ( select clid1, sum(clpoints) as clpoints from %{input_global_tbl} group by clid1 )
where clid1 = clid;

var 'columntypes' from select strreplace(a) from (select create_complex_query("int,","real,","","int",'%{x}') as a);

var 'resulthighchartbubble' from select * from (highchartbubble title:KMEANS_OUTPUT select %{x}, noofpoints from kmeansglobalresult);
var 'resulthighchartscatter3d' from select * from (highchartscatter3d title:KMEANS_OUTPUT select %{x} from kmeansglobalresult);
var 'resultjson' from select tabletojson(clid,%{x},noofpoints, 'clid,%{x},clpoints', 1) from kmeansglobalresult;
var 'resulttable' from select * from (totabulardataresourceformat title:Kmeans types:%{columntypes}
                                      select clid as `cluster id`, %{x}, noofpoints as `number of points` from kmeansglobalresult);


select '{"result": ['||'%{resultjson}'||','||'%{resulthighchartbubble}'||','||'%{resulthighchartscatter3d}'||','||'%{resulttable}'||']}';
