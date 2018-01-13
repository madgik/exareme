requirevars 'prv_output_local_tbl';

select counter+1 as counter from %{prv_output_local_tbl};

