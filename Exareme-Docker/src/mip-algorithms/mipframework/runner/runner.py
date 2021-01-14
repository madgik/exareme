import io
import sqlite3
import sys
from pathlib import Path
from functools import wraps
from abc import ABCMeta, abstractmethod

import numpy as np
from sqlalchemy import create_engine, MetaData, Table, select, func
from tqdm import tqdm

dbs_dir = Path(__file__).parent / "dbs"


ALGORITHM_TYPES = {
    "PCA": "multiple-local-global",
    "Pearson": "local-global",
    "LogisticRegression": "iterative",
    "CalibrationBelt": "iterative",
    "DescriptiveStats": "local-global",
    "KaplanMeier": "local-global",
    "ThreeC": "local",
    "Anova": "local-global",
}


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


class RunnerABC(object):
    __metaclass__ = ABCMeta

    def __init__(self, alg_cls, algorithm_args, num_wrk):
        self.subdir_path = dbs_dir / "{num_wrk}LocalDBs".format(num_wrk=num_wrk)
        if not self.subdir_path.exists():
            split_db(num_wrk)

        local_argv = [
            self.get_cli_args(algorithm_args, node="worker", num=i)
            for i in range(num_wrk)
        ]
        global_argv = self.get_cli_args(algorithm_args, node="master")

        self.num_wrk = num_wrk
        self.workers = [alg_cls(local_argv[i]) for i in range(num_wrk)]
        self.master = alg_cls(global_argv)

    def get_cli_args(self, algorithm_args, node, num=None):
        if num is None:
            num = ""
        common_args = [
            "-input_local_DB",
            (
                (self.subdir_path / "local_dataset{}.db".format(num)).as_posix()
                if num is not None
                else ""
            ),
            "-cur_state_pkl",
            (self.subdir_path / "state_{0}{1}.pkl".format(node, num)).as_posix(),
            "-prev_state_pkl",
            (self.subdir_path / "state_{0}{1}.pkl".format(node, num)).as_posix(),
            "-local_step_dbs",
            (self.subdir_path / "local_transfer.db").as_posix(),
            "-global_step_db",
            (self.subdir_path / "global_transfer.db").as_posix(),
            "-data_table",
            "data",
            "-metadata_table",
            "metadata",
            "-metadata_code_column",
            "code",
            "-metadata_label_column",
            "label",
            "-metadata_isCategorical_column",
            "isCategorical",
            "-metadata_enumerations_column",
            "enumerations",
            "-metadata_minValue_column",
            "min",
            "-metadata_maxValue_column",
            "max",
        ]
        return algorithm_args + common_args

    def execute_runner_steps(self, method):
        node_type = method.split("_")[0]
        self.make_transfer_db(node_type)
        if node_type == "local":
            nodes = self.workers
        elif node_type == "global":
            nodes = [self.master]
        else:
            raise ValueError("method name should start with local or global")
        for node in nodes:
            out = capture_stdout(getattr(node, method))()
            self.write_to_transfer_db(out, node_type)

    def make_transfer_db(self, node):
        db_name = node + "_transfer.db"
        path = self.subdir_path / db_name
        conn = sqlite3.connect(path.as_posix())
        c = conn.cursor()
        c.execute("DROP TABLE IF EXISTS transfer")
        c.execute("CREATE TABLE transfer (data text)")

    def write_to_transfer_db(self, out, node):
        db_name = node + "_transfer.db"
        db_path = self.subdir_path / db_name
        conn = sqlite3.connect(db_path.as_posix())
        c = conn.cursor()
        c.execute("INSERT INTO transfer VALUES (?)", (out,))
        conn.commit()
        conn.close()

    @abstractmethod
    def run(self):
        """This method is implemented in child classes according to algorithm type (
        local-global, multiple-local-global, iterative)"""


class LocalRunner(RunnerABC):
    def run(self):
        self.workers[0].local_pure()


class LocalGlobalRunner(RunnerABC):
    def run(self):
        self.execute_runner_steps("local_")
        self.master.global_()


class MultipleLocalGlobalRunner(RunnerABC):
    def run(self):
        self.execute_runner_steps("local_init")
        self.execute_runner_steps("global_init")
        # TODO steps
        self.execute_runner_steps("local_final")
        self.master.global_final()


class IterativeRunner(RunnerABC):
    def run(self):
        self.execute_runner_steps("local_init")
        self.execute_runner_steps("global_init")
        while True:
            self.execute_runner_steps("local_step")
            self.execute_runner_steps("global_step")
            if "STOP" in capture_stdout(self.master.termination_condition)():
                break
        self.execute_runner_steps("local_final")
        self.master.global_final()


def create_runner(algorithm_class, algorithm_args, num_workers=3):
    alg_type = ALGORITHM_TYPES[algorithm_class.__name__]
    if alg_type == "local" and num_workers > 1:
        raise ValueError(
            "Purely local algorithms should only have one worker. Please set"
            " num_workers=1."
        )
    if alg_type == "local-global":
        return LocalGlobalRunner(
            alg_cls=algorithm_class, algorithm_args=algorithm_args, num_wrk=num_workers
        )
    elif alg_type == "multiple-local-global":
        return MultipleLocalGlobalRunner(
            alg_cls=algorithm_class, algorithm_args=algorithm_args, num_wrk=num_workers
        )
    elif alg_type == "iterative":
        return IterativeRunner(
            alg_cls=algorithm_class, algorithm_args=algorithm_args, num_wrk=num_workers
        )
    elif alg_type == "local":
        return LocalRunner(
            alg_cls=algorithm_class, algorithm_args=algorithm_args, num_wrk=num_workers
        )


def split_db(num_wrk):
    db_path = dbs_dir / "datasets.db"
    engine = create_engine("sqlite:///{}".format(db_path), echo=False)
    db_metadata = MetaData(engine)
    data = Table("data", db_metadata, autoload=True)
    metadata = Table("metadata", db_metadata, autoload=True)

    worker_engines = create_worker_db_engines(num_wrk, db_metadata)
    split_data_to_workers(data, engine, num_wrk, worker_engines)
    write_metadata_to_workers(engine, metadata, num_wrk, worker_engines)


def write_metadata_to_workers(engine, metadata, num_wrk, worker_engines):
    select_all_md = select([metadata])
    all_md = engine.execute(select_all_md)
    select_count = select([func.count()]).select_from(metadata)
    count = engine.execute(select_count).fetchone()[0]
    with tqdm(total=count, desc="Splitting data to Worker DBs") as pbar:
        for row in all_md:
            ins_md = metadata.insert().values(row)
            for i in range(num_wrk):
                worker_engines[i].execute(ins_md)
            pbar.update(1)


def split_data_to_workers(data, engine, num_wrk, worker_engines):
    select_count = select([func.count()]).select_from(data)
    num_rows = engine.execute(select_count).fetchone()[0]
    ri = iter(np.random.randint(num_wrk, size=num_rows))
    select_all = select([data])
    all_data = engine.execute(select_all)
    with tqdm(total=num_rows, desc="Splitting data to Worker DBs") as pbar:
        for row in all_data:
            ins_row = data.insert().values(row)
            worker_engines[next(ri)].execute(ins_row)
            pbar.update(1)


def create_worker_db_engines(num_wrk, db_metadata):
    subdir_path = dbs_dir / "{num_wrk}LocalDBs".format(num_wrk=num_wrk)
    if subdir_path.exists():
        subdir_path.rmdir()
    subdir_path.mkdir()
    worker_engines = []
    for i in range(num_wrk):
        db_path = subdir_path / "local_dataset{}.db".format(i)
        worker_engines.append(create_engine("sqlite:///{}".format(db_path), echo=False))
        db_metadata.create_all(worker_engines[i])
    return worker_engines
