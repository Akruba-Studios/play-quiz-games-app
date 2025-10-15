package com.akrubastudios.playquizgames.ui.screens.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val JawsIcon: ImageVector
    get() {
        if (_jaws != null) {
            return _jaws!!
        }
        _jaws = Builder(
            name = "Jaws",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(15.56f, 9.09f)
                curveTo(14.66f, 7.18f, 12.54f, 6.0f, 11.0f, 6.0f)
                curveToRelative(0.47f, 1.4f, 0.28f, 2.57f, -0.05f, 3.43f)
                curveTo(8.45f, 10.0f, 6.0f, 11.0f, 6.0f, 11.0f)
                reflectiveCurveTo(3.0f, 7.0f, 1.0f, 7.0f)
                lineToRelative(2.0f, 6.0f)
                lineToRelative(-1.0f, 4.0f)
                curveToRelative(2.0f, 0.0f, 4.0f, -3.0f, 4.0f, -3.0f)
                reflectiveCurveToRelative(5.0f, 2.0f, 8.0f, 2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.65f, 0.0f, 1.91f, -0.83f, 2.73f, -2.23f)
                curveToRelative(1.24f, -0.21f, 2.27f, -0.56f, 3.14f, -0.96f)
                curveToRelative(-0.15f, -0.18f, -0.34f, -0.37f, -0.59f, -0.51f)
                curveToRelative(-0.65f, -0.39f, -1.46f, -0.63f, -2.28f, -0.8f)
                curveToRelative(0.82f, -0.16f, 1.67f, -0.28f, 2.59f, -0.15f)
                curveToRelative(0.46f, 0.06f, 0.95f, 0.19f, 1.41f, 0.51f)
                curveToRelative(0.1f, 0.06f, 0.17f, 0.14f, 0.24f, 0.19f)
                curveTo(22.4f, 13.26f, 23.0f, 12.44f, 23.0f, 12.0f)
                curveToRelative(0.0f, -0.87f, -3.81f, -2.5f, -7.44f, -2.91f)
                moveTo(18.0f, 12.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.24f, 0.1f, -0.45f, 0.23f, -0.62f)
                curveToRelative(0.61f, 0.12f, 1.2f, 0.29f, 1.74f, 0.46f)
                curveToRelative(0.03f, 0.05f, 0.03f, 0.1f, 0.03f, 0.16f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
            }
        }.build()
        return _jaws!!
    }

private var _jaws: ImageVector? = null

val AnglerFishIcon: ImageVector
    get() {
        if (_anglerFish != null) {
            return _anglerFish!!
        }
        _anglerFish = Builder(
            name = "AnglerFish",
            defaultWidth = 512.0.dp,
            defaultHeight = 512.0.dp,
            viewportWidth = 512.0f,
            viewportHeight = 512.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(442.91f, 52.16f)
                curveToRelative(-0.81f, 0.02f, -1.62f, 0.08f, -2.44f, 0.19f)
                curveToRelative(-13.12f, 1.79f, -22.99f, 13.47f, -30.47f, 32.88f)
                curveToRelative(-5.99f, 15.55f, -10.81f, 37.09f, -13.94f, 66.5f)
                curveToRelative(-32.02f, -22.09f, -63.77f, -29.54f, -95.28f, -25.84f)
                lineToRelative(-26.97f, -60.06f)
                lineToRelative(-24.97f, 71.28f)
                lineTo(198.5f, 107.28f)
                lineToRelative(-0.81f, 63.06f)
                lineToRelative(-46.56f, 1.75f)
                lineToRelative(12.53f, 30.56f)
                curveToRelative(-17.5f, 16.25f, -34.96f, 34.4f, -52.4f, 53.84f)
                lineToRelative(-92.81f, -45.38f)
                lineToRelative(44.88f, 75.91f)
                lineToRelative(-36.47f, 39.1f)
                lineToRelative(36.28f, 2.78f)
                lineToRelative(-40.47f, 59.88f)
                lineToRelative(108.06f, -64.9f)
                lineToRelative(87.56f, 42.53f)
                lineToRelative(-49.75f, 17.38f)
                lineToRelative(35.56f, 13.81f)
                lineToRelative(-32.56f, 22.22f)
                lineToRelative(52.16f, 0.97f)
                lineToRelative(-4.59f, 46.85f)
                lineToRelative(59.31f, -72.03f)
                lineTo(365.0f, 437.65f)
                lineTo(496.06f, 270.47f)
                arcToRelative(653.0f, 653.0f, 0.0f, false, false, -11.59f, -19.09f)
                lineToRelative(-119.38f, 150.97f)
                lineToRelative(-4.72f, 5.97f)
                lineToRelative(-6.78f, -3.41f)
                lineToRelative(-44.34f, -22.22f)
                lineToRelative(8.38f, -16.72f)
                lineToRelative(37.53f, 18.81f)
                lineTo(473.69f, 234.91f)
                curveToRelative(-20.16f, -29.63f, -40.19f, -52.59f, -60.09f, -69.66f)
                curveToRelative(2.96f, -34.52f, 8.11f, -58.35f, 13.88f, -73.31f)
                curveToRelative(6.36f, -16.51f, 12.88f, -20.71f, 15.5f, -21.06f)
                curveToRelative(1.31f, -0.18f, 2.63f, 0.05f, 4.75f, 1.53f)
                curveToRelative(2.12f, 1.49f, 4.69f, 4.29f, 7.0f, 8.19f)
                curveToRelative(4.58f, 7.74f, 8.01f, 19.56f, 8.22f, 31.69f)
                arcToRelative(23.33f, 23.33f, 0.0f, false, false, -13.44f, 21.16f)
                curveToRelative(0.0f, 12.88f, 10.44f, 23.31f, 23.31f, 23.31f)
                reflectiveCurveToRelative(23.31f, -10.44f, 23.31f, -23.31f)
                curveToRelative(0.0f, -9.75f, -6.0f, -18.11f, -14.5f, -21.59f)
                curveToRelative(-0.28f, -15.23f, -4.22f, -29.62f, -10.81f, -40.75f)
                curveToRelative(-3.32f, -5.61f, -7.35f, -10.48f, -12.38f, -14.0f)
                curveToRelative(-3.77f, -2.64f, -8.32f, -4.49f, -13.13f, -4.88f)
                curveToRelative(-0.8f, -0.06f, -1.6f, -0.08f, -2.41f, -0.06f)
                close()
                moveTo(390.56f, 210.31f)
                curveToRelative(3.15f, -0.13f, 6.36f, 0.24f, 9.56f, 1.09f)
                arcToRelative(32.0f, 32.0f, 0.0f, false, true, 8.44f, 3.63f)
                curveToRelative(-0.14f, 0.0f, -0.27f, -0.03f, -0.41f, -0.03f)
                curveToRelative(-9.92f, 0.0f, -17.97f, 6.64f, -17.97f, 14.81f)
                curveToRelative(0.0f, 8.18f, 8.05f, 14.81f, 17.97f, 14.81f)
                curveToRelative(6.5f, 0.0f, 12.16f, -2.86f, 15.31f, -7.13f)
                curveToRelative(0.64f, 4.24f, 0.46f, 8.68f, -0.72f, 13.09f)
                curveToRelative(-4.57f, 17.07f, -22.11f, 27.2f, -39.19f, 22.63f)
                curveToRelative(-17.07f, -4.58f, -27.23f, -22.11f, -22.66f, -39.19f)
                curveToRelative(3.72f, -13.87f, 16.02f, -23.18f, 29.66f, -23.72f)
                close()
                moveTo(295.19f, 221.47f)
                lineToRelative(17.91f, 5.31f)
                lineToRelative(-12.75f, 43.06f)
                lineToRelative(47.44f, 38.84f)
                lineToRelative(-11.84f, 14.47f)
                lineToRelative(-52.25f, -42.78f)
                lineToRelative(-4.78f, -3.94f)
                lineToRelative(1.75f, -5.94f)
                close()
                moveTo(250.25f, 247.16f)
                lineToRelative(17.94f, 5.31f)
                lineToRelative(-8.88f, 29.94f)
                lineToRelative(33.44f, 27.38f)
                lineToRelative(-11.84f, 14.47f)
                lineToRelative(-38.25f, -31.31f)
                lineToRelative(-4.81f, -3.94f)
                lineToRelative(1.78f, -5.97f)
                close()
            }
        }.build()
        return _anglerFish!!
    }

private var _anglerFish: ImageVector? = null

val FishSharpIcon: ImageVector
    get() {
        if (_fishSharp != null) {
            return _fishSharp!!
        }
        _fishSharp = Builder(
            name = "FishSharp",
            defaultWidth = 512.0.dp,
            defaultHeight = 512.0.dp,
            viewportWidth = 512.0f,
            viewportHeight = 512.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(335.45f, 256.0f)
                arcToRelative(214.83f, 214.83f, 0.0f, false, true, 29.08f, -108.0f)
                lineToRelative(7.62f, -13.26f)
                arcToRelative(280.7f, 280.7f, 0.0f, false, false, -48.64f, -7.15f)
                curveToRelative(-21.94f, -16.9f, -54.64f, -36.95f, -92.34f, -43.33f)
                lineTo(208.0f, 80.0f)
                lineToRelative(13.37f, 61.86f)
                curveToRelative(-41.67f, 14.14f, -78.43f, 42.86f, -102.76f, 62.62f)
                quadToRelative(-7.06f, -5.91f, -14.78f, -11.55f)
                curveToRelative(-39.71f, -29.0f, -82.6f, -31.8f, -84.4f, -31.9f)
                lineTo(0.0f, 160.0f)
                lineToRelative(2.67f, 19.31f)
                curveToRelative(0.29f, 2.0f, 6.79f, 44.73f, 31.65f, 76.52f)
                curveTo(9.46f, 287.63f, 3.0f, 330.33f, 2.67f, 332.36f)
                lineTo(0.0f, 352.0f)
                lineToRelative(19.43f, -1.36f)
                curveToRelative(1.8f, -0.1f, 44.69f, -2.89f, 84.4f, -31.9f)
                quadToRelative(7.58f, -5.53f, 14.56f, -11.37f)
                curveToRelative(24.37f, 19.83f, 61.14f, 48.6f, 102.86f, 62.74f)
                lineTo(208.0f, 432.0f)
                lineToRelative(23.17f, -4.22f)
                curveToRelative(37.49f, -6.34f, 70.08f, -26.4f, 92.0f, -43.32f)
                arcToRelative(284.3f, 284.3f, 0.0f, false, false, 49.32f, -7.23f)
                lineToRelative(-7.91f, -13.18f)
                arcTo(214.9f, 214.9f, 0.0f, false, true, 335.45f, 256.0f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(499.59f, 221.75f)
                curveToRelative(-5.85f, -9.88f, -16.54f, -24.9f, -34.19f, -40.28f)
                arcToRelative(209.8f, 209.8f, 0.0f, false, false, -62.0f, -37.0f)
                lineTo(392.23f, 164.0f)
                arcToRelative(183.22f, 183.22f, 0.0f, false, false, -0.09f, 183.87f)
                lineToRelative(11.75f, 19.57f)
                arcToRelative(209.3f, 209.3f, 0.0f, false, false, 61.42f, -36.49f)
                curveTo(497.05f, 303.47f, 512.0f, 269.0f, 512.0f, 256.0f)
                curveToRelative(0.0f, -12.31f, -8.0f, -26.74f, -12.41f, -34.25f)
                moveTo(416.0f, 256.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, true, true, 16.0f, -16.0f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -16.0f, 16.0f)
            }
        }.build()
        return _fishSharp!!
    }

private var _fishSharp: ImageVector? = null

val JellyfishIcon: ImageVector
    get() {
        if (_jellyfish != null) {
            return _jellyfish!!
        }
        _jellyfish = Builder(
            name = "Jellyfish",
            defaultWidth = 32.0.dp,
            defaultHeight = 32.0.dp,
            viewportWidth = 32.0f,
            viewportHeight = 32.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = EvenOdd
            ) {
                moveTo(23.0f, 32.0f)
                arcToRelative(8.0f, 8.0f, 0.0f, false, true, -7.0f, -4.124f)
                arcTo(8.0f, 8.0f, 0.0f, false, true, 9.0f, 32.0f)
                lineTo(7.5f, 32.0f)
                arcToRelative(3.0f, 3.0f, 0.0f, false, true, -1.702f, -5.47f)
                curveTo(3.162f, 25.98f, 1.0f, 23.672f, 1.0f, 20.645f)
                lineTo(1.0f, 19.0f)
                arcToRelative(3.0f, 3.0f, 0.0f, true, true, 6.0f, 0.0f)
                verticalLineToRelative(-2.014f)
                curveToRelative(-1.77f, -1.5f, -3.0f, -3.564f, -3.0f, -5.986f)
                curveTo(4.0f, 6.117f, 8.31f, 0.0f, 16.0f, 0.0f)
                reflectiveCurveToRelative(12.0f, 6.117f, 12.0f, 11.0f)
                curveToRelative(0.0f, 2.422f, -1.23f, 4.485f, -3.0f, 5.986f)
                lineTo(25.0f, 19.0f)
                arcToRelative(3.0f, 3.0f, 0.0f, true, true, 6.0f, 0.0f)
                verticalLineToRelative(1.646f)
                curveToRelative(0.0f, 3.027f, -2.162f, 5.335f, -4.798f, 5.883f)
                arcTo(3.0f, 3.0f, 0.0f, false, true, 24.5f, 32.0f)
                close()
                moveTo(9.0f, 18.285f)
                verticalLineToRelative(2.36f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, -4.0f, 0.0f)
                lineTo(5.0f, 19.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, false, -2.0f, 0.0f)
                verticalLineToRelative(1.646f)
                curveToRelative(0.0f, 2.973f, 3.13f, 4.907f, 5.789f, 3.578f)
                arcTo(4.0f, 4.0f, 0.0f, false, false, 11.0f, 20.646f)
                verticalLineToRelative(-1.449f)
                curveToRelative(-0.73f, -0.253f, -1.402f, -0.56f, -2.0f, -0.912f)
                moveToRelative(14.0f, 0.0f)
                curveToRelative(-0.598f, 0.352f, -1.27f, 0.659f, -2.0f, 0.912f)
                verticalLineToRelative(1.449f)
                arcToRelative(4.0f, 4.0f, 0.0f, false, false, 2.211f, 3.578f)
                curveToRelative(2.66f, 1.33f, 5.789f, -0.605f, 5.789f, -3.578f)
                lineTo(29.0f, 19.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, false, -2.0f, 0.0f)
                verticalLineToRelative(1.646f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, true, -4.0f, 0.0f)
                close()
                moveTo(15.0f, 19.97f)
                arcToRelative(16.0f, 16.0f, 0.0f, false, true, -2.0f, -0.245f)
                lineTo(13.0f, 24.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, false, true, -4.0f, 4.0f)
                lineTo(7.5f, 28.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, false, 0.0f, 2.0f)
                lineTo(9.0f, 30.0f)
                arcToRelative(6.0f, 6.0f, 0.0f, false, false, 6.0f, -6.0f)
                close()
                moveTo(19.0f, 19.725f)
                quadToRelative(-0.963f, 0.183f, -2.0f, 0.245f)
                lineTo(17.0f, 24.0f)
                arcToRelative(6.0f, 6.0f, 0.0f, false, false, 6.0f, 6.0f)
                horizontalLineToRelative(1.5f)
                arcToRelative(1.0f, 1.0f, 0.0f, true, false, 0.0f, -2.0f)
                lineTo(23.0f, 28.0f)
                arcToRelative(4.0f, 4.0f, 0.0f, false, true, -4.0f, -4.0f)
                close()
                moveTo(26.0f, 11.0f)
                curveToRelative(0.0f, 3.866f, -4.477f, 7.0f, -10.0f, 7.0f)
                reflectiveCurveTo(6.0f, 14.866f, 6.0f, 11.0f)
                curveToRelative(0.0f, -3.491f, 2.854f, -8.016f, 8.194f, -8.861f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, false, -0.18f, 1.092f)
                curveToRelative(-2.608f, 0.59f, -5.417f, 2.334f, -6.604f, 5.5f)
                curveToRelative(-1.5f, 4.0f, 1.552f, 4.88f, 2.988f, 2.73f)
                curveToRelative(0.508f, -0.76f, 1.015f, -1.844f, 1.533f, -2.951f)
                curveToRelative(0.809f, -1.73f, 1.644f, -3.516f, 2.55f, -4.21f)
                quadToRelative(0.204f, 0.239f, 0.472f, 0.404f)
                curveTo(13.986f, 8.816f, 13.006f, 16.0f, 16.076f, 16.0f)
                curveToRelative(3.092f, 0.0f, 2.076f, -7.287f, 1.102f, -11.384f)
                quadToRelative(0.19f, -0.14f, 0.344f, -0.318f)
                curveToRelative(0.907f, 0.691f, 1.743f, 2.48f, 2.553f, 4.212f)
                curveToRelative(0.518f, 1.107f, 1.025f, 2.19f, 1.533f, 2.951f)
                curveToRelative(1.436f, 2.15f, 4.488f, 1.27f, 2.988f, -2.73f)
                curveToRelative(-1.311f, -3.496f, -4.217f, -5.258f, -6.598f, -5.647f)
                lineTo(18.0f, 3.0f)
                curveToRelative(0.0f, -0.308f, -0.07f, -0.6f, -0.194f, -0.861f)
                curveTo(23.146f, 2.984f, 26.0f, 7.509f, 26.0f, 11.0f)
            }
        }.build()
        return _jellyfish!!
    }

private var _jellyfish: ImageVector? = null

val TropicalFishIcon: ImageVector
    get() {
        if (_tropicalFish != null) {
            return _tropicalFish!!
        }
        _tropicalFish = Builder(
            name = "TropicalFish",
            defaultWidth = 512.0.dp,
            defaultHeight = 512.0.dp,
            viewportWidth = 512.0f,
            viewportHeight = 512.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(162.62f, 52.61f)
                curveTo(97.73f, 52.9f, 43.2f, 71.02f, 20.26f, 77.07f)
                curveToRelative(180.53f, -16.45f, 207.45f, 29.55f, 202.45f, 80.0f)
                curveToRelative(-18.22f, 37.85f, -25.25f, 55.3f, -27.92f, 70.57f)
                curveToRelative(-0.1f, -0.09f, -0.19f, -0.17f, -0.3f, -0.26f)
                arcToRelative(196.0f, 196.0f, 0.0f, false, false, -0.35f, 4.6f)
                curveToRelative(-1.17f, 9.33f, -0.92f, 18.5f, -0.84f, 31.85f)
                curveToRelative(0.23f, 42.95f, 20.35f, 78.61f, 35.56f, 123.86f)
                curveToRelative(-19.41f, 34.98f, -62.9f, 40.41f, -111.14f, 40.02f)
                curveToRelative(15.68f, 4.24f, 57.39f, 35.15f, 183.93f, -18.27f)
                curveToRelative(-18.39f, 38.19f, -101.93f, 66.29f, -104.35f, 80.71f)
                curveToRelative(95.71f, -8.18f, 156.83f, -50.26f, 172.44f, -113.7f)
                curveToRelative(26.88f, -16.66f, 51.02f, -37.67f, 71.22f, -88.49f)
                curveToRelative(4.98f, -15.1f, 26.36f, -16.51f, 42.84f, -20.37f)
                curveToRelative(8.09f, -1.89f, 12.8f, -17.88f, 0.0f, -19.66f)
                curveToRelative(-16.86f, -3.74f, -41.09f, -1.76f, -50.57f, -18.96f)
                curveTo(359.96f, 85.33f, 251.56f, 52.22f, 162.62f, 52.61f)
                moveTo(68.11f, 170.79f)
                curveToRelative(-6.35f, -0.04f, -11.51f, 0.99f, -14.99f, 3.3f)
                curveToRelative(52.98f, 29.35f, 72.78f, 42.04f, 73.21f, 52.06f)
                curveToRelative(0.31f, 18.48f, -56.01f, 23.49f, -57.59f, 33.36f)
                curveToRelative(-0.6f, 3.75f, 45.22f, 28.75f, 51.97f, 38.28f)
                curveToRelative(8.18f, 14.7f, -18.26f, 35.89f, -52.67f, 58.99f)
                curveToRelative(7.18f, 11.28f, 72.52f, -13.6f, 116.69f, -36.81f)
                curveToRelative(-7.36f, -34.74f, -12.07f, -70.13f, -6.83f, -105.55f)
                curveToRelative(-32.39f, -23.23f, -82.26f, -43.47f, -109.78f, -43.64f)
                close()
                moveTo(377.91f, 204.89f)
                curveToRelative(17.18f, 0.0f, 31.3f, 14.12f, 31.3f, 31.3f)
                reflectiveCurveToRelative(-14.12f, 31.3f, -31.3f, 31.3f)
                reflectiveCurveToRelative(-31.3f, -14.12f, -31.3f, -31.3f)
                reflectiveCurveToRelative(14.12f, -31.3f, 31.3f, -31.3f)
                moveToRelative(-71.58f, 12.44f)
                curveToRelative(4.01f, 0.06f, 8.04f, 0.29f, 12.08f, 0.7f)
                lineToRelative(0.49f, 0.05f)
                lineToRelative(0.48f, 0.1f)
                curveToRelative(7.51f, 1.61f, 10.56f, 7.37f, 12.7f, 12.56f)
                curveToRelative(2.15f, 5.19f, 3.38f, 11.14f, 4.01f, 17.52f)
                curveToRelative(1.25f, 12.75f, 0.43f, 26.89f, -5.97f, 37.98f)
                curveToRelative(-3.82f, 6.61f, -9.49f, 8.91f, -16.46f, 11.56f)
                curveToRelative(-6.97f, 2.65f, -15.51f, 4.69f, -25.05f, 5.83f)
                curveToRelative(-19.08f, 2.29f, -42.13f, 1.07f, -62.62f, -8.35f)
                lineToRelative(-19.9f, -9.15f)
                lineToRelative(20.58f, -7.49f)
                curveToRelative(9.96f, -3.62f, 17.25f, -7.86f, 22.49f, -11.59f)
                curveToRelative(-8.46f, -3.52f, -18.93f, -9.31f, -28.45f, -18.88f)
                lineToRelative(-10.3f, -10.36f)
                lineToRelative(13.89f, -4.54f)
                curveToRelative(26.63f, -8.71f, 53.96f, -16.39f, 82.04f, -15.95f)
                close()
                moveTo(377.91f, 222.89f)
                curveToRelative(-7.45f, 0.0f, -13.3f, 5.85f, -13.3f, 13.3f)
                curveToRelative(0.0f, 5.78f, 3.53f, 10.58f, 8.58f, 12.45f)
                arcToRelative(13.77f, 13.01f, 0.0f, false, true, -1.8f, -6.4f)
                arcToRelative(13.77f, 13.01f, 0.0f, false, true, 13.77f, -13.01f)
                arcToRelative(13.77f, 13.01f, 0.0f, false, true, 4.55f, 0.75f)
                curveToRelative(-2.2f, -4.24f, -6.61f, -7.09f, -11.8f, -7.09f)
                close()
                moveTo(306.05f, 235.34f)
                curveToRelative(-19.51f, -0.27f, -39.51f, 4.09f, -59.92f, 10.1f)
                curveToRelative(11.73f, 7.2f, 22.06f, 8.86f, 22.06f, 8.86f)
                lineToRelative(14.73f, 2.25f)
                lineToRelative(-8.85f, 11.99f)
                reflectiveCurveToRelative(-3.55f, 4.69f, -10.19f, 10.02f)
                curveToRelative(-2.56f, 2.05f, -5.81f, 4.29f, -9.45f, 6.55f)
                curveToRelative(10.91f, 1.81f, 22.13f, 1.86f, 32.03f, 0.67f)
                curveToRelative(8.26f, -0.99f, 15.59f, -2.8f, 20.81f, -4.79f)
                curveToRelative(5.22f, -1.98f, 8.04f, -5.09f, 7.26f, -3.74f)
                curveToRelative(2.87f, -4.97f, 4.63f, -17.15f, 3.64f, -27.23f)
                curveToRelative(-0.49f, -5.04f, -1.58f, -9.63f, -2.72f, -12.39f)
                curveToRelative(-0.48f, -1.16f, -0.86f, -1.54f, -1.09f, -1.81f)
                curveToRelative(-2.76f, -0.23f, -5.53f, -0.44f, -8.31f, -0.48f)
                close()
            }
        }.build()
        return _tropicalFish!!
    }

private var _tropicalFish: ImageVector? = null

val WhaleIcon: ImageVector
    get() {
        if (_whale != null) {
            return _whale!!
        }
        _whale = Builder(
            name = "Whale",
            defaultWidth = 64.0.dp,
            defaultHeight = 64.0.dp,
            viewportWidth = 64.0f,
            viewportHeight = 64.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF0dccd1)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(35.3f, 45.7f)
                curveToRelative(0.6f, 3.7f, -0.5f, 8.7f, -2.6f, 9.2f)
                reflectiveCurveToRelative(-4.5f, -3.8f, -5.1f, -7.5f)
                reflectiveCurveToRelative(0.9f, -5.6f, 3.1f, -6.0f)
                curveToRelative(2.1f, -0.5f, 4.1f, 0.6f, 4.6f, 4.3f)
            }
            path(
                fill = SolidColor(Color(0xFF0dccd1)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(54.1f, 11.3f)
                curveToRelative(-3.8f, 0.0f, -6.8f, -4.3f, -7.3f, -2.3f)
                curveToRelative(-0.5f, 2.3f, 3.5f, 6.7f, 6.0f, 6.7f)
                curveToRelative(0.0f, 0.0f, -8.6f, 6.2f, -46.7f, 6.2f)
                curveToRelative(-3.5f, 0.0f, -3.9f, 4.4f, -3.9f, 4.4f)
                curveToRelative(11.0f, 10.5f, 27.7f, 30.7f, 44.5f, 18.1f)
                curveToRelative(7.9f, -6.0f, 9.7f, -24.9f, 9.8f, -27.7f)
                curveToRelative(4.6f, -1.6f, 5.6f, -4.2f, 5.6f, -7.6f)
                curveToRelative(-0.1f, -3.4f, -2.4f, 2.2f, -8.0f, 2.2f)
            }
            path(
                fill = SolidColor(Color(0xFFdae5ef)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(2.2f, 26.4f)
                reflectiveCurveToRelative(-0.4f, 3.3f, 0.0f, 6.0f)
                curveToRelative(1.3f, 7.9f, 7.5f, 18.4f, 24.9f, 18.4f)
                curveToRelative(10.8f, 0.0f, 13.0f, -1.4f, 17.8f, -4.9f)
                quadToRelative(1.05f, -0.75f, 1.8f, -1.5f)
                curveTo(31.5f, 48.5f, 20.0f, 37.0f, 20.0f, 27.9f)
                curveToRelative(0.0f, -1.6f, -11.8f, -2.1f, -17.8f, -1.5f)
            }
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(45.1f, 45.5f)
                curveToRelative(-3.2f, 1.4f, -6.8f, 2.1f, -10.4f, 1.9f)
                reflectiveCurveToRelative(-7.1f, -1.3f, -10.2f, -3.1f)
                reflectiveCurveToRelative(-5.8f, -4.4f, -7.6f, -7.5f)
                curveToRelative(-1.9f, -3.1f, -2.9f, -6.7f, -2.7f, -10.4f)
                lineToRelative(1.9f, 0.1f)
                curveToRelative(-0.3f, 3.3f, 0.5f, 6.7f, 2.1f, 9.6f)
                curveToRelative(1.6f, 3.0f, 4.0f, 5.5f, 6.9f, 7.4f)
                reflectiveCurveToRelative(6.2f, 3.1f, 9.7f, 3.4f)
                curveToRelative(3.5f, 0.4f, 7.1f, -0.1f, 10.3f, -1.4f)
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(39.0f, 49.3f)
                curveToRelative(-3.6f, 0.3f, -7.2f, 0.2f, -10.7f, -0.5f)
                reflectiveCurveToRelative(-7.0f, -2.0f, -10.0f, -4.1f)
                reflectiveCurveToRelative(-5.5f, -4.8f, -7.2f, -8.1f)
                curveTo(9.4f, 33.4f, 8.5f, 29.7f, 8.7f, 26.0f)
                lineToRelative(1.9f, 0.1f)
                curveToRelative(-0.3f, 3.3f, 0.3f, 6.8f, 1.8f, 9.9f)
                curveToRelative(1.4f, 3.1f, 3.7f, 5.8f, 6.5f, 7.9f)
                reflectiveCurveToRelative(6.1f, 3.6f, 9.5f, 4.4f)
                curveToRelative(3.5f, 1.0f, 7.1f, 1.2f, 10.6f, 1.0f)
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(28.7f, 50.8f)
                curveToRelative(-3.2f, -0.4f, -6.4f, -1.0f, -9.4f, -2.1f)
                reflectiveCurveTo(13.4f, 46.0f, 11.0f, 43.8f)
                reflectiveCurveToRelative(-4.3f, -4.9f, -5.5f, -8.0f)
                curveToRelative(-1.2f, -3.0f, -1.8f, -6.3f, -1.8f, -9.5f)
                horizontalLineToRelative(1.9f)
                curveToRelative(-0.1f, 3.1f, 0.2f, 6.1f, 1.2f, 9.1f)
                curveToRelative(1.0f, 2.9f, 2.6f, 5.6f, 4.9f, 7.8f)
                curveToRelative(2.2f, 2.2f, 4.9f, 3.9f, 7.8f, 5.2f)
                curveToRelative(2.9f, 1.1f, 6.0f, 1.9f, 9.2f, 2.4f)
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(27.1f, 27.0f)
                arcToRelative(2.2f, 2.3f, 0.0f, true, false, 4.4f, 0.0f)
                arcToRelative(2.2f, 2.3f, 0.0f, true, false, -4.4f, 0.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF3e4347)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(30.8f, 27.0f)
                curveToRelative(0.0f, 0.9f, -0.7f, 1.6f, -1.5f, 1.6f)
                reflectiveCurveToRelative(-1.5f, -0.7f, -1.5f, -1.6f)
                reflectiveCurveToRelative(0.7f, -1.6f, 1.5f, -1.6f)
                curveToRelative(0.8f, -0.1f, 1.5f, 0.6f, 1.5f, 1.6f)
            }
            path(fill = SolidColor(Color(0xFF0dccd1)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(26.6f, 28.6f)
                curveToRelative(0.1f, 0.0f, 4.1f, 2.8f, 5.4f, -0.6f)
                curveToRelative(0.7f, -1.8f, -5.4f, 0.6f, -5.4f, 0.6f)
            }
            path(fill = SolidColor(Color(0xFF3e4347)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(29.3f, 28.5f)
                curveToRelative(0.3f, 0.1f, 0.5f, 0.1f, 0.8f, 0.1f)
                reflectiveCurveToRelative(0.6f, 0.1f, 0.8f, 0.3f)
                curveToRelative(0.5f, 0.4f, 0.6f, 1.0f, 0.7f, 1.5f)
                curveToRelative(-0.1f, -0.3f, -0.2f, -0.5f, -0.3f, -0.7f)
                reflectiveCurveToRelative(-0.3f, -0.4f, -0.5f, -0.6f)
                curveToRelative(-0.2f, -0.1f, -0.5f, -0.2f, -0.7f, -0.3f)
                curveToRelative(-0.2f, 0.0f, -0.5f, -0.1f, -0.8f, -0.3f)
            }
            path(fill = SolidColor(Color(0xFF3e4347)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(2.2f, 26.4f)
                curveToRelative(2.4f, -0.4f, 4.8f, -0.7f, 7.3f, -0.7f)
                curveToRelative(2.5f, -0.1f, 4.9f, 0.1f, 7.4f, 0.6f)
                curveToRelative(2.4f, 0.5f, 4.8f, 1.4f, 6.9f, 2.5f)
                curveToRelative(1.1f, 0.5f, 2.1f, 1.2f, 3.3f, 1.4f)
                curveToRelative(1.1f, 0.2f, 2.3f, -0.3f, 3.4f, -0.9f)
                curveToRelative(-0.9f, 0.8f, -2.1f, 1.4f, -3.4f, 1.3f)
                reflectiveCurveToRelative(-2.4f, -0.7f, -3.5f, -1.2f)
                curveToRelative(-2.2f, -1.0f, -4.5f, -1.8f, -6.8f, -2.3f)
                reflectiveCurveToRelative(-4.8f, -0.7f, -7.2f, -0.8f)
                curveToRelative(-2.5f, 0.0f, -5.0f, 0.0f, -7.4f, 0.1f)
            }
            path(fill = SolidColor(Color(0xFF0dccd1)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero) {
                moveTo(47.0f, 44.2f)
                curveToRelative(0.7f, 4.8f, -0.6f, 11.2f, -3.3f, 11.8f)
                reflectiveCurveToRelative(-5.8f, -4.8f, -6.5f, -9.6f)
                reflectiveCurveToRelative(1.2f, -7.1f, 3.9f, -7.7f)
                curveToRelative(2.7f, -0.7f, 5.2f, 0.7f, 5.9f, 5.5f)
            }
        }.build()
        return _whale!!
    }

private var _whale: ImageVector? = null