requirevars  'defaultDB' 'input_global_tbl';
-- attach database '%{defaultDB}' as defaultDB;


select colname0, id0, minvalue0, maxvalue0, colname1, val, sum(num) as total
from  %{input_global_tbl}
group by colname0, id0, minvalue0, maxvalue0, colname1, val;

