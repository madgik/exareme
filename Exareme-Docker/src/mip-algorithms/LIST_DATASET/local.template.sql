requirevars 'input_local_DB' 'db_query';

attach database '%{input_local_DB}' as localDB;

select execprogram(null, "/root/exareme/set-local-datasets.sh");

var 'a' from select count(distinct(rid)) as sum1 from (select distinct rid from (toeav %{db_query}));

var 'b' from select execprogram(null,'cat','/root/exareme/etc/exareme/name');
select var('a') as sum1, val as val, var('b') as who from (select distinct val from (toeav %{db_query}) where colname = 'dataset') group by val;
