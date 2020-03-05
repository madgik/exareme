requirevars 'defaultDB';
attach database '%{defaultDB}' as defaultDB;

update defaultDB.globaltree set nextnode = ""  where nextnode ="-" or nextnode is null;
update defaultDB.globaltree set leafval = ""  where leafval ="?"  or leafval = '';

drop table if exists defaultDB.id3resultl;
create table defaultDB.id3resultl ('result') ;

var 'pfaresult' from select * from ( totabulardataresourceformat title:ID3_TABLE types:int,text,text,int,text,text
select no  as `id`,colname as `colname`, colval as `edge`,nextnode as `childnodes` ,leafval  as `leafval`, samplesperclass as `samplesperclass` from defaultDB.globaltree);

var 'jsonresult' from select * from (treetojson select no,tabletojson(no,colname,colval,nextnode,leafval,samplesperclass,"id,colname,edge,childnodes,leafval,samplesperclass",0) from defaultDB.globaltree
group by no);

-- var 'wekaresult' from select tabletojson(no,result, "no,result") from
-- (formattreetotableoutput select no as id,jgroup(jpack(colname,colval,nextnode,leafval)) as nodeinfo
-- from defaultDB.globaltree group by no order by id);

insert into defaultDB.id3resultl
select '{"result": [ %{pfaresult}, {"type": "application/json", "data": %{jsonresult}} ]}';


select * from defaultDB.id3resultl;
