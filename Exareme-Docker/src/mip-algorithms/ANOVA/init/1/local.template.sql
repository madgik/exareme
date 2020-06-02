
requirevars 'defaultDB' 'input_local_DB' 'db_query' 'x' 'y' 'dataset' 'sstype';
attach database '%{defaultDB}' as defaultDB;
attach database '%{input_local_DB}' as localDB;

select categoricalparameter_inputerrorchecking('sstype', '%{sstype}', '1,2,3');

var 'xnames' from
select group_concat(xname) as  xname from
(select distinct xname from (select strsplitv(regexpr("\+|\:|\*|\-",'%{x}',"+") ,'delimiter:+') as xname) where xname!=0);

--Read dataset and Delete patients with null values (val is null or val = '' or val = 'NA'). Cast values of columns using cast function.
var 'nullCondition' from select create_complex_query(""," ? is not null and ? <>'NA' and ? <>'' ", "and" , "" , '%{xnames},%{y}');
var 'cast_xnames' from select create_complex_query("","cast(? as text) as ?", "," , "" , '%{xnames}');
drop table if exists defaultDB.localinputtblflat;
create table defaultDB.localinputtblflat as
select %{cast_xnames}, cast(%{y} as real) as '%{y}', cast(1.0 as real) as intercept
from (select * from (%{db_query})) where %{nullCondition};

var 'privacy' from select privacychecking(no) from (select count(*) as no from defaultDB.localinputtblflat);

drop table if exists partialmetadatatbl;
create temp table partialmetadatatbl (code text, categorical int, enumerations text);
var 'metadata' from select create_complex_query(""," insert into  partialmetadatatbl
                                                     select '?' as code, 1 as categorical, group_concat(vals) as enumerations
                                                     from (select distinct ? as vals from defaultDB.localinputtblflat);", "" , "" , '%{xnames}');

%{metadata};


select * from partialmetadatatbl;
