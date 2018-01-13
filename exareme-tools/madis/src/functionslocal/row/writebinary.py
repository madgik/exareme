
def writebinary(filename,arg):
    f = open(filename,"wb")
    f.write(arg)
    f.close()

writebinary.registered = True