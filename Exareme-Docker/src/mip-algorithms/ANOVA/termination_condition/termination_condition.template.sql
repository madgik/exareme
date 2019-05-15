requirevars 'defaultDB' ;
attach database '%{defaultDB}' as defaultDB;

update iterationsDB.iterations_condition_check_result_tbl set iterations_condition_check_result = (
	select count(*)>0 from defaultDB.globalAnovatbl where sst is null
);

select "ok";
