import os
from contextlib import contextmanager
from invoke import task, Responder

root = os.path.dirname(__file__)


@task
def clean(c):
    caches = "**/*.pyc"
    c.run("rm -rf {}".format(caches))


@task(clean)
def build(c):
    with c.cd(root):
        with c.cd("Exareme-Docker"):
            c.run("docker build -t jassak/exareme:local_image .")
        with c.cd("Local-Deployment"):
            responder = Responder(
                pattern=r"Do you wish to run Portainer?", response="n\n",
            )
            c.run("sudo ./deployLocal.sh", watchers=[responder])


@task
def test(c):
    with c.cd(root):
        c.run("source venv/bin/activate")
        # c.run('pip install -r Exareme-Docker/src/mip-algorithms/tests/tests_requirements.txt')
        with c.cd(os.path.join("Exareme-Docker", "src", "mip-algorithms", "tests")):
            with environ(ENVIRONMENT_TYPE="TEST"):
                with c.cd("algorithm_tests"):
                    c.run("python -B -m pytest")
                with c.cd("integration_tests"):
                    c.run("python -B -m pytest")
            with c.cd("exareme_tests"):
                c.run("python3.6 -m pytest")
            with c.cd("algorithm_tests_with_privacy"):
                c.run("python3.6 -m pytest")
        c.run("deactivate")


@contextmanager
def environ(**env):
    original_environ = os.environ.copy()
    os.environ.update(env)
    yield
    os.environ = original_environ
