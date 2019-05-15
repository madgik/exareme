requirevars 'defaultDB' 'prv_output_global_tbl' 'classname';
attach database '%{defaultDB}' as defaultDB;

--var 'prv_output_global_tbl' 'globalpathforsplittree';

--- Split initial dataset based on global_pathforsplittree
var 'filters' from select tabletojson(colname,colval, "colname,val") from %{prv_output_global_tbl};
drop table if exists defaultDB.localinputtblcurrent;
create table defaultDB.localinputtblcurrent as
filtertable filters:%{filters} select * from defaultDB.localinputtbl;

select 'ok';
