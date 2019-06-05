requirevars 'defaultDB' 'prv_output_global_tbl';
attach database '%{defaultDB}' as defaultDB;

-- var 'prv_output_global_tbl' 'defaultDB.globalstatistics';

var 'groupvar1' from select distinct groupval from defaultDB.globalstatistics limit 1;
var 'groupvar2' from select distinct groupval from defaultDB.globalstatistics limit 2 offset 1;

var 'localstats1' from select create_complex_query("","
insert into defaultDB.localstatistics2
select '?' as colname, '%{groupvar1}' as groupval,mean,std, Ntotal, sum((?-mean)*(?-mean)) as sse from
defaultDB.localinputtblflat,%{prv_output_global_tbl}
where colname = '?' and groupval ='%{groupvar1}' and %{y}='%{groupvar1}' and ? is not null and ? <>'NA' and ? <>'' ;" , "" , "" , '%{x}');

var 'localstats2' from select create_complex_query("","
insert into defaultDB.localstatistics2
select '?' as colname, '%{groupvar2}' as groupval,mean,std, Ntotal, sum((?-mean)*(?-mean)) as sse from
defaultDB.localinputtblflat,%{prv_output_global_tbl}
where colname = '?' and groupval ='%{groupvar2}' and %{y}='%{groupvar2}' and ? is not null and ? <>'NA' and ? <>'';" , "" , "" , '%{x}');

drop table if exists defaultDB.localstatistics2;
create table defaultDB.localstatistics2 (colname text, groupval text, mean real,std real, Ntotal int, sse real);
%{localstats1};
%{localstats2};

select * from defaultDB.localstatistics2;
