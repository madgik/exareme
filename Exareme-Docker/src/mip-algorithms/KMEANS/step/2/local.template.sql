requirevars 'defaultDB' 'prv_output_global_tbl';
attach database '%{defaultDB}' as defaultDB;

--var 'prv_output_global_tbl' 'defaultDB.clustercentersnew_global'; --DELETE

drop table if exists defaultDB.clustercenters_local;
create table defaultDB.clustercenters_local as select * from %{prv_output_global_tbl};

select "ok";
