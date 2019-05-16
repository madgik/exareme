requirevars 'defaultDB' 'prv_output_global_tbl';
attach database '%{defaultDB}' as defaultDB;

--var 'prv_output_global_tbl' 'globalresult';
drop table if exists defaultDB.localAnovatbl;
create table defaultDB.localAnovatbl as select no ,formula , sst , ssregs , sse from %{prv_output_global_tbl} where tablename="globalAnovattbl";

drop table if exists defaultDB.metadatatbl;
create table defaultDB.metadatatbl as select code , enumerations from %{prv_output_global_tbl} where tablename="metadatatbl";

select "ok";
