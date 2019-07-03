requirevars 'defaultDB' 'iterations_max_number';
attach database '%{defaultDB}' as defaultDB;

var 'EH_IterationsMaxNumber' from  select maxnumberofiterations_errorhandling(0,no) from (select count(nextnode) as no from defaultdb.globaltree where nextnode='?');

update defaultDB.globaltree set nextnode = ""  where nextnode ="-";
update defaultDB.globaltree set leafval = ""  where leafval ="?";

drop table if exists defaultDB.id3resultl;
create table defaultDB.id3resultl ('result') ;

var 'pfaresult' from select * from ( totabulardataresourceformat title:ID3_TABLE types:int,text,text,int,text
select no  as `id`,colname as `colname`, colval as `edge`,nextnode as `childnodes` ,leafval  as `leafval` from defaultDB.globaltree);

var 'jsonresult' from select * from (treetojson select no,tabletojson(no,colname,colval,nextnode,leafval,"id,colname,edge,childnodes,leafval") from defaultDB.globaltree
group by no);
--where '%{outputformat}'= 'json';

var 'wekaresult' from select tabletojson(no,result, "no,result") from
(formattreetotableoutput select no as id ,jgroup(jpack(colname,colval,nextnode,leafval)) as nodeinfo
from defaultDB.globaltree group by no order by id);
--where '%{outputformat}'= 'wekaviewer';

insert into defaultDB.id3resultl
select '{"result": ['||'%{pfaresult}'||','||'{"type": "application/json", "data":' || '%{jsonresult}' ||'},'||'{"type": "application/wekaresult", "data":' || '%{wekaresult}' ||'}'||']}';


select * from defaultDB.id3resultl;