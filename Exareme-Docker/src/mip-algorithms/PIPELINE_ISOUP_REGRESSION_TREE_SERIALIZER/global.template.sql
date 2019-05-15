requirevars 'prv_output_local_tbl' 'target_attributes' 'descriptive_attributes';

drop table if exists columnstable;
create table columnstable as
select strsplitv('%{target_attributes},%{descriptive_attributes}' ,'delimiter:,') as xname;

var 'select_vars' from
( select group_concat('"'||xname||'"',', ') as select_vars from columnstable);

var 'target_var_count' from select count(*) from (select strsplitv('%{target_attributes}' ,'delimiter:,') as xname);

create temp table data as select %{select_vars};

select * from (output 'input.arff'
               select "@relation hour-weka.filters.unsupervised.attribute.Remove-R1-2" union all
                      select "" union all select "@attribute "||column||" numeric" from (
coltypes select * from data) union all
                             select "" union all select "@data" );

select writebinary('rtree.ser.prev', bin) from  %{prv_output_local_tbl};

select execprogram(null, 'java', '-jar', 'ISOUPRegressionTreeSerializer.jar', 'input.arff', '1-%{target_var_count}', 'rtree.ser.prev');
select execprogram(null, 'rm', 'input.arff');
select execprogram(null, 'rm', c2) from dirfiles(.) where c2 like "rtree%prev";
select execprogram(null,'rm',c2) from dirfiles(.) where c2 like "rtree%pfa.action.json";
select execprogram(null,'rm',c2) from dirfiles(.) where c2 like "rtree%ser";

var 'js_filename' from select c2 from dirfiles(.) where c2 like "rtree%vis.js";

create temp table res_js as select group_concat(C1, " ") as res from (file '%{js_filename}');
select execprogram(null, 'rm', '%{js_filename}');

select res from res_js;
