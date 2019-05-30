requirevars 'defaultDB' 'input_global_tbl' 'centers' ;
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.partialclustercenters'; --DELETE
var 'centersisempty' from select case when (select '%{centers}')='' then 1 else 0 end;

var 'schema' from select create_complex_query("clid,","?_clval",",","",'%{columns}');
var 'Sums' from select create_complex_query("clid,","sum(?_clS)/sum(clN) as ?_clval",",","",'%{columns}');
var 'renamecolnamestoschema' from select create_complex_query("clid ,","? as ?_clval ",",","",'%{columns}');

drop table if exists defaultDB.clustercentersnew_global;
create table defaultDB.clustercentersnew_global (%{schema});

insert into defaultDB.clustercentersnew_global
select %{Sums} from %{input_global_tbl}  where  %{centersisempty} = 1 group by clid
union
select %{renamecolnamestoschema} from (select jsontotable('%{centers}','clid,%{columns}')) where %{centersisempty} = 0;

drop table if exists defaultDB.clustercenters_global;
create table defaultDB.clustercenters_global as select * from defaultDB.clustercentersnew_global;

select * from defaultDB.clustercentersnew_global;
