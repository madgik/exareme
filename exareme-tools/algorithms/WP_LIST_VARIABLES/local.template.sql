requirevars 'input_local_tbl' ;

select distinct __colname as variable, typeof(__val) as type
from %{input_local_tbl};
