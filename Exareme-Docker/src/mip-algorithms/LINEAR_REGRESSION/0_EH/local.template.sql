




--Check if x is empty
var 'empty' from select case when (select '%{x}')='' then 0 else 1 end;
emptyfield '%{empty}';
------------------
--Check if y is empty
var 'empty' from select case when (select '%{y}')='' then 0 else 1 end;
emptyfield '%{empty}';
------------------
--Check if dataset is empty
var 'empty' from select case when (select '%{dataset}')='' then 0 else 1 end;
emptyset '%{empty}';
------------------
create table columnexist as setschema 'colname' select distinct(colname) from (postgresraw);
--Check if x exist in dataset
var 'counts' from select count(distinct(colname)) from columnexist where colname in (select xname from xvariables);
var 'result' from select count(xname) from xvariables;
var 'valExists' from select case when(select %{counts})=%{result} then 1 else 0 end;
vars '%{valExists}';
--Check if y exist in dataset
var 'valExists' from select case when (select exists (select colname from columnexist where colname='%{y}'))=0 then 0 else 1 end;
vars '%{valExists}';




--y value:Real,Float or Integer.
--Some values could be null (type:Text). We want to make sure that if "rid-colname('%{y}')-val" exist in a node, colname type is not "Text". That is why
--we previously Delete patients with null values.
var 'type' from select case when (select distinct(typeof(tonumber(val))) as val from localinputtbl where colname='%{y}')='integer' or  (select distinct(typeof(tonumber(val))) as val from localinputtbl where colname = '%{y}')='real' or (select distinct(typeof(tonumber(val))) as val from localinputtbl where colname='%{y}')='float' then 1 else 0 end;
var 'empty' from select count(colname) from localinputtbl where colname='%{y}';          --is epmpty?...If it is, then 'type' is 0
var 'checkEpmpty' from select case when (select  %{empty})= 0 then 1 else 0 end;
var 'final' from select case when  (%{type}=0 and %{checkEpmpty}=1) or (%{type}=1 and %{checkEpmpty}=0) then 1 else 0 end;
vartypey '%{final}';

----Check if number of patients are more than minimum records----
var 'minimumrecords' 10;
create table emptytable(rid  text primary key, colname, val);
var 'privacycheck' from select case when (select count(distinct(rid)) from localinputtbl) < %{minimumrecords} then 0 else 1 end;
create table localinputtbl2 as setschema 'rid , colname, val'
select * from localinputtbl where %{privacycheck}=1
union
select * from emptytable where %{privacycheck}=0;
drop table if exists localinputtbl;
alter table localinputtbl2 rename to localinputtbl;
