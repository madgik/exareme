requirevars 'defaultDB' 'input_local_tbl' 'variable' 'covariables' 'groupings';
attach database '%{defaultDB}' as defaultDB;

--------------------------------------------------------------------------------------------
--C. Compute gramian (LOCAL LAYER)
--drop table if exists partial_gramian;
--create table partial_gramian as
select gramian(rid,colname, cast (val as real)) from
(select * from defaultDB.input_local_tbl_LR_Final order by rid, colname);