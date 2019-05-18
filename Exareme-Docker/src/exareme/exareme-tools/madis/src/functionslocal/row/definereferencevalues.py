import json

def definereferencevalues(*args):

    code = str(args[0])
    categorical = int(str(1))
    enumerations = str(args[2])
    referencevalues = json.loads(args[3])

    yield ("code", "categorical","enumerations","referencevalue")

    defined = 0
    if categorical ==1:
         for x in referencevalues:
             if code== x['name']:
                defined = 1
                yield (code, categorical,enumerations,x['val'])
         if defined ==0:
             enum = enumerations.split(',')
             enum.sort()
             yield (code, categorical,enumerations,enum[0])
    else :
         yield (code, categorical,enumerations,None)

definereferencevalues.registered = True