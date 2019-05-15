requirevars 'input_local_DB' 'db_query';

attach database '%{input_local_DB}' as localDB;

select jdictgroup('variables', variable) as variables
from (
  select jgroup(variable, t) as variable
  from (
    select distinct colname as variable, typeof(tonumber(val)) as t
    from (toeav %{db_query})
    where val is not null
    group by colname
    )
);
