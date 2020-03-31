import pytest

from mipframework.data import get_formula


@pytest.fixture
def make_mock_args():
    class MockArgs(object):
        def __init__(self, x, y, formula):
            self.x = x
            self.y = y
            self.formula = formula

    return MockArgs


def test_get_formula_from_args(make_mock_args):
    expected = 'y ~ x * z + w'
    args = make_mock_args(x=None, y=None, formula=expected)
    output = get_formula._original(args, None)
    assert output == expected


def test_get_formula_y_only(make_mock_args):
    expected = 'x+y+z-1'
    args = make_mock_args(x=None, y=['x', 'y', 'z'], formula=None)
    output = get_formula._original(args, None)
    assert output == expected


def test_get_formula_y_and_x(make_mock_args):
    expected = 'y+x~z+w+t'
    args = make_mock_args(x=['z', 'w', 't'], y=['y', 'x'], formula=None)
    output = get_formula._original(args, None)
    assert output == expected


def test_get_formula_with_coding(make_mock_args):
    expected = 'y~x+C(z, Treatment)'
    args = make_mock_args(x=['x', 'z'], y=['y'], formula=None)
    args.coding = 'Treatment'
    is_categorical = {'x': 0, 'y': 0, 'z': 1}
    output = get_formula._original(args, is_categorical)
    assert output == expected
