requirevars 'defaultDB' 'input_local_DB' 'db_query' 'y' 'hypothesis';
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

-- ErrorHandling
select categoricalparameter_inputerrorchecking('hypothesis', '%{hypothesis}', 'different,greaterthan,lessthan');

--Read dataset and Cast values of columns using cast function.
var 'cast_x' from select create_complex_query("","tonumber(?) as ?", "," , "" , '%{y}');
drop table if exists localinputtblflat;
create temp table localinputtblflat as
select %{cast_x} from (select * from (%{db_query}));

--One Sample T-test
var 'localstats' from select create_complex_query("","insert into  localstatistics
select '?' as colname, sum(?) as S1, sum(?*?) as S2, count(?) as N from localinputtblflat
where ? is not null and ? <>'NA' and ? <>'';" , "" , "" , '%{y}');
drop table if exists localstatistics;
create temp table localstatistics (colname text, S1 real, S2 real, N int);
%{localstats};


select privacychecking(N) from localstatistics;

select * from localstatistics;
