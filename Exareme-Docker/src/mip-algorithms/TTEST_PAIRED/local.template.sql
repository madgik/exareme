requirevars 'defaultDB' 'input_local_DB' 'db_query' 'y' 'hypothesis';
--to x formula ths morfhs x1-x2

attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

var 'x' '%{y}';
-- ErrorHandling
select categoricalparameter_inputerrorchecking('hypothesis', '%{hypothesis}', 'different,greaterthan,lessthan');

var 'xnames' from
select group_concat(xname) as  xname from
(select distinct xname from (select strsplitv(regexpr("\-",'%{x}',"+") ,'delimiter:+') as xname) where xname!=0);


--Check input: number of variables is modulo 2 
select pairedttest_inputerrorchecking(no) from
(select count(*) as no from
(select strsplitv(xname,'dialect:csv') from
(select group_concat(xname) as  xname from
(select distinct xname from (select strsplitv(regexpr("\-",'%{x}',"+") ,'delimiter:+') as xname) where xname!=0))));



--Read dataset and Cast values of columns using cast function.
var 'cast_x' from select create_complex_query("","tonumber(?) as ?", "," , "" , '%{xnames}');
drop table if exists localinputtblflat;
create temp table localinputtblflat as
select %{cast_x} from (select * from (%{db_query}));

--One Sample T-test
drop table if exists splittedpairs;
create temp table splittedpairs as
select group_concat(xname1) as  xname1, group_concat(xname2) as  xname2 from
(select strsplit(regexpr("\-",xname,"+") ,'delimiter:+') as xname from (
select strsplitv(regexpr("\,",'%{x}',"+") ,'delimiter:+') as xname));

var 'names1' from select xname1 from splittedpairs;
var 'names2' from select xname2 from splittedpairs;

var 'localstats' from select create_complex_query("","insert into  localstatistics
select '?-??' as colname, sum(?-??) as S1, sum((?-??)*(?-??)) as S2, count(?-??) as N from localinputtblflat
where ? is not null and ? <>'NA' and ? <>''
and ?? is not null and ?? <>'NA' and ?? <>'';" , "" , "" , '%{names1}','%{names2}');

drop table if exists localstatistics;
create temp table localstatistics (colname text, S1 real, S2 real, N int);
%{localstats};

select privacychecking(N) from localstatistics;

select * from localstatistics;
