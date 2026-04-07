package org.ka8zrt

import groovy.transform.CompileDynamic

/**
 * A class for parsing a Python coverage XML coverage report and manipulating
 * an embeddable build status badge according to values stored in a
 * pyproject.toml file, which must exist but need not have configuration
 * settings specific to this class.
 */
@CompileDynamic
class ReportCoverage extends UtilityBase {

    static String LINE_METRIC = 'line'
    static String BRANCH_METRIC = 'branch'
    static String PERCENT_FORMAT = '%.2f%%'
    static int TO_PERCENT = 100

    /**
     * Constructor for the class. We save the script parameter for accessing methods like error, sh and such.
     *
     * @param script The Jenkins instance
     */
    ReportCoverage(Script script) {
        super(script)
    }

    /**
     * Use xmllint to get a specified value from a coverage XML file and
     * return it as a string.
     *
     * @param key The key to the attribute in the XML coverage element.
     * @return The value as a string, which can then be converted by the
     *         caller to whatever type is needed.
     */
    String getCoverageValueAsString(String coverageFile, String key) {
        try {
            String value = this.script.sh(script: "xmllint --xpath 'string(//coverage/@${key})' ${coverageFile}",
                    returnStdout: true).trim()

            return value
        } catch (ignored) {
            this.script.error("Failed to find ${key} in ${coverageFile}")
        }
    }

    /**
     * Read the coverage XML file, and manipulate the badge to  set the
     * status and the color based on a fail/warn/pass grading, the
     * grading limits and colors being overridable in the project's
     * pypackage.toml file.
     *
     * @param The embeddable build status badge returned by that plugin's
     *        addEmbeddableBadgeConfiguration method.
     * @param The metric to use. One of "line" or "branch", defaulting to
     *        "line".
     *
     * @return Nothing.
     */
    void reportCoveragePercent(Object badge, String metric = LINE_METRIC) {
        try {
            // Allowed values.
            String[] allowedMetrics = [LINE_METRIC, BRANCH_METRIC]

            // Validate the metric passed in.
            if (!(metric in allowedMetrics)) {
                throw new IllegalArgumentException("Invalid choice: ${metric}. Must be one of ${allowedMetrics}")
            }

            // Read our settings.
            Map settingsJson = callAndReturnJson('readConfigSettings.py')

            // Read our values
            // int linesValid = getCoverageValueAsString(settingsJson.coverage_file, 'lines-valid') as int
            // int linesCovered = getCoverageValueAsString(settingsJson.coverage_file, 'lines-covered') as int
            BigDecimal lineRate = getCoverageValueAsString(settingsJson.coverage_file as String, 'line-rate').toBigDecimal()
            // int branchedValid = getCoverageValueAsString(settingsJson.coverage_file, 'branches-valid') as int
            // int branchesCovered = getCoverageValueAsString(settingsJson.coverage_file, 'branches-covered') as int
            BigDecimal branchRate = getCoverageValueAsString(settingsJson.coverage_file as String, 'branch-rate').toBigDecimal()
            // int complexity = getCoverageValueAsString(settingsJson.coverage_file, 'complexity') as int

            // Set our status to the percentage.
            if (metric == LINE_METRIC) {
                badge.setStatus(String.format(PERCENT_FORMAT, lineRate * TO_PERCENT))
            } else {
                badge.setStatus(String.format(PERCENT_FORMAT, branchRate * TO_PERCENT))
            }

            // Look at our lineRate and figure out whether we are warning or failing.
            if (lineRate * TO_PERCENT < (settingsJson.fail_under as BigDecimal)) {
                badge.setColor(settingsJson.fail_color)
            } else if (lineRate * TO_PERCENT < (settingsJson.warn_under as BigDecimal)) {
                badge.setColor(settingsJson.warn_color)
            } else {
                badge.setColor(settingsJson.pass_color)
            }
        } catch (e) {
            this.script.error("An error occurred in reportCoveragePercent - ${e}")
        }
    }
}
