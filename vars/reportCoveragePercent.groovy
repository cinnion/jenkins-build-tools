import org.ka8zrt.ReportCoverage

void call(Object badge, String metric = ReportCoverage.LINE_METRIC) {
    new ReportCoverage(this).reportCoveragePercent(badge, metric)
}
