import io
import os
import sqlite3
import sys
import importlib
from pathlib import Path
from functools import wraps
from abc import ABCMeta, abstractmethod

import numpy as np
from sqlalchemy import create_engine, MetaData, Table, select, func

dbs_folder = Path(__file__).parent / "dbs"


def write_to_transfer_db(out, node):
    db_name = node + "_transfer.db"
    db_path = dbs_folder / db_name
    conn = sqlite3.connect(db_path.as_posix())
    c = conn.cursor()
    c.execute("INSERT INTO transfer VALUES (?)", (out,))
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


class Runner(object):
    __metaclass__ = ABCMeta

    def __init__(self, alg_cls, algorithm_args, num_wrk, split=False):
        # Split db
        if split:
            split_db(num_wrk)
        # Get cli arguments
        local_argv = [
            get_cli_args(algorithm_args, node="worker", num=i) for i in range(num_wrk)
        ]
        global_argv = get_cli_args(algorithm_args, node="master")

        # Create workers and master
        self.num_wrk = num_wrk
        self.workers = []
        for i in range(num_wrk):
            self.workers.append(alg_cls(local_argv[i]))
        self.master = alg_cls(global_argv)

    @abstractmethod
    def run(self):
        """This method is implemented in child classes according to algorithm type (
        local-global, multiple-local-global, iterative)"""


def make_transfer_db(node):
    db_name = node + "_transfer.db"
    path = dbs_folder / db_name
    conn = sqlite3.connect(path.as_posix())
    c = conn.cursor()
    c.execute("DROP TABLE IF EXISTS transfer")
    c.execute("CREATE TABLE transfer (data text)")


class LocalGlobalRunner(Runner):
    def run(self):
        make_transfer_db("local")
        for i in range(self.num_wrk):
            out = capture_stdout(self.workers[i].local_)()
            write_to_transfer_db(out, "local")
        self.master.global_()


class MultipleLocalGlobalRunner(Runner):
    def run(self):
        # ===== INIT ===== #
        # Locals
        make_transfer_db("local")
        for i in range(self.num_wrk):
            out = capture_stdout(self.workers[i].local_init)()
            write_to_transfer_db(out, "local")
        # Global
        make_transfer_db("global")
        out = capture_stdout(self.master.global_init)()
        write_to_transfer_db(out, "global")
        # ===== STEPS ===== #
        # TODO
        # ===== FINAL ===== #
        # Locals
        make_transfer_db("local")
        for i in range(self.num_wrk):
            out = capture_stdout(self.workers[i].local_final)()
            write_to_transfer_db(out, "local")
        # Global
        self.master.global_final()


class IterativeRunner(Runner):
    def run(self):
        # ===== INIT ===== #
        # Locals
        make_transfer_db("local")
        for i in range(self.num_wrk):
            out = capture_stdout(self.workers[i].local_init)()
            write_to_transfer_db(out, "local")
        # Global
        make_transfer_db("global")
        out = capture_stdout(self.master.global_init)()
        write_to_transfer_db(out, "global")
        # ===== STEP ===== #
        while True:
            # Locals
            make_transfer_db("local")
            for i in range(self.num_wrk):
                out = capture_stdout(self.workers[i].local_step)()
                write_to_transfer_db(out, "local")
            # Global
            make_transfer_db("global")
            out = capture_stdout(self.master.global_step)()
            write_to_transfer_db(out, "global")
            if "STOP" in capture_stdout(self.master.termination_condition)():
                break
        # ===== FINAL ===== #
        # Locals
        make_transfer_db("local")
        for i in range(self.num_wrk):
            out = capture_stdout(self.workers[i].local_final)()
            write_to_transfer_db(out, "local")
        # Global
        self.master.global_final()


def create_runner(cls, alg_type, algorithm_args, num_workers=3):
    if alg_type == "local-global":
        return LocalGlobalRunner(
            alg_cls=cls, algorithm_args=algorithm_args, num_wrk=num_workers
        )
    elif alg_type == "multiple-local-global":
        return MultipleLocalGlobalRunner(
            alg_cls=cls, algorithm_args=algorithm_args, num_wrk=num_workers
        )
    elif alg_type == "iterative":
        return IterativeRunner(
            alg_cls=cls, algorithm_args=algorithm_args, num_wrk=num_workers
        )


def split_db(num_wrk):
    db_path = dbs_folder / "datasets.db"
    engine = create_engine("sqlite:///{}".format(db_path), echo=False)
    sqla_md = MetaData(engine)
    data = Table("data", sqla_md, autoload=True)
    metadata = Table("metadata", sqla_md, autoload=True)

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
        db_path = dbs_folder / "local_dataset{}.db".format(i)
        worker_engines.append(create_engine("sqlite:///{}".format(db_path), echo=False))
        sqla_md.create_all(worker_engines[i])
    return worker_engines


def get_cli_args(algorithm_args, node, num=None):
    common_args = [
        "-input_local_DB",
        (
            (dbs_folder / "local_dataset{}.db".format(num)).as_posix()
            if num is not None
            else ""
        ),
        "-cur_state_pkl",
        (
            dbs_folder / "state_{0}{1}.pkl".format(node, num if num is not None else "")
        ).as_posix(),
        "-prev_state_pkl",
        (
            dbs_folder / "state_{0}{1}.pkl".format(node, num if num is not None else "")
        ).as_posix(),
        "-local_step_dbs",
        (dbs_folder / "local_transfer.db").as_posix(),
        "-global_step_db",
        (dbs_folder / "global_transfer.db").as_posix(),
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
