requirevars 'target_attributes' 'descriptive_attributes' 'input_local_DB' 'db_query';

attach database '%{input_local_DB}' as localDB;

drop table if exists targetstable;
create table targetstable as
select strsplitv('%{target_attributes}' ,'delimiter:,') as targetname;

drop table if exists columnstable;
create table columnstable as
select strsplitv('%{target_attributes},%{descriptive_attributes}' ,'delimiter:,') as xname;

create temp table localinputtbl_1 as
select rid,colname, val from (toeav %{db_query});

var 'target_vars' from
( select group_concat('"'||targetname||'"',', ') from targetstable);

delete from localinputtbl_1 where rid in (select distinct rid from localinputtbl_1 where colname in (%{target_vars}) and val is null);

--Check if descriptive_attributes is empty
var 'empty' from select case when (select '%{descriptive_attributes}')='' then 0 else 1 end;
emptyfield '%{empty}';
---------
--Check if target_attributes is empty
var 'empty' from select case when (select '%{target_attributes}')='' then 0 else 1 end;
emptyfield '%{empty}';
------------------
create table columnexist as setschema 'colname' select distinct(colname) from localinputtbl_1;
--Check if columns exist
var 'counts' from select count(distinct(colname)) from columnexist where colname in (select xname from columnstable);
var 'result' from select count(distinct(xname)) from columnstable;
var 'valExists' from select case when(select %{counts})=%{result} then 1 else 0 end;
vars '%{valExists}';
-------------

var 'select_vars' from
( select group_concat('"'||xname||'"',', ') as select_vars from columnstable);

var 'target_var_count' from select count(*) from (select strsplitv('%{target_attributes}' ,'delimiter:,') as xname);

create temp table data as select %{select_vars}  from (fromeav select * from localinputtbl_1);

----Check if number of patients are more than minimum records----
var 'minimumrecords' 10;
create temp table emptytable as select * from data limit 0;
var 'privacycheck' from select case when (select count(*) from data) < %{minimumrecords} then 0 else 1 end;
create temp table safeData as
select * from data where %{privacycheck}=1
union all
select * from emptytable where %{privacycheck}=0;
------

--select * from (output 'input.arff'
--               select "@relation hour-weka.filters.unsupervised.attribute.Remove-R1-2" union all
--                      select "" union all select "@attribute "||column||" numeric" from (
--coltypes select * from safeData) union all
--                             select "" union all select "@data" union all select * from (csvout select * from safeData));

arff_writer select  * from safeData;

select execprogram(null, 'java', '-jar', 'ISOUPRegressionTreeSerializer.jar', 'input.arff', '1-%{target_var_count}');

select execprogram(null, 'rm', 'input.arff');
select execprogram(null,'rm',c2) from dirfiles(.) where c2 like "rtree%pfa.action.json";
select execprogram(null,'rm',c2) from dirfiles(.) where c2 like "rtree%vis.js";

select bin from (unindexed select bin, execprogram(null,'rm',c2) from
 (unindexed select c2, execprogram(null,'cat',c2) as bin from dirfiles(.) where c2 like "rtree%ser"));
