import io
import os
import sqlite3
import sys
import importlib
from functools import wraps

import numpy as np
from sqlalchemy import create_engine, MetaData, Table, select, func

dbs_path = os.path.join(os.path.dirname(__file__), 'dbs')


def write_to_transfer_db(out, db_name):
    conn = sqlite3.connect(os.path.join(dbs_path, db_name))
    c = conn.cursor()
    c.execute('INSERT INTO transfer VALUES (?)', (out,))
    conn.commit()
    conn.close()


def capture_stdout(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        old_stdout = sys.stdout
        sys.stdout = new_stdout = io.BytesIO()
        func(*args, **kwargs)
        func_out = new_stdout.getvalue()
        sys.stdout = old_stdout
        new_stdout.close()
        return func_out

    return wrapper


def create_runner(for_class, alg_type, found_in, algorithm_args, num_workers=3):
    folder = os.path.split(found_in)
    folder = folder[0] + '.' + folder[1]
    mod = importlib.import_module(folder)
    alg_cls = getattr(mod, for_class)

    # Create Worker and Master classes
    Worker = type('Worker', (alg_cls,), {})
    Master = type('Master', (alg_cls,), {})

    class Runner(object):
        def __init__(self, num_wrk, split=False):
            # Split db
            if split:
                split_db(num_wrk)
            # Get cli arguments
            local_argv = [
                get_cli_args(algorithm_args, node='worker', num=i)
                for i in range(num_wrk)
            ]
            global_argv = get_cli_args(algorithm_args, node='master')

            # Create workers and master
            self.num_wrk = num_wrk
            self.workers = []
            for i in range(num_wrk):
                self.workers.append(Worker(local_argv[i]))
            self.master = Master(global_argv)

        @staticmethod
        def make_transfer_db(path):
            conn = sqlite3.connect(path)
            c = conn.cursor()
            c.execute('DROP TABLE IF EXISTS transfer')
            c.execute('CREATE TABLE transfer (data text)')

        def run(self):
            raise NotImplementedError

    if alg_type == 'local-global':
        class LocalGlobalRunner(Runner):
            def run(self):
                self.make_transfer_db(path=os.path.join(dbs_path, 'local_transfer.db'))
                for i in range(self.num_wrk):
                    out = capture_stdout(self.workers[i].local_)()
                    write_to_transfer_db(out, 'local_transfer.db')
                self.master.global_()

        return LocalGlobalRunner(num_wrk=num_workers)

    elif alg_type == 'iterative':
        class IterativeRunner(Runner):
            def run(self):
                # ===== INIT ===== #
                # Locals
                self.make_transfer_db(path=os.path.join(dbs_path, 'local_transfer.db'))
                for i in range(self.num_wrk):
                    out = capture_stdout(self.workers[i].local_init)()
                    write_to_transfer_db(out, 'local_transfer.db')
                # Global
                self.make_transfer_db(path=os.path.join(dbs_path, 'global_transfer.db'))
                out = capture_stdout(self.master.global_init)()
                write_to_transfer_db(out, 'global_transfer.db')
                # ===== STEP ===== #
                while True:
                    # Locals
                    self.make_transfer_db(path=os.path.join(dbs_path, 'local_transfer.db'))
                    for i in range(self.num_wrk):
                        out = capture_stdout(self.workers[i].local_step)()
                        write_to_transfer_db(out, 'local_transfer.db')
                    # Global
                    self.make_transfer_db(path=os.path.join(dbs_path, 'global_transfer.db'))
                    out = capture_stdout(self.master.global_step)()
                    write_to_transfer_db(out, 'global_transfer.db')
                    if 'STOP' in capture_stdout(self.master.termination_condition)():
                        break
                # ===== FINAL ===== #
                # Locals
                self.make_transfer_db(path=os.path.join(dbs_path, 'local_transfer.db'))
                for i in range(self.num_wrk):
                    out = capture_stdout(self.workers[i].local_final)()
                    write_to_transfer_db(out, 'local_transfer.db')
                # Global
                self.master.global_final()

        return IterativeRunner(num_wrk=num_workers)


def split_db(num_wrk):
    engine = create_engine('sqlite:///{}'.format(os.path.join(dbs_path, 'datasets.db')), echo=False)
    sqla_md = MetaData(engine)
    data = Table('data', sqla_md, autoload=True)
    metadata = Table('metadata', sqla_md, autoload=True)

    worker_engines = create_worker_db_engines(num_wrk, sqla_md)

    split_data_to_workers(data, engine, num_wrk, worker_engines)

    write_metadata_to_workers(engine, metadata, num_wrk, worker_engines)


def write_metadata_to_workers(engine, metadata, num_wrk, worker_engines):
    select_all_md = select([metadata])
    all_md = engine.execute(select_all_md)
    for row in all_md:
        ins_md = metadata.insert().values(row)
        for i in range(num_wrk):
            worker_engines[i].execute(ins_md)


def split_data_to_workers(data, engine, num_wrk, worker_engines):
    select_count = select([func.count()]).select_from(data)
    num_rows = engine.execute(select_count).fetchone()[0]
    ri = iter(np.random.randint(num_wrk, size=num_rows))
    select_all = select([data])
    all_data = engine.execute(select_all)
    for row in all_data:
        ins_row = data.insert().values(row)
        worker_engines[next(ri)].execute(ins_row)


def create_worker_db_engines(num_wrk, sqla_md):
    worker_engines = []
    for i in range(num_wrk):
        worker_engines.append(
            create_engine('sqlite:///{}'.format(
                os.path.join(dbs_path, 'local_dataset{}.db'.format(i))
            ), echo=False)
        )
        sqla_md.create_all(worker_engines[i])
    return worker_engines


def get_cli_args(algorithm_args, node, num=None):
    common_args = [
        '-input_local_DB', os.path.join(dbs_path, 'local_dataset{}.db'.format(
                num) if num is not None else ''),
        '-cur_state_pkl', os.path.join(dbs_path, 'state_{0}{1}.pkl'.format(node, num if num is not None else '')),
        '-prev_state_pkl', os.path.join(dbs_path, 'state_{0}{1}.pkl'.format(node, num if num is not None else '')),
        '-local_step_dbs', os.path.join(dbs_path, 'local_transfer.db'),
        '-global_step_db', os.path.join(dbs_path, 'global_transfer.db'),
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

