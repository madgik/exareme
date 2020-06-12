requirevars 'defaultDB' 'prv_output_global_tbl' 'y' 'x';
attach database '%{defaultDB}' as defaultDB;

var 'groupvar1' from select distinct groupval from %{prv_output_global_tbl} limit 1;
var 'groupvar2' from select distinct groupval from %{prv_output_global_tbl} limit 2 offset 1;

var 'localstats1' from select create_complex_query("","
insert into localstatistics2
select '?' as colname, '%{groupvar1}' as groupval,mean,std, Ntotal, sum((?-mean)*(?-mean)) as sse from
defaultDB.localinputtblflat,%{prv_output_global_tbl}
where colname = '?' and groupval ='%{groupvar1}' and %{x}='%{groupvar1}' and ? is not null and ? <>'NA' and ? <>'' ;" , "" , "" , '%{y}');

var 'localstats2' from select create_complex_query("","
insert into localstatistics2
select '?' as colname, '%{groupvar2}' as groupval,mean,std, Ntotal, sum((?-mean)*(?-mean)) as sse from
defaultDB.localinputtblflat,%{prv_output_global_tbl}
where colname = '?' and groupval ='%{groupvar2}' and %{x}='%{groupvar2}' and ? is not null and ? <>'NA' and ? <>'';" , "" , "" , '%{y}');

drop table if exists localstatistics2;
create temp table localstatistics2 (colname text, groupval text, mean real,std real, Ntotal int, sse real);
%{localstats1};
%{localstats2};

select * from localstatistics2;
