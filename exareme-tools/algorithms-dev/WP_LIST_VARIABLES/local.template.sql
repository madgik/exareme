requirevars 'input_local_tbl' ;

select jdictgroup('variables', variable) as variables
from (
  select jgroup(variable, t) as variable
  from (
    select distinct __colname as variable, typeof(tonumber(__val)) as t
    from %{input_local_tbl}
    where __val is not null
    group by __colname
    )
);

