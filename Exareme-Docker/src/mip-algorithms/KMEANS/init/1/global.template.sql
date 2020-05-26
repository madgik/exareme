requirevars 'defaultDB' 'input_global_tbl' 'centers' 'y' ;
attach database '%{defaultDB}' as defaultDB;

var 'x' '%{y}';

--var 'input_global_tbl' 'defaultDB.partialclustercenters'; --DELETE
var 'centersisempty' from select case when (select '%{centers}')='' or (select '%{centers}')='[]' or (select '%{centers}')='[{}]' or (select '%{centers}')='{}' then 1 else 0 end;
var 'centers2' from select case when %{centersisempty}==1 then '{}' else '%{centers}' end;

var 'schema' from select create_complex_query("clid,","?_clval",",","",'%{x}');
var 'Sums' from select create_complex_query("clid,","sum(?_clS)/sum(clN) as ?_clval",",","",'%{x}');
var 'renamecolnamestoschema' from select create_complex_query("clid ,","? as ?_clval ",",","",'%{x}');

drop table if exists defaultDB.clustercentersnew_global;
create table defaultDB.clustercentersnew_global (%{schema});

insert into defaultDB.clustercentersnew_global
select %{Sums} from %{input_global_tbl}  where  %{centersisempty} = 1 group by clid
union
select %{renamecolnamestoschema} from (select jsontotable('%{centers2}','clid,%{x}')) where %{centersisempty} = 0;

drop table if exists defaultDB.clustercenters_global;
create table defaultDB.clustercenters_global as select * from defaultDB.clustercentersnew_global;


drop table if exists defaultDB.iterations;
create table defaultDB.iterations (val);
insert into defaultDB.iterations select 1;


select * from defaultDB.clustercentersnew_global;
