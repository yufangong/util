junit_tests(
    sources = globs("*.scala"),
    compiler_option_sets = {"fatal_warnings"},
    dependencies = [
        "3rdparty/jvm/com/github/ben-manes/caffeine",
        "3rdparty/jvm/com/google/code/findbugs:jsr305",
        "3rdparty/jvm/junit",
        "3rdparty/jvm/org/mockito:mockito-all",
        "3rdparty/jvm/org/scalatest",
        "util/util-cache/src/main/scala/com/twitter/cache",
        "util/util-cache/src/main/scala/com/twitter/cache/caffeine",
        "util/util-cache/src/test/scala/com/twitter/cache:abstract_tests",
        "util/util-core/src/main/scala/com/twitter/util",
    ],
)
