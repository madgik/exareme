requirevars 'input_global_tbl' ;

select attr1,
       attr2,
       sum(val)/(sum(reccount)-1) as cov
from   %{input_global_tbl}
group by attr1, attr2;