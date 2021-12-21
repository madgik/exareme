import pytest

from mipframework.parameters import parse_cli_args, get_parser


def test_parse_exareme_args_without_shared_algorithm_args():
    algorithm_folder_path = "Exareme-Docker/src/mip-algorithms/PCA/"
    cli_args = [
        "-y",
        "gender",
        "-pathology",
        "dementia",
        "-dataset",
        "ppmi",
    ]
    args = parse_cli_args(algorithm_folder_path, cli_args)
    assert hasattr(args, "y")
    assert isinstance(args.y, list)
    assert hasattr(args, "pathology")
    assert isinstance(args.pathology, str)
    assert hasattr(args, "dataset")
    assert isinstance(args.dataset, list)


def test_parse_exareme_args_without_shared_algorithm_args_with_x():
    algorithm_folder_path = "Exareme-Docker/src/mip-algorithms/LOGISTIC_REGRESSION/"
    cli_args = [
        "-x",
        "righthippocampus,lefthippocampus",
        "-y",
        "gender",
        "-pathology",
        "dementia",
        "-dataset",
        "ppmi",
        "-positive_level",
        "AD",
        "-negative_level",
        "CN",
    ]
    args = parse_cli_args(algorithm_folder_path, cli_args)
    assert hasattr(args, "x")
    assert isinstance(args.x, list)
    assert hasattr(args, "y")
    assert isinstance(args.y, list)
    assert hasattr(args, "pathology")
    assert isinstance(args.pathology, str)
    assert hasattr(args, "dataset")
    assert isinstance(args.dataset, list)
    assert hasattr(args, "positive_level")
    assert isinstance(args.positive_level, str)
    assert hasattr(args, "negative_level")
    assert isinstance(args.negative_level, str)


def test_parse_exareme_args_with_shared_algorithm_args():
    algorithm_folder_path = "Exareme-Docker/src/mip-algorithms/LOGISTIC_REGRESSION/"
    cli_args = [
        "-x",
        "righthippocampus,lefthippocampus",
        "-y",
        "gender",
        "-pathology",
        "dementia",
        "-dataset",
        "ppmi",
        "-positive_level",
        "AD",
        "-negative_level",
        "CN",
        "-input_local_DB",
        "DUMMY",
        "-db_query",
        "DUMMY",
        "-cur_state_pkl",
        "DUMMY",
        "-prev_state_pkl",
        "DUMMY",
        "-local_step_dbs",
        "DUMMY",
        "-global_step_db",
        "DUMMY",
        "-data_table",
        "DUMMY",
        "-metadata_table",
        "DUMMY",
        "-metadata_code_column",
        "DUMMY",
        "-metadata_label_column",
        "DUMMY",
        "-metadata_isCategorical_column",
        "DUMMY",
        "-metadata_enumerations_column",
        "DUMMY",
        "-metadata_minValue_column",
        "DUMMY",
        "-metadata_maxValue_column",
        "DUMMY",
        "-metadata_sqlType_column",
        "DUMMY",
    ]
    args = parse_cli_args(algorithm_folder_path, cli_args)
    assert hasattr(args, "x")
    assert isinstance(args.x, list)
    assert hasattr(args, "y")
    assert isinstance(args.y, list)
    assert hasattr(args, "pathology")
    assert isinstance(args.pathology, str)
    assert hasattr(args, "dataset")
    assert isinstance(args.dataset, list)
    assert hasattr(args, "positive_level")
    assert isinstance(args.positive_level, str)
    assert hasattr(args, "negative_level")
    assert isinstance(args.negative_level, str)


# FIXME see reason below
@pytest.mark.xfail(reason="function doesnt handle wrong args, should be fixed")
def test_parse_exareme_args_wrong_args():
    algorithm_folder_path = "Exareme-Docker/src/mip-algorithms/LOGISTIC_REGRESSION/"
    cli_args = [
        "-x",
        "righthippocampus,lefthippocampus",
        "-y",
        "gender",
        "-pathology",
        "dementia",
        "-dataset",
        "ppmi",
        "-positive_level",
        "AD",
        "-negative_level",
        "CN",
        "-wrong_arg",
        "WRONG",
    ]
    args = parse_cli_args(algorithm_folder_path, cli_args)
    assert hasattr(args, "x")
    assert isinstance(args.x, list)
    assert hasattr(args, "y")
    assert isinstance(args.y, list)
    assert hasattr(args, "pathology")
    assert isinstance(args.pathology, str)
    assert hasattr(args, "dataset")
    assert isinstance(args.dataset, list)
    assert hasattr(args, "positive_level")
    assert isinstance(args.positive_level, str)
    assert hasattr(args, "negative_level")
    assert isinstance(args.negative_level, str)


def test_parse_exareme_args_escape_chars():
    algorithm_folder_path = "Exareme-Docker/src/mip-algorithms/LOGISTIC_REGRESSION/"
    cli_args = [
        "-x",
        "righthippocampus,lefthippocampus",
        "-y",
        "agegroup",
        "-pathology",
        "dementia",
        "-dataset",
        "ppmi",
        "-positive_level",
        "+80y",
        "-negative_level",
        "-50y",
    ]
    args = parse_cli_args(algorithm_folder_path, cli_args)
    assert hasattr(args, "x")
    assert isinstance(args.x, list)
    assert hasattr(args, "y")
    assert isinstance(args.y, list)
    assert hasattr(args, "pathology")
    assert isinstance(args.pathology, str)
    assert hasattr(args, "dataset")
    assert isinstance(args.dataset, list)
    assert hasattr(args, "positive_level")
    assert isinstance(args.positive_level, str)
    assert hasattr(args, "negative_level")
    assert isinstance(args.negative_level, str)
    assert args.negative_level == "-50y"
