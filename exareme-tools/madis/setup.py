# /usr/bin/env python

from setuptools import setup, find_packages

setup(
    install_requires=['distribute'],
    name='madis',
    version='${VERSION}',
    packages=find_packages('.')
)
