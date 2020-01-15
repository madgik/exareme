------------------Input for testing
------------------------------------------------------------------------------
-- hidden var 'defaultDB' defaultDB_TTEST;
-- hidden var 'x' 'lefthippocampus';
-- hidden var 'y' 'gender';
-- var 'ylevels' 'M,F';
-- hidden var 'outputformat' 'pfa';
-- hidden var 'effectsize' 1;
-- hidden var 'ci'  0;
-- hidden var 'meandiff'  0;
-- hidden var 'hypothesis' 'diferrent' ;
-- var 'input_local_DB' 'datasets.db';
--
-- drop table if exists inputdata;
-- create table inputdata as
-- select %{x},%{y}
-- from (file header:t '/home/eleni/Desktop/HBP/exareme/Exareme-Docker/src/mip-algorithms/unit_tests/datasets/CSVs/desd-synthdata.csv');
--
-- --http://www.sthda.com/english/wiki/t-test-formula


------------------ End input for testing
------------------------------------------------------------------------------

requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' 'xlevels' 'hypothesis' 'effectsize' 'ci' 'meandiff';
--x: a vector of strings naming the variables of interest in data
--testValue: a number specifying the value of the null hypothesis

attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;


-- ErrorHandling
select categoricalparameter_inputerrorchecking('hypothesis', '%{hypothesis}', 'different,greaterthan,lessthan');
select categoricalparameter_inputerrorchecking('effectsize', '%{effectsize}', '0,1');
select categoricalparameter_inputerrorchecking('ci', '%{ci}', '0,1');
select categoricalparameter_inputerrorchecking('meandiff', '%{meandiff}', '0,1');

--Read dataset
drop table if exists inputdata;
create table inputdata as select * from (%{db_query});

drop table if exists defaultDB.lala;
create table defaultDB.lala as select * from (%{db_query});


-- Cast values of columns using cast function.
var 'cast_y' from select create_complex_query("","tonumber(?) as ?", "," , "" , '%{y}');
drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select %{cast_y}, cast(%{x} as text) as '%{x}' from inputdata
where  %{x} is not null and %{x}  <>'NA' and %{x}  <>''
       and  %{x} in (select strsplitv('%{xlevels}','delimiter:,'));

--Independent or Unpaired T-test
var 'localstats' from select create_complex_query("","insert into  defaultDB.localstatistics
select '?' as colname, %{x} as groupval, sum(?) as S1, sum(?*?) as S2, count(?) as N from localinputtblflat
where ? is not null and ? <>'NA' and ? <>'' group by %{x};" , "" , "" , '%{y}');
drop table if exists defaultDB.localstatistics;
create table defaultDB.localstatistics (colname text, groupval text, S1 real, S2 real, N int);
%{localstats};

-- drop table if exists defaultDB.privacychecking; -- For error handling
-- create table defaultDB.privacychecking as
--ErrorChecking
select privacychecking(N) from (select count(*) as N from defaultDB.localinputtblflat);
select privacychecking(N) from defaultDB.localstatistics;


select variableshouldbebinary_inputerrorchecking('%{x}', val)
from (select count(distinct %{x}) as val from defaultDB.localinputtblflat)
where '%{xlevels}' <> '';
select variabledistinctvalues_inputerrorchecking('%{x}', val, '%{xlevels}')
from (select group_concat(distinct %{x}) as val from defaultDB.localinputtblflat)
where '%{xlevels}' <> '';


select * from defaultDB.localstatistics;

-- --Independent T-tests
-- var 'localstats' from select create_complex_query("","insert into  defaultDB.localstatistics
-- select '?' as colname, %{group_var} as group, sum(?) as S1, sum(?*?) as S2, count(?) as N from localinputtblflat
-- where ? is not null and ? <>'NA' and ? <>'' group by %{group_var};" , "" , "" , '%{localstats}');

--Paired T-test
-- var 'localstats' from select create_complex_query("","insert into  defaultDB.localstatistics
-- select strsplit('?','delimiter:-'), sum(?) as S1, sum(?*?) as S2, count(?) as N from localinputtblflat
-- where ? is not null and ? <>'NA' and ? <>'';" , "" , "" , '%{pairs}');
-- select C1 as colname1, C2 as colname2, sum(C1-C2) as S1, sum((C1-C2)*(C1-C2)) as S2, count(C1) from localinputtblflat, select strsplit('?','delimiter:-')
