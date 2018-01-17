requirevars 'variables' 'input_local_tbl';

create temp table variables_tbl as select strsplitv('%{variables}','delimiter:,') as variable;

var 'select_vars' from
( select group_concat('"'||variable||'"',', ') as select_vars from variables_tbl);

var 'var_count' from select count(*) from variables_tbl;

create temp table data as select %{select_vars}, 0 as C1, 0 as C2, 0 as C3   from (fromeav select * from (%{input_local_tbl}));

select * from (output 'input.arff'
               select "@attribute relation hour-weka.filters.unsupervised.attribute.Remove-R1-2" union all
                      select "" union all select "@attribute "||column||" numeric" from (
coltypes select * from data) union all
                             select "" union all select "@data" union all select * from (csvout select * from data));


select execprogram(null, 'java', '-jar', 'Serialization.jar', 'input.arff', '1-%{var_count}');

select execprogram(null, 'rm', 'input.arff');

select bin from (unindexed select bin, execprogram(null,'rm',c2) from
 (unindexed select c2, execprogram(null,'cat',c2) as bin from dirfiles(.) where c2 like "tree%ser"));