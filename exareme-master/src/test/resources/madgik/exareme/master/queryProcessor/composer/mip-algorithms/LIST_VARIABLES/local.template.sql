requirevars 'input_local_tbl' ;

select jdictgroup('variables', variable) as variables
from (
  select jgroup(variable, t) as variable
  from (
    select distinct colname as variable, typeof(tonumber(val)) as t
    from (toeav select * from %{input_local_tbl})
    where val is not null
    group by colname
    )
);
