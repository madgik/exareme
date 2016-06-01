requirevars 'input_local_tbl' ;

select distinct __colname as variable, typeof(tonumber(__val)) as type
from %{input_local_tbl}
group by __colname;

