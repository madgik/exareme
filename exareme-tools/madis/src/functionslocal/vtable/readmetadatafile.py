import setpath
import functions
import json
import re
import itertools
registered=True


class readmetadatafile (functions.vtable.vtbase.VT):
    def VTiter(self, *parsedArgs,**envars):
        largs, dictargs = self.full_parse(parsedArgs)

        if 'filename' not in dictargs:
            raise functions.OperatorError(__name__.rsplit('.')[-1],"No filename argument ")
        filename = str(dictargs['filename'])

        with open(filename) as data_file:
            metadata = json.load(data_file)

        GroupsTotal = []
        GroupsTotal.append(metadata['variables'][0])
        GroupsTotal.extend([metadata['groups'][0]['groups'][0]['variables'][i] for i in xrange(14)])
        GroupsTotal.extend([metadata['groups'][1]['variables'] [i] for i in xrange(3)])
        GroupsTotal.extend([metadata['groups'][2]['groups'][0]['variables'][i] for i in xrange(4)])
        GroupsTotal.extend([metadata['groups'][3]['variables'][i] for i in xrange(2)])
        GroupsTotal.extend([metadata['groups'][3]['groups'][0]['variables'][i] for i in xrange(7)])
        GroupsTotal.extend([metadata['groups'][3]['groups'][1]['variables'][i] for i in xrange(5)])
        Group3 = [metadata['groups'][3]['groups'][2]['groups'][i] for i in xrange(9)]
        no =[5,0,14,16,16,12,40,4,2]
        for i in xrange(9):
            if i!=1:
                GroupsTotal.extend([Group3[i]['variables'][j] for j in xrange(no[i])])
            else:
                GroupsTotal.extend([Group3[1]['groups'][0]['variables'][j] for j in xrange(10)])
                GroupsTotal.extend([Group3[1]['groups'][1]['variables'][j] for j in xrange(2)])
        GroupsTotal.extend([metadata['groups'][4]['variables'][i] for i in xrange(5)])
        GroupsTotal.extend([metadata['groups'][5]['groups'][0]['variables'][i] for i in xrange(2)])
        GroupsTotal.extend([metadata['groups'][5]['variables'][i] for i in xrange(2)])
        GroupsTotal.extend([metadata['groups'][6]['groups'][0]['variables'][i] for i in xrange(3)])
        GroupsTotal.extend([metadata['groups'][6]['variables'][i] for i in xrange(3)])

        yield (['code'],['sqltype'],['categorical'],['enumerations'],['minValue'],['maxValue'])

        for Group in GroupsTotal:
            try:
                code = str(Group['code'])
            except:
                code = None
            try:
                type = str(Group['type'])
            except:
                type = None
            try:
                sql_type = str(Group['sql_type'])
            except:
                sql_type = None
            try:
                methodology = str(Group['methodology'])
            except:
                methodology = None
            try:
                enumerations = ','.join([str(x['code']) for x in Group['enumerations']])
            except:
                enumerations = None
            try:
                minValue = int(Group['minValue'])
            except:
                minValue = None
            try:
                maxValue = int(Group['maxValue'])
            except:
                maxValue = None

            # print "Code",type,sql_type,"methodology",enumerations
            categorical ='No'
            if maxValue!=None and minValue!=None:
                if maxValue-minValue < 32: categorical = 'Yes'
            elif type == 'polynominal' or type=='binominal':
                categorical = 'Yes'

            if categorical == 'No': sqltype = type
            else:sqltype = sql_type

            if sqltype == None:
                try:
                    ret = int(enumerations[0])
                    sqltype = 'int'
                except:
                    sqltype = 'text'

            if categorical =='Yes' and enumerations is None:
                enum = [i for i in xrange(maxValue-minValue)]
                enumerations = ','.join([str(i) for i in xrange(maxValue-minValue)] )

            yield code,sqltype,categorical,enumerations,minValue,maxValue



def Source():
    return functions.vtable.vtbase.VTGenerator(readmetadatafile)


if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys
    import setpath
    from functions import *
    testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest
        doctest.tes