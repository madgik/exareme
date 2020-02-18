import json
import numpy as np
from PEARSON_EXPERIMENTAL import Pearson
from sqlalchemy import create_engine, MetaData, Table, select, insert, func


class Worker(Pearson):
    pass


class Master(Pearson):
    pass


def split_db(num_wrk):
    # Connect to original DB
    engine = create_engine('sqlite:///datasets.db', echo=False)
    sqla_md = MetaData(engine)
    data = Table('data', sqla_md, autoload=True)
    metadata = Table('metadata', sqla_md, autoload=True)
    # Create node DBs
    worker_engines = []
    for i in range(num_wrk):
        worker_engines.append(create_engine('sqlite:///local_dataset{}.db'.format(i), echo=False))
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
    # select_all_md = select([metadata])
    # all_md = engine.execute(select_all_md)
    # ins_md = metadata.insert().values(all_md.fetchall())
    # for i in range(num_wrk):
    #     worker_engines[i].execute(ins_md)


def gen_arguments(node, num=None):
    algorithm_args = [
        '-x', 'leftaccumbensarea, leftacgganteriorcingulategyrus, leftainsanteriorinsula',
        '-y', '',
        '-pathology', 'dementia',
        '-dataset', 'adni',
        '-filter', '',
        '-formula', '',
        '-coding', '',
    ]
    common_args = [
        '-input_local_DB', '',
        '-cur_state_pkl', 'state_{0}{1}.pkl'.format(node, num if num else ''),
        '-prev_state_pkl', 'state_{0}{1}.pkl'.format(node, num if num else ''),
        '-local_step_dbs', 'local.db',
        '-global_step_db', 'global.db',
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


class Runner(object):
    def __init__(self, num_wrk):
        split_db(num_wrk)
        local_argv = []
        for i in range(num_wrk):
            local_argv[i] = gen_arguments(node='worker', num=i)
        global_argv = gen_arguments(node='master')
        self.num_wrk = num_wrk
        self.workers = []
        for i in range(num_wrk):
            self.workers[i] = Worker(local_argv[i])
        self.master = Master(global_argv)

    def execute(self):
        raise NotImplementedError


class LocalGlobal(Runner):
    def execute(self):
        for i in range(self.num_wrk):
            self.workers[i].local_()
        self.master.global_()


if __name__ == '__main__':
    split_db(3)