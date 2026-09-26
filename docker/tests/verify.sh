#!/bin/sh
# Runs mvn verify, then prints the JaCoCo coverage totals. Used by the tests service in docker-compose.yml.

# target/ is a volume that outlives runs, so drop old results rather than print them after a failed build.
rm -f target/site/jacoco/jacoco.csv target/site/jacoco-it/jacoco.csv

mvn verify "$@"
status=$?

# Totals the per-class rows of a JaCoCo CSV report.
summary() {
    [ -f "$2" ] || { printf '%-18s no report (build stopped before it was written)\n' "$1"; return; }
    awk -F, -v name="$1" '
        function pct(missed, covered) {
            return missed + covered == 0 ? "   n/a" : sprintf("%5.1f%%", 100 * covered / (missed + covered))
        }
        NR > 1 { im += $4; ic += $5; bm += $6; bc += $7; lm += $8; lc += $9; mm += $12; mc += $13 }
        END {
            printf "%-18s %11s %11s %11s %11s\n", name, pct(im, ic), pct(bm, bc), pct(lm, lc), pct(mm, mc)
        }' "$2"
}

echo
echo "Code coverage"
printf '%-18s %11s %11s %11s %11s\n' "" "Instructions" "Branches" "Lines" "Methods"
summary "Unit tests" target/site/jacoco/jacoco.csv
summary "Integration tests" target/site/jacoco-it/jacoco.csv

exit $status
