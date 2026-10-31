# Wellen Release

builds and publishes a release from the command line, no IDE needed.

```
dist/release.sh v0.14                 # full release, asks before pushing
dist/release.sh v0.14 --build-only    # just build, nothing committed (tree may be dirty)
dist/release.sh v0.14 --no-push       # build + commit + tag locally
dist/release.sh v0.14 --publish-only  # later: push that commit + tag and create the GitHub release
```

other flags: `-y` skips the question before pushing, `--skip-tests` skips the tests.

## Before a Release

1. add a section `## v0.14` to `RELEASE_NOTES.md` ( the date is added by the script )
2. commit all changes. the working tree must be clean
3. `gh auth login` once ( GitHub CLI, `brew install gh` )

## Requirements

- JDK 17 or newer ( or set `JAVA_HOME` )
- Processing.app ( default `/Applications/Processing.app`, or set `PROCESSING_APP` )
- oscP5 ( default `~/Documents/Processing/libraries/oscP5/library/oscP5.jar`, or set `OSCP5_JAR` )

## What the Script Does

1. sets the version in `lib/library.properties`, `Wellen.java` and dates the release notes heading
2. compiles `src` into `lib/wellen.jar` ( java 17 ) and runs `wellen.tests.TestMIDI`
3. generates the reference ( javadoc ) into `docs`
4. runs `build_dist.sh`: builds `processing-library/wellen` and `wellen.zip`
5. commits `release v0.14`, tags `v0.14`, pushes, and creates the GitHub release with the release notes section,
   `wellen.zip` and `wellen.txt`

`build_dist.sh` can also be run alone. it packs the existing `lib/wellen.jar` and warns if the jar is older than `src`.
