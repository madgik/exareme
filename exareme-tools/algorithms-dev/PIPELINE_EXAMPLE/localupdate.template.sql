requirevars 'prv_output_local_tbl';


create temporary table local_temp as select (
(select jdictsplit(result, "res") as counter from %{prv_output_local_tbl}) + 1)  as counter;

select jdict('res', counter) as result
from local_temp;