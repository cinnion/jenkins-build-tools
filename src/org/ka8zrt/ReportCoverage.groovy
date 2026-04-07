package org.ka8zrt

import groovy.transform.CompileStatic
import org.jenkinsci.plugins.badge.EmbeddableBadgeConfig

/**
 * A class for parsing a Python coverage XML coverage report and manipulating
 * an embeddable build status badge according to values stored in a
 * pyproject.toml file, which must exist but need not have configuration
 * settings specific to this class.
 */
@CompileStatic
class ReportCoverage extends UtilityBase {

    String lineMetric = 'line'
    String branchMetric = 'branch'
    String percentFormat = '%.2f%%'
    int toPercent = 100

    /**
     * Use xmllint to get a specified value from a coverage XML file and
     * return it as a string.
     *
     * @param key The key to the attribute in the XML coverage element.
     * @return The value as a string, which can then be converted by the
     *         caller to whatever type is needed.
     */
    static String getCoverageValueAsString(String coverageFile, String key) {
        String value = ''

        try {
            value = sh(
                script: "xmllint --xpath 'string(//coverage/@${key})' ${coverageFile}",
                returnStdout: true
            ).trim()

            return value
        } catch (e) {
            error("Failed to find ${key} in ${coverageFile}")
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
    static void reportCoveragePercent(EmbeddableBadgeConfig badge, String metric = lineMetric) {
        try {
            // Allowed values.
            String[] allowedMetrics = [lineMetric, branchMetric]

            // Validate the metric passed in.
            if (!(metric in allowedMetrics)) {
                throw new IllegalArgumentException("Invalid choice: ${metric}. Must be one of ${allowedMetrics}")
            }

            // Read our settings.
            Map settingsJson = callAndReturnJson('readConfigSettings.py')

            // Read our values
            // int linesValid = getCoverageValueAsString(settingsJson.coverageFile, 'lines-valid') as int
            // int linesCovered = getCoverageValueAsString(settingsJson.coverageFile, 'lines-covered') as int
            BigDecimal lineRate = getCoverageValueAsString(settingsJson.coverageFile, 'line-rate').toBigDecimal()
            // int branchedValid = getCoverageValueAsString(settingsJson.coverageFile, 'branches-valid') as int
            // int branchesCovered = getCoverageValueAsString(settingsJson.coverageFile, 'branches-covered') as int
            BigDecimal branchRate = getCoverageValueAsString(settingsJson.coverageFile, 'branch-rate').toBigDecimal()
            // int complexity = getCoverageValueAsString(settingsJson.coverageFile, 'complexity') as int

            // Set our status to the percentage.
            if (metric == lineMetric) {
                badge.setStatus(String.format(percentFormat, lineRate * toPercent))
            } else {
                badge.setStatus(String.format(percentFormat, branchRate * toPercent))
            }

            // Look at our lineRate and figure out whether we are warning or failing.
            if (lineRate * toPercent < settingsJson.fail_under) {
                badge.setColor(settingsJson.fail_color)
            } else if (lineRate * toPercent < settingsJson.warn_under) {
                badge.setColor(settingsJson.warn_color)
            } else {
                badge.setColor(settingsJson.pass_color)
            }
        } catch (e) {
            error("An error occurred in reportCoveragePercent - ${e}")
        }
    }

    /**
     * Convenience method for reportCoveragePercent() with the default of
     * "line" specified.
     *
     * @param The embeddable build status badge returned by that plugin's
     *        addEmbeddableBadgeConfiguration method.
     *
     * @return Nothing.
     */
    static void reportCoverageLinePercent(EmbeddableBadgeConfig badge) {
        return reportCoveragePercent(badge)
    }

    /**
     * Convenience method for reportCoveragePercent() with the default of
     * "branch" specified.
     *
     * @param The embeddable build status badge returned by that plugin's
     *        addEmbeddableBadgeConfiguration method.
     *
     * @return Nothing.
     */
    static void reportCoverageBranchPercent(EmbeddableBadgeConfig badge) {
        return reportCoveragePercent(badge, branchMetric)
    }

}
