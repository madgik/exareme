requirevars 'prv_output_local_tbl';

select jdict("res", count(*)) from %{prv_output_local_tbl};