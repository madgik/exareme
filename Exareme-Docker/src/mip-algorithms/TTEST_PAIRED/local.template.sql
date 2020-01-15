-- ------------------Input for testing
-- ------------------------------------------------------------------------------
-- hidden var 'defaultDB' defaultDB_TTEST;
-- hidden var 'x' 'righthippocampus-lefthippocampus,leftaccumbensarea-rightaccumbensarea';
-- hidden var 'outputformat' 'pfa';
-- hidden var 'effectsize' 1;
-- hidden var 'ci'  0;
-- hidden var 'meandiff'  0;
-- hidden var 'hypothesis'  'different';
--
--
-- drop table if exists inputdata;
-- create table inputdata as
-- select %{xnames}
-- from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/unit_tests/datasets/CSVs/desd-synthdata.csv');
--
--
------------------ End input for testing
------------------------------------------------------------------------------

requirevars 'defaultDB' 'input_local_DB' 'db_query' 'y' 'hypothesis' 'effectsize' 'ci' 'meandiff' 'sediff';
--to x formula ths morfhs x1-x2

attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

var 'x' '%{y}';
-- ErrorHandling
select categoricalparameter_inputerrorchecking('hypothesis', '%{hypothesis}', 'different,greaterthan,lessthan');
select categoricalparameter_inputerrorchecking('effectsize', '%{effectsize}', '0,1');
select categoricalparameter_inputerrorchecking('ci', '%{ci}', '0,1');
select categoricalparameter_inputerrorchecking('meandiff', '%{meandiff}', '0,1');
select categoricalparameter_inputerrorchecking('sediff', '%{sediff}', '0,1');


var 'xnames' from
select group_concat(xname) as  xname from
(select distinct xname from (select strsplitv(regexpr("\-",'%{x}',"+") ,'delimiter:+') as xname) where xname!=0);

--Read dataset
drop table if exists inputdata;
create table inputdata as select * from (%{db_query});

-- Cast values of columns using cast function.
var 'cast_x' from select create_complex_query("","tonumber(?) as ?", "," , "" , '%{xnames}');
drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select %{cast_x} from inputdata;

--One Sample T-test
drop table if exists defaultDB.splittedpairs;
create table defaultDB.splittedpairs as
select group_concat(xname1) as  xname1, group_concat(xname2) as  xname2 from
(select strsplit(regexpr("\-",xname,"+") ,'delimiter:+') as xname from (
select strsplitv(regexpr("\,",'%{x}',"+") ,'delimiter:+') as xname));

var 'names1' from select xname1 from defaultDB.splittedpairs;
var 'names2' from select xname2 from defaultDB.splittedpairs;

var 'localstats' from select create_complex_query("","insert into  defaultDB.localstatistics
select '?-??' as colname, sum(?-??) as S1, sum((?-??)*(?-??)) as S2, count(?-??) as N from localinputtblflat
where ? is not null and ? <>'NA' and ? <>''
and ?? is not null and ?? <>'NA' and ?? <>'';" , "" , "" , '%{names1}','%{names2}');

drop table if exists defaultDB.localstatistics;
create table defaultDB.localstatistics (colname text, S1 real, S2 real, N int);
%{localstats};

drop table if exists defaultDB.privacychecking; -- For error handling
create table defaultDB.privacychecking as
select privacychecking(N) from defaultDB.localstatistics;

select * from defaultDB.localstatistics;
