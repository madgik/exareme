requirevars 'defaultDB' ;
attach database '%{defaultDB}' as defaultDB;

var 'returnValue' from select case when (select ('?' in (select nextnode from defaultdb.globaltree))) = 1 then
'"CONTINUE"'
else
'"STOP"' end;

select %{returnValue};
