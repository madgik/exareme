requirevars 'defaultDB' ;
attach database '%{defaultDB}' as defaultDB;

-- update iterationsDB.iterations_condition_check_result_tbl set iterations_condition_check_result = (
-- 	select ('?' in (select nextnode from defaultdb.globaltree))
-- );
--
-- select "ok";


var 'returnValue' from select case when (select ('?' in (select nextnode from defaultdb.globaltree))) = 1 then
'"CONTINUE"'
else
'"STOP"' end;

select %{returnValue};
