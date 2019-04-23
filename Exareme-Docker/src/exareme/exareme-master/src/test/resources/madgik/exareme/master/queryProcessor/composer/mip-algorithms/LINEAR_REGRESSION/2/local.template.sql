requirevars 'defaultDB' 'prv_output_global_tbl';
attach database '%{defaultDB}' as defaultDB;

drop table if exists defaultDB.globalstatistics;
create table defaultDB.globalstatistics as
select * from %{prv_output_global_tbl};

--C. Compute gramian (LOCAL LAYER)
select gramian(rid,colname, cast (val as real)) from
(select * from defaultDB.input_local_tbl_LR_Final order by rid, colname);
