requirevars 'defaultDB' 'input_global_tbl';
attach database '%{defaultDB}' as defaultDB;
--var 'input_global_tbl' 'defaultDB.partialmetadatatbl';

drop table if exists defaultDB.metadatatbl;
create table defaultDB.metadatatbl (code text,categorical int, enumerations text,minval real, maxval real, N int);

insert into defaultDB.metadatatbl
select code, categorical, group_concat(vals) as enumerations, null,null, null
from (select code, categorical,vals
      from (select code, categorical,strsplitv(enumerations ,'delimiter:,') as vals
            from %{input_global_tbl} where categorical=1) group by code,vals  )
group by code;

insert into defaultDB.metadatatbl
select code, categorical, null as enumerations, min(minval) as minval, max(maxval) as maxval, sum(N) as N
from %{input_global_tbl} where categorical=0 group by code;


select * from defaultDB.metadatatbl;
