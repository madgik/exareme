requirevars 'input_global_tbl' 'column1';


create temporary table eavtbl as
select * from %{input_global_tbl};

select (__local_id) as no, '%{column1}' as attr, FARITH(total) as hosp_sum, total_sum
from  eavtbl, ( select FARITH(FSUM(total)) as total_sum from  eavtbl);

