import codecs
import os
import pickle
import sqlite3
import types
import importlib

import numpy as np
from sqlalchemy import create_engine, MetaData, Table, select, func


def transfer_all(self):
    conn = sqlite3.connect('dbs/local_transfer.db')
    c = conn.cursor()
    c.execute('INSERT INTO transfer VALUES (?)', (codecs.encode(pickle.dumps(self), 'ascii'),))
    conn.commit()
    conn.close()


def __init__(self, cli_args):
    super(self.__class__, self).__init__(cli_args)
    self._transfer_struct.transfer_all = types.MethodType(transfer_all, self._transfer_struct)


def create_runner(for_class, of_type, found_in, num_workers, algorithm_args):
    folder = os.path.split(found_in)
    folder = folder[0] + '.' + folder[1]
    mod = importlib.import_module(folder)
    alg_cls = getattr(mod, for_class)

    # Create Worker and Master classes
    Worker = type('Worker', (alg_cls,), {'__init__': __init__})
    Master = type('Master', (alg_cls,), {'__init__': __init__})

    class Runner(object):
        def __init__(self, num_wrk, split=False):
            # Split db
            if split:
                split_db(num_wrk)
            # Get cli arguments
            local_argv = []
            for i in range(num_wrk):
                local_argv.append(get_cli_args(algorithm_args, node='worker', num=i))
            global_argv = get_cli_args(algorithm_args, node='master')

            # Create workers and master
            self.num_wrk = num_wrk
            self.workers = []
            for i in range(num_wrk):
                self.workers.append(Worker(local_argv[i]))
            self.master = Master(global_argv)

        def run(self):
            raise NotImplementedError

    if of_type == 'local-global':
        class LocalGlobalRunner(Runner):
            def run(self):
                self.make_transfer_db(path='dbs/local_transfer.db')
                for i in range(self.num_wrk):
                    self.workers[i].local_()
                self.master.global_()

            def make_transfer_db(self, path):
                conn = sqlite3.connect(path)
                c = conn.cursor()
                c.execute('DROP TABLE IF EXISTS transfer')
                c.execute('CREATE TABLE transfer (data text)')

        return LocalGlobalRunner(num_wrk=num_workers)


def split_db(num_wrk):
    # Connect to original DB
    engine = create_engine('sqlite:///dbs/datasets.db', echo=False)
    sqla_md = MetaData(engine)
    data = Table('data', sqla_md, autoload=True)
    metadata = Table('metadata', sqla_md, autoload=True)
    # Create node DBs
    worker_engines = []
    for i in range(num_wrk):
        worker_engines.append(create_engine('sqlite:///dbs/local_dataset{}.db'.format(i), echo=False))
        sqla_md.create_all(worker_engines[i])
    # Split data into node DBs
    select_count = select([func.count()]).select_from(data)
    num_rows = engine.execute(select_count).fetchone()[0]
    ri = iter(np.random.randint(num_wrk, size=num_rows))
    select_all = select([data])
    all_data = engine.execute(select_all)
    for row in all_data:
        ins_row = data.insert().values(row)
        worker_engines[next(ri)].execute(ins_row)
    # Write metadata
    select_all_md = select([metadata])
    all_md = engine.execute(select_all_md)
    for row in all_md:
        ins_md = metadata.insert().values(row)
        for i in range(num_wrk):
            worker_engines[i].execute(ins_md)


def get_cli_args(algorithm_args, node, num=None):
    common_args = [
        '-input_local_DB', 'dbs/local_dataset{}.db'.format(num) if num is not None else '',
        '-cur_state_pkl', 'state_{0}{1}.pkl'.format(node, num if num is not None else ''),
        '-prev_state_pkl', 'state_{0}{1}.pkl'.format(node, num if num is not None else ''),
        '-local_step_dbs', 'dbs/local_transfer.db',
        '-global_step_db', 'dbs/global_transfer.db',
        '-data_table', 'data',
        '-metadata_table', 'metadata',
        '-metadata_code_column', 'code',
        '-metadata_label_column', 'label',
        '-metadata_isCategorical_column', 'isCategorical',
        '-metadata_enumerations_column', 'enumerations',
        '-metadata_minValue_column', 'min',
        '-metadata_maxValue_column', 'max'
    ]
    return algorithm_args + common_args


def main():
    algorithm_args = [
        '-x', '',
        '-y', 'leftaccumbensarea, leftacgganteriorcingulategyrus, leftainsanteriorinsula',
        '-pathology', 'dementia',
        '-dataset', 'adni',
        '-filter', '',
        '-formula', '',
        '-coding', '',
    ]
    runner = create_runner(for_class='Pearson', found_in='PEARSON_EXPERIMENTAL/pearson',
                           of_type='local-global', num_workers=3, algorithm_args=algorithm_args)
    runner.run()


if __name__ == '__main__':
    main()
