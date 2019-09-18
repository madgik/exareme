requirevars 'defaultDB' ;
attach database '%{defaultDB}' as defaultDB;

var 'returnValue' from select case when count(*)>0 then 
'"CONTINUE"' 
else 
'"STOP"' end from defaultDB.globalAnovatbl where sst is null;

select %{returnValue};