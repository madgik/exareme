requirevars 'defaultDB' 'prv_output_global_tbl' 'x' 'y' 'bins';
attach database '%{defaultDB}' as defaultDB;

--var 'prv_output_global_tbl' 'defaultDB.metadatatbl';

var 'x_metadata' from select tabletojson(code,categorical,enumerations,minval,maxval,N,"code,categorical,enumerations,minval,maxval,N",0) from %{prv_output_global_tbl} where code =='%{x}';
var 'enumerations' from select enumerations from %{prv_output_global_tbl} where code =='%{y}';

drop table if exists defaultDB.partialhistogramresults;
create table  defaultDB.partialhistogramresults (grouping text, id int,val text, minval real, maxval real, num int);

var 'binsnew' from select case when "%{bins}"="" then 1 else "%{bins}" end;
insert into defaultDB.partialhistogramresults
select '' as grouping,id, val,minval ,maxval ,num from (histogram metadata:%{x_metadata} bins:%{binsnew} select %{x} from defaultDB.localinputtblflat)
where  "%{y}"= "";

var 'ynew' from select case when "%{y}"="" then "%{x}" else "%{y}" end;
var 'computehistogram' from select create_complex_query("",'
insert into defaultDB.partialhistogramresults
select "?" as grouping,id, val,minval ,maxval ,num
from (histogram metadata:%{x_metadata} bins:%{binsnew} select %{x} from defaultDB.localinputtblflat where %{ynew}="?")
where "%{y}"<>"";', "" , "" , "%{enumerations}");

%{computehistogram};



select * from defaultDB.partialhistogramresults;




--Check privacy due to minimum records in each bucket -TODO
