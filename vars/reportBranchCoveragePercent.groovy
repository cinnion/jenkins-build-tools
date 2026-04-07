import org.ka8zrt.ReportCoverage

void call(Object badge) {
    new ReportCoverage(this).reportCoveragePercent(badge, ReportCoverage.BRANCH_METRIC)
}
