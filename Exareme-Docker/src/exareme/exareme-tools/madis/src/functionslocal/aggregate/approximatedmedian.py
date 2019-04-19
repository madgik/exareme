from array import *


class approximatedmedian:
    registered = True  # Value to define db operator

    def __init__(self):
        self.n = 0
        self.totalnums = 0
        self.numberofcolumns = 5
        self.colname = []
        self.buckets = []
        self.minvalues = []
        self.maxvalues = []
        self.nums = []

    def step(self, *args):

        try:
            self.colname.append(args[0])
            self.buckets.append(int(args[1]))
            self.minvalues.append(float(args[2]))
            self.maxvalues.append(float(args[3]))
            self.nums.append(int(args[4]))
            self.totalnums += int(args[4])
            self.n += 1

        except (ValueError, TypeError):
            raise

    def final(self):

        # print self.nums
        # print self.totalnums / 2.0

        yield ('colname0', 'val', 'bucket', 'numsBeforeMedian', 'numsAfterMedian')
        # yield ('attr1', 'attr2', 'val', 'reccount')

        currentsum = 0
        for i in xrange(0, self.n):
            # print i,self.totalnums / 2.0,self.nums[i],currentsum
            currentsum += self.nums[i]
            if currentsum >= (self.totalnums / 2.0):
                break

        median = self.minvalues[i] + (currentsum - self.totalnums / 2.0) * (self.maxvalues[i] - self.minvalues[i]) / \
                 self.nums[i]
        # print (self.totalnums / 2.0), currentsum, currentsum -self.nums[i]
        numsBeforeMedian = (self.totalnums / 2.0) - (currentsum - self.nums[i])
        numsAfterMedian = currentsum - (self.totalnums / 2.0)
        yield self.colname[0], median, i, numsBeforeMedian, numsAfterMedian


if not ('.' in __name__):
    """
    This is needed to be able to test the function, put it at the end of every
    new function you create
    """
    import sys

    # from functions import *

    # testfunction()
    if __name__ == "__main__":
        reload(sys)
        sys.setdefaultencoding('utf-8')
        import doctest

        doctest.testmod()
