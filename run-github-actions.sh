# this script avoids a weird problem where Gradle is printing the autograder output at the wrong time

export RICECHECKS_QUIET=true
./gradlew --console=plain :autograder:check autograder
retVal=$?
echo ============= exampleRegex/
cat exampleRegex/build/autograder/report.txt
echo ============= exampleRpn/
cat exampleRpn/build/autograder/report.txt
echo ============= exampleSort/
cat exampleSort/build/autograder/report.txt
echo ============= standaloneSort/
cat standaloneSort/build/autograder/report.txt
exit $retVal

# debugging for GitHub Actions
echo "Where are the google-java-format directories?"
find . -name google-java-format -print | xargs ls
