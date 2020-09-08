requirevars 'input_local_tbl';

select execprogram(null, "/root/exareme/set-local-datasets.sh");

var 'a' from select count(distinct(rid)) as sum1 from (select distinct rid from (toeav select * from %{input_local_tbl}));

var 'b' from select execprogram(null,'cat','/root/exareme/etc/exareme/name');
select var('a') as sum1, val as val, var('b') as who from (select distinct val from (toeav select * from %{input_local_tbl}) where colname = 'dataset') group by val;
