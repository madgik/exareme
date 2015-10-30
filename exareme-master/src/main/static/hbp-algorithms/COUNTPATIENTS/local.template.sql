requirevars 'input_local_tbl' 'column1';
select FSUM(__val) as total
from %{input_local_tbl} where __colname in ('%{column1}');


