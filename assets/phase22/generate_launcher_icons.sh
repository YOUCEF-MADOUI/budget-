#!/usr/bin/env bash
set -euo pipefail

# Reproduces the launcher assets from the user-supplied board with ImageMagick.
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
BOARD="$ROOT/assets/icon-board.png"
CROP="$ROOT/assets/phase22/icon-official-cropped.png"
RES="$ROOT/app/src/main/res"

convert "$BOARD" -crop 720x720+43+15 +repage "$CROP"
convert "$CROP" -resize 760x760 -gravity center -background none -extent 1080x1080 \
  "$RES/drawable-nodpi/ic_launcher_foreground.png"
convert "$CROP" -crop 550x460+85+145 +repage -colorspace gray -threshold 25% \
  -transparent black -resize 760x636 -gravity center -background none -extent 1080x1080 \
  "$RES/drawable-nodpi/ic_launcher_monochrome.png"

for spec in mdpi:48 hdpi:72 xhdpi:96 xxhdpi:144 xxxhdpi:192; do
  density="${spec%%:*}"
  size="${spec##*:}"
  center="$((size / 2))"
  mkdir -p "$RES/mipmap-$density"
  convert "$CROP" -filter Lanczos -resize "${size}x${size}" \
    "$RES/mipmap-$density/ic_launcher.png"
  convert "$CROP" -filter Lanczos -resize "${size}x${size}" \
    \( -size "${size}x${size}" xc:none -fill white \
       -draw "circle $center,$center $center,1" \) \
    -compose CopyOpacity -composite "$RES/mipmap-$density/ic_launcher_round.png"
done
