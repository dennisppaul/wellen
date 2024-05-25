#! /bin/sh

echo "usage: convert-to-raw.sh INPUT_FILE OUTPUT_SAMPLE_RATE"

ffmpeg -i $1 -f f32le -acodec pcm_f32le -ac 1 -ar $2 "${1%.*}.raw"
