class UnknownFunctionError(Exception):
    pass


class TransferError(Exception):
    pass


class AlgorithmError(Exception):
    def __getattr__(self, key):
        raise self


class PrivacyError(Exception):
    pass