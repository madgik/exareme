# ------------------------- State ------------------------- #
# class State(object):
#
#     @one_kwarg
#     def save(self, **kwarg):
#         name, var = kwarg.popitem()
#         setattr(self, name, var)
#
#     def save_all(self, fname):
#         make_dirs(fname)
#         with open(fname, 'wb') as f:
#             try:
#                 pickle.dump(self, f, protocol=2)
#             except pickle.PicklingError:
#                 print('Cannot pickle object.')
#
#     @classmethod
#     def load_all(cls, fname):
#         with open(fname, 'rb') as f:
#             try:
#                 obj = pickle.load(f)
#             except pickle.UnpicklingError:
#                 print('Cannot unpickle.')
#                 raise
#         return obj
#