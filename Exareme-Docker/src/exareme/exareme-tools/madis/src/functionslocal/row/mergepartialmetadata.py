

def mergepartialmetadata(*args):

    code = str(args[0])
    enumerations = str(args[1]).split(',')
    enumerationsDB = str(args[2]).split(', ')
    Exist =[0]*len(enumerationsDB)
    for i in xrange(len(enumerations)):
        for j in xrange(len(enumerationsDB)):
            if enumerations[i] == enumerationsDB[j]:
                Exist[j]=1
    # print Exist
    Result = ""
    for i in xrange(len(Exist)):
        if Exist[i]==True:
            Result+= enumerationsDB[i] +","
    yield ("code", "enumerations")
    # print Result
    yield (code, Result[:-1])

mergepartialmetadata.registered = True