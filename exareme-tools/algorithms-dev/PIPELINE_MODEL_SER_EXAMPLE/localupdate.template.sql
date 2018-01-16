requirevars 'prv_output_local_tbl';


select writebinary('model.ser.prev', bin) from  %{prv_output_local_tbl};

select execprogram(null, 'java', '-jar', 'Serialization.jar', 'bicycles.arff', '13-15', 'model.ser.prev');

select bin from (unindexed select bin, execprogram(null,'rm',c2)  from
(unindexed select c2, execprogram(null,'cat',c2) as bin from dirfiles(.) where c2 like "tree%ser"));
