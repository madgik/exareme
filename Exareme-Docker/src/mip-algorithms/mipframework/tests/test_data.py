import pytest
import pdb

from mipframework.data import DataBase, AlgorithmData


@pytest.fixture
def args():
    class Args:
        pass

    return Args()


@pytest.fixture
def db():
    path = "Exareme-Docker/src/mip-algorithms/mipframework/runner/dbs/datasets.db"
    db = DataBase(
        db_path=path,
        data_table_name="data",
        metadata_table_name="metadata",
        privacy=False,
        dropna=False,
    )
    yield db


class TestDataBase:
    def test_create_table(self, db):
        table = db.create_table("data")
        assert type(table).__name__ == "Table"

    def test_read_data_from_db_y(self, db, args):
        args.y = ["lefthippocampus"]
        args.filter = ""
        args.dataset = ["adni"]
        data = db.read_data_from_db(args)
        assert set(data.columns) == {"lefthippocampus", "dataset"}

    def test_read_data_from_db_xy(self, db, args):
        args.y = ["lefthippocampus"]
        args.x = ["righthippocampus"]
        args.filter = ""
        args.dataset = ["adni"]
        data = db.read_data_from_db(args)
        assert set(data.columns) == {"lefthippocampus", "dataset", "righthippocampus"}

    @pytest.mark.skip(reason="Longitudinal data not present in db")
    def test_read_longitudinal_data_from_db(self, db, args):
        args.y = ["lefthippocampus"]
        args.x = ["righthippocampus"]
        args.filter = ""
        args.dataset = ["fake_longitudinal"]
        data = db.read_longitudinal_data_from_db(args)
        assert {
            "lefthippocampus",
            "righthippocampus",
            "dataset",
            "subjectvisitid",
        } <= set(data.columns)

    def test_read_metadata_from_db(self, db, args):
        args.y = ["lefthippocampus"]
        args.x = ["righthippocampus"]
        args.filter = ""
        args.dataset = ["adni"]
        args.metadata_code_column = "code"
        args.metadata_label_column = "label"
        args.metadata_isCategorical_column = "isCategorical"
        args.metadata_enumerations_column = "enumerations"
        args.metadata_minValue_column = "min"
        args.metadata_maxValue_column = "max"
        metadata = db.read_metadata_from_db(args)
        assert type(metadata).__name__ == "AlgorithmMetadata"
        assert hasattr(metadata, "is_categorical")
        assert hasattr(metadata, "label")
        assert hasattr(metadata, "enumerations")
        assert hasattr(metadata, "minmax")


class TestAlgorithmData:
    def test_init(self, args):
        args.input_local_DB = (
            "Exareme-Docker/src/mip-algorithms/mipframework/runner/dbs/datasets.db"
        )
        args.data_table = "data"
        args.metadata_table = "metadata"
        args.privacy = False
        args.dropna = False
        args.y = ["lefthippocampus"]
        args.x = ["righthippocampus"]
        args.intercept = True
        args.formula_is_equation = True
        args.filter = ""
        args.dataset = ["adni"]
        args.metadata_code_column = "code"
        args.metadata_label_column = "label"
        args.metadata_isCategorical_column = "isCategorical"
        args.metadata_enumerations_column = "enumerations"
        args.metadata_minValue_column = "min"
        args.metadata_maxValue_column = "max"
        alg_data = AlgorithmData(args)
        assert len(alg_data.full) >= 1
        assert len(alg_data.variables) >= 1
        assert len(alg_data.covariables) >= 1
        assert alg_data.metadata is not None

    def test_init_with_new_style_formula(self, args):
        args.input_local_DB = (
            "Exareme-Docker/src/mip-algorithms/mipframework/runner/dbs/datasets.db"
        )
        args.data_table = "data"
        args.metadata_table = "metadata"
        args.privacy = False
        args.dropna = False
        args.y = ["gender"]
        args.x = ["lefthippocampus", "righthippocampus"]
        args.intercept = True
        args.formula = {
            "single": [
                {"var_name": "lefthippocampus", "unary_operation": "nop"},
                {"var_name": "righthippocampus", "unary_operation": "nop"},
            ],
            "interactions": [{"var1": "lefthippocampus", "var2": "righthippocampus"}],
        }
        args.formula_is_equation = False
        args.filter = ""
        args.dataset = ["adni"]
        args.metadata_code_column = "code"
        args.metadata_label_column = "label"
        args.metadata_isCategorical_column = "isCategorical"
        args.metadata_enumerations_column = "enumerations"
        args.metadata_minValue_column = "min"
        args.metadata_maxValue_column = "max"
        alg_data = AlgorithmData(args)
        assert list(alg_data.covariables.columns) == [
            "Intercept",
            "lefthippocampus",
            "righthippocampus",
            "lefthippocampus:righthippocampus",
        ]
