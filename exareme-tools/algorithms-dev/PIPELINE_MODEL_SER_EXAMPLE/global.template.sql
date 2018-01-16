requirevars 'prv_output_local_tbl';

select writebinary('/root/exareme/static/exa-view/results/model.ser', bin)
from  %{prv_output_local_tbl};

select jdict("Download Model from: ","http://localhost:9090/exa-view/results/model.ser");
