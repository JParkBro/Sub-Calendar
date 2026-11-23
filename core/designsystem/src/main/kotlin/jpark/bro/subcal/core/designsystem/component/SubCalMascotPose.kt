package jpark.bro.subcal.core.designsystem.component

import androidx.annotation.DrawableRes
import jpark.bro.subcal.core.designsystem.R

/** 마스코트 정지 이미지. 모두 80×80 픽셀아트 (theme-guide.md 7장 "마스코트") */
enum class SubCalMascotPose(
    @param:DrawableRes internal val drawable: Int,
) {
    FRONT_IDLE(R.drawable.mascot_front_idle_80),
    WAVE(R.drawable.mascot_wave_80),
    JUMP(R.drawable.mascot_jump_80),
    ARMS_UP(R.drawable.mascot_arms_up_80),
    BLOB(R.drawable.mascot_blob_80),
    BLOB_MINI(R.drawable.mascot_blob_mini_80),
    BLOB_OPEN_MOUTH(R.drawable.mascot_blob_open_mouth_80),
    BLOB_SMILE(R.drawable.mascot_blob_smile_80),
    FACE_NEUTRAL(R.drawable.mascot_face_neutral_80),
    FACE_SMILE(R.drawable.mascot_face_smile_80),
    FACE_LAUGH(R.drawable.mascot_face_laugh_80),
    FACE_CONTENT(R.drawable.mascot_face_content_80),
    FACE_SURPRISED(R.drawable.mascot_face_surprised_80),
    FACE_WORRIED(R.drawable.mascot_face_worried_80),
    FACE_CRYING(R.drawable.mascot_face_crying_80),
    FACE_ANGRY(R.drawable.mascot_face_angry_80),
    FACE_SLEEPY(R.drawable.mascot_face_sleepy_80),
    FACE_CONFUSED(R.drawable.mascot_face_confused_80),
    FACE_SHY(R.drawable.mascot_face_shy_80),
    FACE_SMUG(R.drawable.mascot_face_smug_80),
}
