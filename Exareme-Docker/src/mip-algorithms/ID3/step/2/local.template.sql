requirevars 'defaultDB' 'prv_output_global_tbl';
attach database '%{defaultDB}' as defaultDB;

--- Split initial dataset based on global_pathforsplittree
var 'filters' from select tabletojson(colname,colval, "colname,val",0) from %{prv_output_global_tbl};
drop table if exists defaultDB.localinputtblcurrent;
create table defaultDB.localinputtblcurrent as
filtertable filters:%{filters} select * from defaultDB.localinputtbl;

select 'ok';
