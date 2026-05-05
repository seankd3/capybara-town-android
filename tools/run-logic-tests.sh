#!/usr/bin/env sh
set -eu

out_dir="${TMPDIR:-/tmp}/capybara-town-test"
rm -rf "$out_dir"
mkdir -p "$out_dir"

javac -d "$out_dir" \
  app/src/main/java/com/capybaratown/game/GameModel.java \
  test/java/com/capybaratown/game/GameModelTest.java

java -cp "$out_dir" com.capybaratown.game.GameModelTest
