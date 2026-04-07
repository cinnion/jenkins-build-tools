#!/usr/bin/env python3
#
# A little utility for finding and parsing the pyproject.toml file of a python project to report the values for
# tools.ReportCoverage.
#
# The output is a JSON object, for easy parsing by Jenkins Groovy scripts.
#
import json
import subprocess
import sys
from pathlib import Path

if sys.version_info >= (3, 11):
    import tomllib
else:
    import tomli as tomllib


def parse_pyproject() -> None:
    """
    This method parses a pyproject.toml file found in the git root directory for the project, and extracts two
    values in the section "[tools.ReportCoverage]", namely, "fail_under" and "warn_under".

    For simplicity, error handling is done entirely through exceptions, some of which are caught and re-raised
    as a different exception type.

    :return: None
    """

    # Get the root of our current git project and from that, the path for our pyproject.toml file
    result = subprocess.run(["git", "rev-parse", "--show-toplevel"], capture_output=True, text=True, check=True)
    git_root = result.stdout.strip()
    filepath = Path(git_root) / "pyproject.toml"

    # Try to load the file, and if we get exceptions, intercept the exceptions and try to make them a little
    # more user friendly.
    try:
        with open(filepath, "rb") as file:
            data = tomllib.load(file)
    except FileNotFoundError:
        raise Exception(f"Error: file '{filepath}' not found.")
    except tomllib.TOMLDecodeError as e:
        raise Exception(f"Error: Invalid TOML format for pyproject.toml - {e}")

    # Start with a dictionary of default values, then merge in override values from the TOML file.
    values = {
        "coverage_file": "coverage.xml",
        "fail_under": 60,
        "fail_color": "red",
        "warn_under": 80,
        "warn_color": "orange",
        "pass_color": "green",
    }
    if data["tool"] and data["tool"]["ReportCoverage"]:
        values = values | data["tool"]["ReportCoverage"]
    print(json.dumps(values, indent=4, sort_keys=True))


if __name__ == "__main__":
    try:
        parse_pyproject()
    except Exception as e:
        print(f"An error occurred: {e}", file=sys.stderr)
        exit(1)

exit(0)
