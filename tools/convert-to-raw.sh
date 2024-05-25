#! /bin/sh

ffmpeg -i $1 -f f32le -acodec pcm_f32le -ac 1 -ar 48000 "${1%.*}.raw"
