requirevars  'defaultDB' 'input_global_tbl';

select colname0, id0, minvalue0, maxvalue0, sum(num) as total
from  %{input_global_tbl}
group by colname0, id0, minvalue0, maxvalue0;

