# this script avoids a weird problem where Gradle is printing the autograder output at the wrong time

export RICECHECKS_QUIET=true
./gradlew --console=plain :autograder:check autograder
retVal=$?
echo
echo ============= exampleRegex/
echo
cat exampleRegex/build/autograder/report.txt
echo
echo ============= exampleRpn/
echo
cat exampleRpn/build/autograder/report.txt
echo
echo ============= exampleSort/
echo
cat exampleSort/build/autograder/report.txt
echo
echo ============= standaloneSort/
echo
cat standaloneSort/build/autograder/report.txt

# debugging for GitHub Actions
# echo 'Where are the google-java-format directories?'
# find . -name google-java-format -print | xargs ls -lR > tmp
# cat tmp

exit $retVal
