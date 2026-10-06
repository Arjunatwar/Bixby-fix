# F23 Bixby Wake Fix v5

Targets the actual failure seen in the supplied Bixby logs: after media/AEC stops, Bixby's wakeup service reaches its restart path and a boolean recognition/recording gate returns false. This module does not change DSP flags or SoundTrigger module enumeration. It hooks boolean no-argument methods in ApWakeupService/AecWakeupService and changes false to true only while the stack is inside the Bixby AEC restart path (`onRestart`/`restart`).

This is intentionally a narrow runtime experiment. It does not touch Bluetooth, DeX, audio HAL, or system XML.
