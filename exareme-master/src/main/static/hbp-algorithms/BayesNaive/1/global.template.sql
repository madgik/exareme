requirevars 'input_global_tbl' ;

select  colname, val, classval, sum(quantity) as quantity
from %{input_global_tbl}
group by colname, val, classval;