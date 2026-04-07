# Jenkins Build Tools Library

A set of shared pipeline functions for use by Jenkins build jobs.

<!-- START doctoc generated TOC please keep comment here to allow auto update -->

<!---toc start-->

* [Jenkins Build Tools Library](#jenkins-build-tools-library)
* [Requirements](#requirements)
* [Installation](#installation)
* [Provided functions](#provided-functions)
  * [reportCoverage()](#reportcoverage)

<!---toc end-->

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


# Requirements

To parse the `pyproject.toml` file of a project, this library makes use of the 
default Python 3 version found in the search path as `python3`, and needs the 
following packages installed (follow the instructions on each of those pages to
install them):

- [tomli](https://pypi.org/project/tomli/) for Python versions less than 3.11 or
- [tomllib](https://docs.python.org/3/library/tomllib.html) for Python 3.11 and later

All other Python libraries used should be automatically installed for any 
supported Python version.

It also requires the `xmllint` utility, which is available as a part of the 
`libxml2` package on EL9 based systems, and similar packages on other distros.

# Installation

To Be Provided

# Provided functions

## reportCoverage()

The `reportCoverage()` function is designed to work with an embeddable build status badge produced
by the Jenkins [Embeddable Build Status](https://plugins.jenkins.io/embeddable-build-status/) plugin.
It reads the `coverage.xml` file produced by the Python [coverage](https://coverage.readthedocs.io/)
tool and makes the calls to set the status string and color of the badge according to settings
configured in the project's `pyproject.toml` file. The settings can be set using the following block
in the file (which shows the default values):

```toml
[tools.ReportCoverage]
coverage_file = "coverage.xml"
fail_under = 60
fail_color = "red"
warn_under = 80
warn_color = "orange"
pass_color = "green"

```

**NOTE**: Do not call this file unless you have a `pyproject.toml` file, as while it will generate the
expected results without setting the values in the file, the failure to find the file or parse it is
considered an error.
