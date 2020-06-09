requirevars 'defaultDB';
attach database '%{defaultDB}' as defaultDB;

update defaultDB.globaltree set nextnode = ""  where nextnode ="-" or nextnode is null;
update defaultDB.globaltree set leafval = ""  where leafval ="?"  or leafval = '';

drop table if exists id3resultl;
create temp table id3resultl ('result') ;

var 'pfaresult' from select * from ( totabulardataresourceformat title:ID3_TABLE types:int,text,text,int,text,text
select no  as `id`,colname as `colname`, colval as `edge`,nextnode as `childnodes` ,leafval  as `leafval`, samplesperclass as `samplesperclass` from defaultDB.globaltree);

var 'jsonresult' from select * from (treetojson select no,tabletojson(no,colname,colval,nextnode,leafval,samplesperclass,"id,colname,edge,childnodes,leafval,samplesperclass",0) from defaultDB.globaltree
group by no);

insert into id3resultl
select '{"result": [ %{pfaresult}, {"type": "application/json", "data": %{jsonresult}} ]}';

select * from id3resultl;
