requirevars 'defaultDB' 'input_global_tbl' 'x' ;
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.kmeanslocalresult';
--var  'x' "lefthippocampus,righthippocampus";

var 'a' from select create_complex_query("clid, ","?_clval as ?,","","clpoints as noofpoints",'%{x}');
drop table if exists defaultDB.kmeansglobalresult;
create table defaultDB.kmeansglobalresult as
select %{a}
from defaultDB.clustercentersnew_global,
     ( select clid1, sum(clpoints) as clpoints from %{input_global_tbl} group by clid1 )
where clid1 = clid;

var 'columntypes' from select strreplace(a) from (select create_complex_query("int,","real,","","int",'%{x}') as a);

var 'resulthighchartbubble' from select * from (highchartbubble title:KMEANS_OUTPUT select %{x}, noofpoints from defaultDB.kmeansglobalresult);
var 'resulthighchartscatter3d' from select * from (highchartscatter3d title:KMEANS_OUTPUT select %{x} from defaultDB.kmeansglobalresult);
var 'resultjson' from select tabletojson(clid,%{x},noofpoints, 'clid,%{x},clpoints', 1) from defaultDB.kmeansglobalresult;
var 'resulttable' from select * from (totabulardataresourceformat title:Kmeans types:%{columntypes}
                                      select clid as `cluster id`, %{x}, noofpoints as `number of points` from defaultDB.kmeansglobalresult);


select '{"result": ['||'%{resultjson}'||','||'%{resulthighchartbubble}'||','||'%{resulthighchartscatter3d}'||','||'%{resulttable}'||']}';
