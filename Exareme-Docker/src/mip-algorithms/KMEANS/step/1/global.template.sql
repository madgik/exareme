requirevars 'defaultDB' 'input_global_tbl' ;
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.partialclustercenters'; --DELETE

drop table if exists defaultDB.clustercenters_global;
create table defaultDB.clustercenters_global as select * from defaultDB.clustercentersnew_global;

var 'Sums' from select create_complex_query("clid,","sum(?_clS)/sum(clN) as ?_clval",",","",'%{columns}');
var 'schema2' from select create_complex_query("clid,","?_clval",",","",'%{columns}');
drop table if exists defaultDB.clustercentersnew_global;
create table defaultDB.clustercentersnew_global as
setschema '%{schema2}'
select %{Sums} from %{input_global_tbl}  group by clid;

select * from defaultDB.clustercentersnew_global;
