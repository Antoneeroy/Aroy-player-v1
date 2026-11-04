# Aroy Player playback fixes

## Changes
- Play/Pause now uses ExoPlayer's `playWhenReady` intent as the source of truth. This allows Pause to work while a track is buffering, when `isPlaying` is still false.
- Resume no longer reports playback as active before ExoPlayer has actually started. The player listener remains responsible for publishing the active playback state.
- Resuming an ended player seeks to the beginning before starting again.
- Queue advancement on `STATE_ENDED` is guarded so duplicate end callbacks cannot advance more than one track.
